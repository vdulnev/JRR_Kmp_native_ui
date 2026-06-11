package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.logged
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val log = Logger.withTag("vm:MainShell")

private fun MainShellState.summary(): String = buildString {
    append("tab=$activeTab")
    if (isAutoConnecting) append(" autoConnecting=$autoConnectServerName")
    if (toastMessage != null) append(" toast='$toastMessage'")
}

interface MainShellSettings {
    fun getLastActiveZoneId(): String?
    fun setLastActiveZoneId(zoneId: String?)
    fun getHasSavedServers(): Boolean
    fun setHasSavedServers(hasSaved: Boolean)
}

/**
 * Connection-flow state. Navigation proper lives in the Decompose component
 * tree; [activeTab] survives only as a *bridge signal* — the connect flow flips
 * it (Server = 1, Player = 2) and each host forwards that to `root.selectTab`.
 */
data class MainShellState(
    val activeTab: Int = 1,
    val isAutoConnecting: Boolean = false,
    val autoConnectServerName: String = "",
    val hasAttemptedAutoConnect: Boolean = false,
    val toastMessage: String? = null
)

class MainShellViewModel(
    private val facade: AudioPlayerFacade,
    private val serverRepository: ServerRepository,
    private val settings: MainShellSettings
) : ViewModel() {

    private val _state = MutableStateFlow(MainShellState())
    val state: StateFlow<MainShellState> = _state.asStateFlow()

    private var autoConnectJob: Job? = null
    private var toastDismissJob: Job? = null

    init {
        val lastActiveZoneId = settings.getLastActiveZoneId()
        val hasSavedServers = settings.getHasSavedServers()
        // Always start on the Player/offline tab; the Server screen is only
        // entered explicitly (Connect button in Settings) — never on cold start.
        log.d { "init lastZone=$lastActiveZoneId hasSavedServers=$hasSavedServers" }
        _state.value = MainShellState(activeTab = 2)
        state.logged(log, "state") { it.summary() }.launchIn(viewModelScope)
    }

    fun performAutoConnect() {
        if (_state.value.hasAttemptedAutoConnect) {
            log.v { "performAutoConnect() skipped (already attempted)" }
            return
        }
        log.i { "performAutoConnect()" }
        _state.update { it.copy(hasAttemptedAutoConnect = true) }

        val lastActiveZoneId = settings.getLastActiveZoneId()
        if (lastActiveZoneId == Zone.Offline.id) {
            // The facade already restored the Offline zone — and asynchronously
            // reloaded its saved queue — during its own init, from the same
            // persisted zone id. Calling setZone(Offline) again here would run
            // saveQueueState() against the not-yet-populated engine and wipe the
            // persisted queue. So just surface the tab; don't re-set the zone.
            log.i { "auto-connect → Offline (last active zone, already restored)" }
            _state.update { it.copy(activeTab = 2) }
            return
        }

        autoConnectJob = viewModelScope.launch {
            try {
                val lastServer = serverRepository.getLastUsedServer()
                if (lastServer != null) {

                    log.i { "auto-connect to ${lastServer.friendlyName ?: lastServer.host}:${lastServer.port} ssl=${lastServer.useSsl}" }
                    settings.setHasSavedServers(true)
                    _state.update {
                        it.copy(
                            isAutoConnecting = true,
                            autoConnectServerName = lastServer.friendlyName ?: "JRiver Server"
                        )
                    }

                    // Delegate auto-connect recovery to ServerRepository
                    val success = serverRepository.recoverActiveServer { host, port, useSsl, sslPort, token ->
                        withContext(Dispatchers.Main) {
                            facade.setServerConnection(host, port, useSsl, sslPort, token)
                        }
                    }

                    if (success) {
                        val activeServer = serverRepository.getLastUsedServer()
                        val finalName = activeServer?.friendlyName ?: lastServer.friendlyName ?: "JRiver Server"
                        log.i { "auto-connect ok → $finalName" }
                        showToast("Connected to $finalName")
                        _state.update { it.copy(activeTab = 2) }
                    } else {
                        log.w { "auto-connect failed: authentication or connection check failed" }
                        showToast("Auto-connect failed: Connection or authentication error")
                        _state.update { it.copy(activeTab = 1) }
                    }
                } else {
                    log.d { "no saved server → enter offline mode" }
                    settings.setHasSavedServers(false)
                    facade.setZone(Zone.Offline)
                    _state.update { it.copy(activeTab = 2) }
                }
            } catch (e: Exception) {
                log.e(e) { "auto-connect failed" }
                showToast("Auto-connect failed: ${e.message ?: "unknown error"}")
                _state.update { it.copy(activeTab = 1) }
            } finally {
                _state.update { it.copy(isAutoConnecting = false) }
                autoConnectJob = null
            }
        }
    }

    fun cancelAutoConnect() {
        log.i { "cancelAutoConnect()" }
        autoConnectJob?.cancel()
        autoConnectJob = null
        facade.setZone(Zone.Offline)
        _state.update { it.copy(isAutoConnecting = false, activeTab = 2) }
        showToast("Connection cancelled")
    }

    fun showToast(message: String) {
        log.d { "showToast('$message')" }
        toastDismissJob?.cancel()
        _state.update { it.copy(toastMessage = message) }
        toastDismissJob = viewModelScope.launch {
            delay(2500)
            _state.update { it.copy(toastMessage = null) }
        }
    }

    fun clearToast() {
        toastDismissJob?.cancel()
        _state.update { it.copy(toastMessage = null) }
    }

    fun disconnect() {
        log.i { "disconnect()" }
        // Clearing the active server is enough: the facade observes it and
        // centrally stops all online activity (zone → Offline, polling,
        // download cancellation) — same path the TV apps' disconnect takes.
        facade.setServerConnection("", 0, false, 0, null)
        // Stay in the app on the Player/offline tab rather than returning to
        // the Server screen — the user can reach the server screen via Settings.
        _state.update { it.copy(activeTab = 2) }
    }
}
