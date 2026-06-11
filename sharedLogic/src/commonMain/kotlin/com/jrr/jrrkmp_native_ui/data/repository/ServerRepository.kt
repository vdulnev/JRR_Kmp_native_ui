package com.jrr.jrrkmp_native_ui.data.repository

import arrow.core.Either
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.redact
import com.jrr.jrrkmp_native_ui.data.api.McwsError
import com.jrr.jrrkmp_native_ui.data.api.WebPlayLookupResult
import com.jrr.jrrkmp_native_ui.data.api.mcwsAuthenticate
import com.jrr.jrrkmp_native_ui.data.api.mcwsCheckAlive
import com.jrr.jrrkmp_native_ui.data.api.webPlayLookup
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.SavedServerEntity
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

private val log = Logger.withTag("repo:Server")

data class McwsServerData(
    val host: String,
    val port: Int,
    val useSsl: Boolean,
    val sslPort: Int,
    val token: String?
)

/**
 * One real server: all connection profiles sharing a [serverId]. A server with
 * a single profile is a standalone server; profiles never appear ungrouped,
 * because every profile belongs to exactly one server identity.
 *
 * [displayName] comes from the most-recently-used profile's JRiver
 * FriendlyName (falling back to its host).
 */
data class ServerGroup(
    val serverId: String,
    val displayName: String,
    val profiles: List<SavedServerEntity>,
)

/**
 * Fold flat saved profiles into [ServerGroup]s by `serverId` (one entry per
 * real server, most-recently-used profile first; groups ordered by recency).
 * Exposed as `internal` so unit tests can drive it without a database; the
 * public entry point is [ServerRepository.getServerGroups].
 */
internal fun groupServers(servers: List<SavedServerEntity>): List<ServerGroup> =
    servers
        .groupBy { it.serverId }
        .map { (serverId, profiles) ->
            val ordered = profiles.sortedByDescending { it.lastUsedAt }
            val rep = ordered.first()
            ServerGroup(
                serverId = serverId,
                displayName = rep.friendlyName?.takeIf { it.isNotBlank() } ?: rep.host,
                profiles = ordered,
            )
        }
        .sortedByDescending { group -> group.profiles.maxOfOrNull { it.lastUsedAt } ?: 0L }

