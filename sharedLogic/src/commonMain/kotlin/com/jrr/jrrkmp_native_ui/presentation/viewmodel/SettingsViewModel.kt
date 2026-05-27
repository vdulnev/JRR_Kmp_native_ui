package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun clearDownloads() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    clearPhysicalDownloads()
                    database.downloadedTrackDao().deleteAll()
                } catch (e: Exception) {
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
