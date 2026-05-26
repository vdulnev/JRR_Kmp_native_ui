package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ZonesViewState(
    val serverZones: List<Zone> = emptyList(),
    val deviceZones: List<Zone> = listOf(Zone.Local, Zone.Offline),
    val activeZoneId: String = "",
    val currentVolume: Float = 0.5f,
    val isLoading: Boolean = false,
    val isOfflineMode: Boolean = true,
    val transientError: String? = null
)

class ZonesViewModel(
    private val facade: AudioPlayerFacade,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ZonesViewState())
    val state: StateFlow<ZonesViewState> = _state.asStateFlow()

    private val serverZonesFlow = MutableStateFlow<List<Zone>>(emptyList())
    private val isLoadingFlow = MutableStateFlow(false)

    init {
        var lastZone = facade.activeZone.value
        facade.activeZone
            .onEach { activeZone ->
                if (lastZone.isOffline && activeZone.isLocal) {
                    refreshZones()
                }
                lastZone = activeZone
            }
            .launchIn(viewModelScope)

        // Observe facade states and combine them
        combine(
            facade.activeZone,
            facade.playerStatus,
            serverZonesFlow,
            isLoadingFlow
        ) { activeZone, playerStatus, serverZones, isLoading ->
            val isOfflineMode = activeZone.isOffline || facade.currentServerHost.isNullOrEmpty()
            val activeVolume = playerStatus?.volume ?: 0.5f

            ZonesViewState(
                serverZones = if (isOfflineMode) emptyList() else serverZones,
                activeZoneId = activeZone.id,
                currentVolume = activeVolume,
                isLoading = if (isOfflineMode) false else isLoading,
                isOfflineMode = isOfflineMode
            )
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)

        refreshZones()
    }

    fun refreshZones() {
        val isOfflineMode = facade.activeZone.value.isOffline || facade.currentServerHost.isNullOrEmpty()
        if (!isOfflineMode) {
            viewModelScope.launch {
                isLoadingFlow.value = true
                try {
                    val zones = libraryRepository.getZones()
                    serverZonesFlow.value = zones
                } catch (e: Exception) {
                    _state.update { it.copy(transientError = "Failed to load server zones: ${e.message ?: "unknown error"}") }
                } finally {
                    isLoadingFlow.value = false
                }
            }
        }
    }

    fun selectZone(zone: Zone) {
        try {
            facade.setZone(zone)
        } catch (e: Exception) {
            _state.update { it.copy(transientError = "Failed to select zone: ${e.message ?: "unknown error"}") }
        }
    }

    fun setVolume(level: Float) {
        try {
            facade.setVolume(level)
        } catch (e: Exception) {
            _state.update { it.copy(transientError = "Failed to set volume: ${e.message ?: "unknown error"}") }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }
}
