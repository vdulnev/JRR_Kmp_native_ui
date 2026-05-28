package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.logged
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.DownloadJobEntity
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val log = Logger.withTag("vm:Settings")

private fun SettingsViewState.summary(): String = buildString {
    append("offline=$isOfflineMode")
    if (serverHost != null) {
        append(" server=$serverHost:${if (useSsl) serverSslPort else serverPort}")
        if (useSsl) append("(ssl)")
    }
    append(" downloaded=$downloadedTracksCount")
    append(" jobs=${downloadJobs.size}")
    if (transientError != null) append(" err=$transientError")
}

data class SettingsViewState(
    val isOfflineMode: Boolean = true,
    val serverHost: String? = null,
    val useSsl: Boolean = false,
    // Match AudioPlayerFacade.currentServerPort / currentServerSslPort
    // defaults — visible only for the one frame before the first combine
    // emission.
    val serverPort: Int = 52199,
    val serverSslPort: Int = 52200,
    val downloadedTracksCount: Int = 0,
    val downloadJobs: List<DownloadJobEntity> = emptyList(),
    val transientError: String? = null,
)

class SettingsViewModel(
    private val facade: AudioPlayerFacade,
    private val database: JrrDatabase,
    private val clearPhysicalDownloads: () -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsViewState())
    val state: StateFlow<SettingsViewState> = _state.asStateFlow()

    init {
        log.d { "init" }
        combine(
            facade.activeZone,
            // connectionToken is a trigger only — its value is unused, but its
            // change rebuilds the state so the facade's currentServer* getters
            // are re-read when the active server swaps.
            facade.connectionToken,
            database.downloadedTrackDao().getAllTracksFlow(),
            database.downloadJobDao().getAllJobsFlow(),
        ) { activeZone, _, downloadedTracks, jobs ->
            val isOffline = activeZone.isOffline || facade.currentServerHost.isNullOrEmpty()
            SettingsViewState(
                isOfflineMode = isOffline,
                serverHost = facade.currentServerHost,
                useSsl = facade.currentServerUseSsl,
                serverPort = facade.currentServerPort,
                serverSslPort = facade.currentServerSslPort,
                downloadedTracksCount = downloadedTracks.size,
                downloadJobs = jobs,
            )
        }
            .distinctUntilChanged()
            .logged(log, "state") { it.summary() }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun clearDownloads() {
        log.i { "clearDownloads()" }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    clearPhysicalDownloads()
                    val before = database.downloadedTrackDao().getAllTracks().size
                    database.downloadedTrackDao().deleteAll()
                    log.i { "cleared $before downloaded tracks + filesystem" }
                } catch (e: Exception) {
                    log.e(e) { "clearDownloads failed" }
                    _state.update {
                        it.copy(
                            transientError = "Failed to clear downloads: ${e.message ?: "unknown error"}",
                        )
                    }
                }
            }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }
}
