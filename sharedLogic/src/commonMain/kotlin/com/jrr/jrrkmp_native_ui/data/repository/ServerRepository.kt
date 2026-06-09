package com.jrr.jrrkmp_native_ui.data.repository

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.redact
import com.jrr.jrrkmp_native_ui.data.api.WebPlayLookupResult
import com.jrr.jrrkmp_native_ui.data.api.parseMcwsResponse
import com.jrr.jrrkmp_native_ui.data.api.webPlayLookup
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.SavedServerEntity
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.encodeBase64
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private val log = Logger.withTag("repo:Server")

data class McwsServerData(
    val host: String,
    val port: Int,
    val useSsl: Boolean,
    val sslPort: Int,
    val token: String?
)

/**
 * One real server and its connection profiles. A [name] of `null` marks a
 * standalone (ungrouped) profile — those each become their own single-profile
 * group so callers can render one flat list.
 */
data class ServerGroup(
    val name: String?,
    val profiles: List<SavedServerEntity>,
)

/**
 * Fold flat saved profiles into [ServerGroup]s: profiles sharing a non-null
 * `groupName` collapse into one group (most-recently-used profile first), and
 * each ungrouped profile becomes its own single-profile group. Groups are
 * ordered by their most-recently-used profile. Exposed as `internal` so unit
 * tests can drive it without a database; the public entry point is
 * [ServerRepository.getServerGroups].
 */
internal fun groupServers(servers: List<SavedServerEntity>): List<ServerGroup> {
    val grouped = servers
        .filter { it.groupName != null }
        .groupBy { it.groupName!! }
        .map { (name, profiles) -> ServerGroup(name, profiles.sortedByDescending { it.lastUsedAt }) }
    val standalone = servers
        .filter { it.groupName == null }
        .map { ServerGroup(null, listOf(it)) }
    return (grouped + standalone)
        .sortedByDescending { group -> group.profiles.maxOfOrNull { it.lastUsedAt } ?: 0L }
}

class ServerRepository(
    private val database: JrrDatabase,
    private val httpClient: HttpClient,
) {
    private val serverDao = database.savedServerDao()

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
    }

    suspend fun lookupAccessKey(key: String): WebPlayLookupResult? = withContext(Dispatchers.IO) {
        log.i { "lookupAccessKey(key=${key.redact()})" }
        webPlayLookup(httpClient, key)
    }

    suspend fun authenticate(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        username: String,
        passwordVal: String
    ): String? = withContext(Dispatchers.IO) {
        val scheme = if (useSsl) "https" else "http"
        val actualPort = if (useSsl) sslPort else port
        val url = "$scheme://$host:$actualPort/MCWS/v1/Authenticate"
        log.i { "authenticate(host=$host port=$actualPort ssl=$useSsl user=$username)" }

        try {
            val authValue = "$username:$passwordVal"
            val credential = "Basic ${authValue.encodeBase64()}"
            val response: HttpResponse = httpClient.get(url) {
                header("Authorization", credential)
                header("No-Auth", "true")
            }
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val xmlResponse = parseMcwsResponse(body)
                if (xmlResponse.status == "OK") {
                    val token = xmlResponse.items["Token"]
                    log.i { "authenticate ok host=$host token=${token.redact()}" }
                    token
                } else {
                    log.w { "authenticate: MCWS responded status=${xmlResponse.status} host=$host" }
                    null
                }
            } else {
                log.w { "authenticate: HTTP ${response.status.value} host=$host" }
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "authenticate failed host=$host" }
            null
        }
    }

    suspend fun checkAlive(
        host: String,
        port: Int,
        useSsl: Boolean,
        sslPort: Int,
        token: String
    ): String? = withContext(Dispatchers.IO) {
        val scheme = if (useSsl) "https" else "http"
        val actualPort = if (useSsl) sslPort else port
        val url = "$scheme://$host:$actualPort/MCWS/v1/Alive?Token=$token"
        log.d { "checkAlive(host=$host port=$actualPort ssl=$useSsl)" }

        try {
            val response: HttpResponse = httpClient.get(url) {
                header("No-Auth", "true")
            }
            if (response.status.value in 200..299) {
                val body = response.bodyAsText()
                val xmlResponse = parseMcwsResponse(body)
                if (xmlResponse.status == "OK") {
                    val friendly = xmlResponse.items["FriendlyName"] ?: "JRiver Server"
                    log.d { "checkAlive ok host=$host friendlyName=$friendly" }
                    friendly
                } else {
                    log.w { "checkAlive: MCWS responded status=${xmlResponse.status} host=$host" }
                    null
                }
            } else {
                log.w { "checkAlive: HTTP ${response.status.value} host=$host" }
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "checkAlive failed host=$host" }
            null
        }
    }

    suspend fun getAllServers(): List<SavedServerEntity> = withContext(Dispatchers.IO) {
        serverDao.getAllServers().also { log.d { "getAllServers → ${it.size}" } }
    }

    /** Saved profiles folded into groups (one entry per real server). */
    suspend fun getServerGroups(): List<ServerGroup> = withContext(Dispatchers.IO) {
        groupServers(serverDao.getAllServers()).also { log.d { "getServerGroups → ${it.size}" } }
    }

    /** Assign a profile to [groupName] (a new or existing group), or clear it with null. */
    suspend fun setProfileGroup(profileId: String, groupName: String?) = withContext(Dispatchers.IO) {
        log.i { "setProfileGroup(id=$profileId group=${groupName ?: "<none>"})" }
        serverDao.setGroupName(profileId, groupName?.takeIf { it.isNotBlank() })
    }

    /** Rename a group — every profile under [oldName] moves to [newName]. */
    suspend fun renameServerGroup(oldName: String, newName: String) = withContext(Dispatchers.IO) {
        if (newName.isBlank() || oldName == newName) return@withContext
        log.i { "renameServerGroup($oldName → $newName)" }
        serverDao.renameGroup(oldName, newName)
    }

    suspend fun getLastUsedServer(): SavedServerEntity? = withContext(Dispatchers.IO) {
        val s = serverDao.getLastUsedServer()
        log.d { "getLastUsedServer → ${s?.host ?: "none"}" }
        s
    }

    suspend fun saveServer(server: SavedServerEntity) = withContext(Dispatchers.IO) {
        log.i { "saveServer(host=${server.host} name=${server.friendlyName})" }
        serverDao.insert(server)
    }

    suspend fun deleteServer(server: SavedServerEntity) = withContext(Dispatchers.IO) {
        log.i { "deleteServer(host=${server.host})" }
        serverDao.delete(server)
    }

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