class ServerRepository(
    private val database: JrrDatabase,
    private val httpClient: HttpClient,
) {
    private val serverDao = database.savedServerDao()
    private val favoriteDao = database.favoriteDao()

    // Identity of the currently-connected real server. Favorites are scoped to
    // it; empty when disconnected/offline.
    private val _activeServerId = MutableStateFlow("")
    val activeServerId: StateFlow<String> = _activeServerId.asStateFlow()

    fun setActiveServerId(serverId: String) {
        log.i { "setActiveServerId($serverId)" }
        _activeServerId.value = serverId
    }

    // Serialises connection recovery so the facade (init) and the shell
    // view-model don't both authenticate/reconnect on startup. Once a recovery
    // succeeds, concurrent/subsequent callers short-circuit (the facade is
    // already connected, so re-running onRecovered is unnecessary).
    private val recoveryMutex = Mutex()
    private var recoverySucceeded = false

    private val _activeServer = MutableStateFlow<McwsServerData?>(null)
    val activeServer: StateFlow<McwsServerData?> = _activeServer.asStateFlow()

    fun setActiveServer(host: String, port: Int, useSsl: Boolean, sslPort: Int, token: String?) {
        log.i { "setActiveServer(host=$host port=$port ssl=$useSsl sslPort=$sslPort token=${token.redact()})" }
        _activeServer.value = if (host.isEmpty()) null else McwsServerData(host, port, useSsl, sslPort, token)
        if (host.isEmpty()) _activeServerId.value = ""
    }

    // The Either-returning members are the primary API for Kotlin callers
    // (ViewModels, composeUi) so they can tell "wrong password" from "server
    // unreachable". They are @HiddenFromObjC: Arrow generics erase at the
    // ObjC bridge, so Swift keeps the nullable wrappers below instead.

    /** Typed access-key lookup; see [McwsError] for the failure modes. */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    suspend fun lookupAccessKeyTyped(key: String): Either<McwsError, WebPlayLookupResult> =
        withContext(Dispatchers.IO) {
            log.d { "lookupAccessKeyTyped(key=${key.redact()})" }
            webPlayLookup(httpClient, key)
        }

    /** SKIE-facing variant: Swift sees a nullable result, never Either. */
    suspend fun lookupAccessKey(key: String): WebPlayLookupResult? =
        lookupAccessKeyTyped(key).getOrNull()

    /** Typed authenticate; Right = session token. */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    suspend fun authenticateTyped(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        username: String,
        passwordVal: String
    ): Either<McwsError, String> = withContext(Dispatchers.IO) {
        mcwsAuthenticate(httpClient, host, port, useSsl, sslPort, username, passwordVal)
    }

    /** SKIE-facing variant: Swift sees a nullable token, never Either. */
    suspend fun authenticate(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        username: String,
        passwordVal: String
    ): String? = authenticateTyped(host, port, useSsl, sslPort, username, passwordVal).getOrNull()

    /** Typed alive check; Right = the server's FriendlyName. */
    @OptIn(ExperimentalObjCRefinement::class)
    @HiddenFromObjC
    suspend fun checkAliveTyped(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        token: String
    ): Either<McwsError, String> = withContext(Dispatchers.IO) {
        mcwsCheckAlive(httpClient, host, port, useSsl, sslPort, token)
    }

    /** SKIE-facing variant: Swift sees a nullable name, never Either. */
    suspend fun checkAlive(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        token: String
    ): String? = checkAliveTyped(host, port, useSsl, sslPort, token).getOrNull()

    suspend fun getAllServers(): List<SavedServerEntity> = withContext(Dispatchers.IO) {
        serverDao.getAllServers().also { log.d { "getAllServers → ${it.size}" } }
    }

    /** Saved profiles folded into groups (one entry per real server). */
    suspend fun getServerGroups(): List<ServerGroup> = withContext(Dispatchers.IO) {
        groupServers(serverDao.getAllServers()).also { log.d { "getServerGroups → ${it.size}" } }
    }

    /**
     * Mark [profileId] as belonging to the same real server as [targetServerId].
     * If the profile was the last one of its old identity, that server's
     * favorites are merged into the target (deduped) and the old identity drops.
     */
    suspend fun mergeProfileIntoServer(profileId: String, targetServerId: String) = withContext(Dispatchers.IO) {
        val profile = serverDao.getServerById(profileId) ?: return@withContext
        val oldServerId = profile.serverId
        if (oldServerId == targetServerId) return@withContext
        log.i { "mergeProfileIntoServer(profile=$profileId $oldServerId → $targetServerId)" }
        serverDao.setServerId(profileId, targetServerId)
        // Only fold favorites when the old identity no longer has any profiles —
        // i.e. this profile *was* that (standalone) server.
        if (serverDao.countProfilesForServer(oldServerId) == 0) {
            favoriteDao.moveFavorites(oldServerId, targetServerId)
            favoriteDao.deleteFavoritesForServer(oldServerId)
        }
        if (_activeServerId.value == oldServerId) _activeServerId.value = targetServerId
    }

    /**
     * Split [profileId] out into its own brand-new real server identity. The
     * former server's favorites are duplicated onto the new identity, so the
     * now-distinct server starts with the same favorites. Returns the new id.
     */
    suspend fun splitProfileToNewServer(profileId: String): String = withContext(Dispatchers.IO) {
        val profile = serverDao.getServerById(profileId) ?: return@withContext ""
        val oldServerId = profile.serverId
        val newServerId = randomServerId()
        log.i { "splitProfileToNewServer(profile=$profileId $oldServerId → $newServerId)" }
        serverDao.setServerId(profileId, newServerId)
        // Duplicate (copy) the source server's favorites onto the new identity.
        favoriteDao.getAllFavorites(oldServerId).forEach { fav ->
            favoriteDao.insert(fav.copy(id = 0, serverId = newServerId))
        }
        newServerId
    }

    suspend fun getLastUsedServer(): SavedServerEntity? = withContext(Dispatchers.IO) {
        val s = serverDao.getLastUsedServer()
        log.d { "getLastUsedServer → ${s?.host ?: "none"}" }
        s
    }

    /**
     * Persist a connection profile, resolving its real-server identity:
     * - keep an explicit [SavedServerEntity.serverId] if set,
     * - else reuse the identity of an already-saved profile with the same id
     *   (so reconnecting is stable),
     * - else mint a new identity (a brand-new standalone server).
     *
     * Returns the resolved `serverId` so callers can mark it active.
     */
    suspend fun saveServer(server: SavedServerEntity): String = withContext(Dispatchers.IO) {
        val resolvedId = server.serverId.takeIf { it.isNotBlank() }
            ?: serverDao.getServerById(server.id)?.serverId?.takeIf { it.isNotBlank() }
            ?: randomServerId()
        log.i { "saveServer(host=${server.host} name=${server.friendlyName} serverId=$resolvedId)" }
        serverDao.insert(server.copy(serverId = resolvedId))
        resolvedId
    }

    suspend fun deleteServer(server: SavedServerEntity) = withContext(Dispatchers.IO) {
        log.i { "deleteServer(host=${server.host})" }
        serverDao.delete(server)
    }

    // Reasonably-unique identity for a new real server (timestamp + random).
    private fun randomServerId(): String =
        "srv-${io.ktor.util.date.getTimeMillis()}-${kotlin.random.Random.nextInt(100_000, 999_999)}"

    suspend fun recoverActiveServer(
        onRecovered: suspend (host: String, port: Int, useSsl: Boolean, sslPort: Int, token: String?) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        recoveryMutex.withLock {
            // Already recovered this process (e.g. the facade beat the shell to
            // it): the connection is live, so don't re-authenticate.
            if (recoverySucceeded) {
                log.d { "recoverActiveServer: already recovered, skipping" }
                return@withContext true
            }
            doRecoverActiveServer(onRecovered).also { recoverySucceeded = it }
        }
    }

    private suspend fun doRecoverActiveServer(
        onRecovered: suspend (host: String, port: Int, useSsl: Boolean, sslPort: Int, token: String?) -> Unit
    ): Boolean {
        return try {
            val lastServer = getLastUsedServer() ?: return false
            log.i { "Attempting background server connection recovery to: ${lastServer.host}" }
            // Restore favorites context to the recovered server's identity.
            if (lastServer.serverId.isNotBlank()) _activeServerId.value = lastServer.serverId

            // Immediately notify caller using the saved token to allow instant browse queries
            onRecovered(lastServer.host, lastServer.port, lastServer.useSsl, lastServer.sslPort, lastServer.authToken)

            // Asynchronously verify token validity or re-authenticate if expired
            val token = lastServer.authToken
            val checkedFriendlyName = if (token != null) {
                checkAlive(
                    lastServer.host,
                    lastServer.port,
                    lastServer.useSsl,
                    lastServer.sslPort,
                    token
                )
            } else null

            if (checkedFriendlyName == null) {
                log.i { "Saved server authentication token invalid/expired. Re-authenticating..." }
                val newToken = authenticate(
                    lastServer.host,
                    lastServer.port,
                    lastServer.useSsl,
                    lastServer.sslPort,
                    lastServer.username,
                    lastServer.passwordKey
                )
                if (newToken != null) {
                    val finalFriendlyName = checkAlive(
                        lastServer.host,
                        lastServer.port,
                        lastServer.useSsl,
                        lastServer.sslPort,
                        newToken
                    ) ?: lastServer.friendlyName ?: "JRiver Server"

                    val updatedServer = lastServer.copy(
                        friendlyName = finalFriendlyName,
                        lastUsedAt = io.ktor.util.date.getTimeMillis(),
                        authToken = newToken
                    )
                    saveServer(updatedServer)
                    log.i { "Re-authentication successful. Recovered server: $finalFriendlyName" }
                    onRecovered(lastServer.host, lastServer.port, lastServer.useSsl, lastServer.sslPort, newToken)
                    true
                } else {
                    log.w { "Re-authentication failed for recovered server" }
                    false
                }
            } else {
                log.i { "Saved token is valid. Recovered server: $checkedFriendlyName" }
                true
            }
        } catch (e: Exception) {
            log.e(e) { "Server connection recovery failed" }
            false
        }
    }
}
