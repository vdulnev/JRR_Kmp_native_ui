package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import arrow.core.getOrElse
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.data.api.McwsError
import com.jrr.jrrkmp_native_ui.data.api.logSummary
import com.jrr.jrrkmp_native_ui.data.db.entity.SavedServerEntity
import com.jrr.jrrkmp_native_ui.data.repository.ServerGroup
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import io.ktor.util.date.getTimeMillis

private val log = Logger.withTag("vm:TvConnect")

/**
 * Outcome of a connect attempt. Carries the typed [McwsError] (a plain sealed
 * interface — no Arrow at this boundary) so the connect screen can say "wrong
 * password" vs "server unreachable" instead of a generic failure line.
 */
sealed interface TvConnectResult {
    data object Connected : TvConnectResult
    data class Failed(val error: McwsError) : TvConnectResult
}

/**
 * Connection flow for the Android TV app (form connect + launch restore). Keeps
 * `ServerRepository` and the facade out of the connect/root composables — they
 * just call [connect]/[restore] and react to the boolean result. Mirrors the
 * phone's `MainShellViewModel` ownership of connection logic.
 */
class TvConnectViewModel(
    private val serverRepository: ServerRepository,
    private val facade: AudioPlayerFacade,
) : ViewModel() {

    /**
     * Authenticate, bring the app online via the facade (which switches the zone
     * Offline → Local), and persist the server for restore. Returns
     * [TvConnectResult.Failed] with the typed error on bad credentials /
     * unreachable server.
     */
    suspend fun connect(
        host: String,
        port: Int,
        username: String,
        password: String,
        useSsl: Boolean = false,
        sslPort: Int = 52200,
    ): TvConnectResult {
        log.i { "connect(host=$host port=$port ssl=$useSsl)" }
        val token = serverRepository.authenticateTyped(host, port, useSsl, sslPort, username, password)
            .getOrElse { err ->
                log.w { "connect failed: ${err.logSummary()}" }
                return TvConnectResult.Failed(err)
            }
        facade.setServerConnection(host, port, useSsl, sslPort, token)
        val serverId = serverRepository.saveServer(
            SavedServerEntity(
                id = "$host:$port",
                host = host,
                port = port,
                username = username,
                passwordKey = password,
                friendlyName = host,
                lastUsedAt = getTimeMillis(),
                authToken = token,
                useSsl = useSsl,
                sslPort = sslPort,
            ),
        )
        serverRepository.setActiveServerId(serverId)
        return TvConnectResult.Connected
    }

    /**
     * On launch, restore the last-used server: re-authenticate from saved
     * credentials (falling back to the stored token), then connect via the
     * facade. Returns true if the app is now connected.
     */
    suspend fun restore(): Boolean {
        val last = runCatching { serverRepository.getLastUsedServer() }.getOrNull()
            ?: run {
                log.i { "restore: no saved server" }
                return false
            }
        // Failure detail (unreachable vs rejected) is logged at the net layer;
        // restore quietly falls back to the stored token either way.
        val fresh = serverRepository.authenticateTyped(
            last.host, last.port, last.useSsl, last.sslPort, last.username, last.passwordKey,
        ).getOrNull()
        val token = fresh ?: last.authToken
        if (token.isNullOrEmpty()) {
            log.w { "restore: no usable token for host=${last.host}" }
            return false
        }
        facade.setServerConnection(last.host, last.port, last.useSsl, last.sslPort, token)
        if (last.serverId.isNotBlank()) serverRepository.setActiveServerId(last.serverId)
        log.i { "restore: connected host=${last.host}" }
        return true
    }

    /**
     * Clear the active connection (Settings → Disconnect). Saved server
     * configurations are kept so the user can reconnect to them later.
     */
    suspend fun disconnect() {
        log.i { "disconnect()" }
        serverRepository.setActiveServer(host = "", port = 52199, useSsl = false, sslPort = 52200, token = null)
    }

    /** Saved server configurations, grouped by real server (for the picker). */
    suspend fun savedServers(): List<ServerGroup> = serverRepository.getServerGroups()

    /** Connect to a previously-saved profile (re-auth with its stored creds). */
    suspend fun connectSaved(server: SavedServerEntity): TvConnectResult =
        connect(server.host, server.port, server.username, server.passwordKey, server.useSsl, server.sslPort)

    /** Remove a single saved profile. */
    suspend fun deleteSaved(server: SavedServerEntity) {
        log.i { "deleteSaved(${server.host})" }
        serverRepository.deleteServer(server)
    }

    /** Mark [profileId] as the same real server as [targetServerId] (merges favorites). */
    suspend fun mergeSaved(profileId: String, targetServerId: String) {
        log.i { "mergeSaved($profileId → $targetServerId)" }
        serverRepository.mergeProfileIntoServer(profileId, targetServerId)
    }

    /** Split a profile into its own distinct server (duplicates favorites). */
    suspend fun splitSaved(profileId: String) {
        log.i { "splitSaved($profileId)" }
        serverRepository.splitProfileToNewServer(profileId)
    }
}
