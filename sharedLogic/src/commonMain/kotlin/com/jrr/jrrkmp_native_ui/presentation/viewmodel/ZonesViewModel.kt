package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.logged
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val log = Logger.withTag("vm:Zones")

private fun ZonesViewState.summary(): String = buildString {
    append("active=$activeZoneId")
    append(" server=${serverZones.size}")
    append(" device=${deviceZones.size}")
    append(" vol=${(currentVolume * 100).toInt()}%")
    append(" offline=$isOfflineMode")
    if (isLoading) append(" loading")
    if (transientError != null) append(" err=$transientError")
}

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
        log.d { "init" }
        state.logged(log, "state") { it.summary() }.launchIn(viewModelScope)
        var lastZone = facade.activeZone.value
        facade.activeZone
            .onEach { activeZone ->
                if (lastZone.isOffline && activeZone.isLocal) {
                    log.d { "activeZone Offline→Local, refreshing server zones" }
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
        log.d { "refreshZones()" }
        val isOfflineMode = facade.activeZone.value.isOffline || facade.currentServerHost.isNullOrEmpty()
        if (!isOfflineMode) {
            viewModelScope.launch {
                isLoadingFlow.value = true
                try {
                    val zones = libraryRepository.getZones()
                    log.d { "loaded ${zones.size} server zones" }
                    serverZonesFlow.value = zones
                } catch (e: Exception) {
                    log.e(e) { "refreshZones failed" }
                    _state.update { it.copy(transientError = "Failed to load server zones: ${e.message ?: "unknown error"}") }
                } finally {
                    isLoadingFlow.value = false
                }
            }
        } else {
            log.v { "refreshZones skipped (offline)" }
        }
    }

    fun selectZone(zone: Zone) {
        log.i { "selectZone(${zone.id})" }
        try {
            facade.setZone(zone)
        } catch (e: Exception) {
            log.e(e) { "selectZone failed zoneId=${zone.id}" }
            _state.update { it.copy(transientError = "Failed to select zone: ${e.message ?: "unknown error"}") }
        }
    }

    fun setVolume(level: Float) {
        log.d { "setVolume($level)" }
        try {
            facade.setVolume(level)
        } catch (e: Exception) {
            log.e(e) { "setVolume failed level=$level" }
            _state.update { it.copy(transientError = "Failed to set volume: ${e.message ?: "unknown error"}") }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }
}
