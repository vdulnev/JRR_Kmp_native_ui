package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.logged
import com.jrr.jrrkmp_native_ui.core.logging.redact
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import io.ktor.util.date.getTimeMillis

private val log = Logger.withTag("vm:MainShell")

private fun MainShellState.summary(): String = buildString {
    append("tab=$activeTab")
    if (selectedAlbum != null) append(" album=${selectedAlbum.name}")
    if (showQueue) append(" queue")
    if (isAutoConnecting) append(" autoConnecting=$autoConnectServerName")
    if (toastMessage != null) append(" toast='$toastMessage'")
}

interface MainShellSettings {
    fun getLastActiveZoneId(): String?
    fun setLastActiveZoneId(zoneId: String?)
    fun getHasSavedServers(): Boolean
    fun setHasSavedServers(hasSaved: Boolean)
}

data class MainShellState(
    val activeTab: Int = 1,
    val selectedAlbum: Album? = null,
    val showQueue: Boolean = false,
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
        val initialTab = when {
            lastActiveZoneId == Zone.Offline.id -> 2
            hasSavedServers -> 2
            else -> 1
        }
        log.d { "init initialTab=$initialTab lastZone=$lastActiveZoneId hasSavedServers=$hasSavedServers" }
        _state.value = MainShellState(activeTab = initialTab)
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
            log.i { "auto-connect → Offline (last active zone)" }
            facade.setZone(Zone.Offline)
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

                    val token = serverRepository.authenticate(
                        lastServer.host,
                        lastServer.port,
                        lastServer.useSsl,
                        lastServer.sslPort,
                        lastServer.username,
                        lastServer.passwordKey
                    )

                    if (token != null) {
                        log.d { "authenticated token=${token.redact()}" }
                        val finalName = serverRepository.checkAlive(
                            lastServer.host,
                            lastServer.port,
                            lastServer.useSsl,
                            lastServer.sslPort,
                            token
                        ) ?: lastServer.friendlyName ?: "JRiver Server"

                        val updatedServer = lastServer.copy(
                            friendlyName = finalName,
                            lastUsedAt = getTimeMillis(),
                            authToken = token
                        )
                        serverRepository.saveServer(updatedServer)

                        facade.setServerConnection(
                            lastServer.host,
                            lastServer.port,
                            lastServer.useSsl,
                            lastServer.sslPort,
                            token
                        )

                        log.i { "auto-connect ok → $finalName" }
                        showToast("Connected to $finalName")
                        _state.update { it.copy(activeTab = 2) }
                    } else {
                        log.w { "auto-connect failed: authentication returned null" }
                        showToast("Auto-connect failed: Authentication error")
                        _state.update { it.copy(activeTab = 1) }
                    }
                } else {
                    log.d { "no saved server → show connect screen" }
                    settings.setHasSavedServers(false)
                    _state.update { it.copy(activeTab = 1) }
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
        _state.update { it.copy(isAutoConnecting = false, activeTab = 1) }
        showToast("Connection cancelled")
    }

    fun selectTab(tab: Int) {
        log.d { "selectTab($tab)" }
        _state.update { it.copy(activeTab = tab) }
        if (tab == 0) {
            _state.update { it.copy(selectedAlbum = null) }
        }
    }

    fun selectAlbum(album: Album?) {
        log.d { "selectAlbum(${album?.name})" }
        _state.update { it.copy(selectedAlbum = album) }
    }

    fun setShowQueue(show: Boolean) {
        log.d { "setShowQueue($show)" }
        _state.update { it.copy(showQueue = show) }
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
        facade.setServerConnection("", 0, false, 0, null)
        facade.setZone(Zone.Offline)
        _state.update { it.copy(activeTab = 1) }
    }
}
