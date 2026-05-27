package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QueueViewState(
    val queueTracks: List<Track> = emptyList(),
    val activeIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isLocal: Boolean = true,
    val transientError: String? = null
)

class QueueViewModel(
    private val facade: AudioPlayerFacade,
    private val libraryRepository: LibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(QueueViewState())
    @NativeCoroutinesState
    val state: StateFlow<QueueViewState> = _state.asStateFlow()

    private val remoteQueueFlow = MutableStateFlow<List<Track>>(emptyList())
    private val isRemoteLoadingFlow = MutableStateFlow(false)

    init {
        // Observe local vs remote configuration and build states
        combine(
            facade.activeZone,
            facade.localQueue,
            facade.playerStatus,
            remoteQueueFlow,
            isRemoteLoadingFlow
        ) { activeZone, localQueue, playerStatus, remoteQueue, isRemoteLoading ->
            val isLocal = activeZone.isLocal || activeZone.isOffline || activeZone.isAndroidAuto
            val tracks = if (isLocal) localQueue else remoteQueue
            val activeIndex = playerStatus?.playingNowPosition ?: -1
            val isPlaying = playerStatus?.state == PlaybackState.PLAYING

            QueueViewState(
                queueTracks = tracks,
                activeIndex = activeIndex,
                isPlaying = isPlaying,
                isLoading = if (isLocal) false else isRemoteLoading,
                isLocal = isLocal
            )
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)

        // Trigger remote queue loading when needed
        combine(
            facade.activeZone,
            facade.playerStatus.map { it?.playingNowTracks }.distinctUntilChanged()
        ) { activeZone, _ ->
            val isLocal = activeZone.isLocal || activeZone.isOffline || activeZone.isAndroidAuto
            if (!isLocal) {
                loadRemoteQueue()
            }
        }.launchIn(viewModelScope)
    }

    private fun loadRemoteQueue() {
        viewModelScope.launch {
            isRemoteLoadingFlow.value = true
            try {
                val remoteTracks = libraryRepository.getRemoteQueue()
                remoteQueueFlow.value = remoteTracks
            } catch (e: Exception) {
                _state.update { it.copy(transientError = "Failed to load remote queue: ${e.message ?: "unknown error"}") }
            } finally {
                isRemoteLoadingFlow.value = false
            }
        }
    }

    fun playByIndex(index: Int) {
        try {
            facade.playByIndex(index)
        } catch (e: Exception) {
            _state.update { it.copy(transientError = "Failed to play item: ${e.message ?: "unknown error"}") }
        }
    }

    fun removeQueueTrack(index: Int) {
        try {
            val isLocal = _state.value.isLocal
            facade.removeQueueTrack(index)
            if (!isLocal) {
                // Optimistic UI update
                val current = remoteQueueFlow.value.toMutableList()
                if (index in current.indices) {
                    current.removeAt(index)
                    remoteQueueFlow.value = current
                }
            }
        } catch (e: Exception) {
            _state.update { it.copy(transientError = "Failed to remove item: ${e.message ?: "unknown error"}") }
        }
    }

    fun moveQueueTrack(from: Int, to: Int) {
        try {
            val isLocal = _state.value.isLocal
            val currentTracks = if (isLocal) facade.localQueue.value else remoteQueueFlow.value
            if (from in currentTracks.indices && to in currentTracks.indices) {
                facade.moveQueueTrack(from, to)
                if (!isLocal) {
                    // Optimistic UI update
                    val current = remoteQueueFlow.value.toMutableList()
                    val item = current.removeAt(from)
                    current.add(to, item)
                    remoteQueueFlow.value = current
                }
            }
        } catch (e: Exception) {
            _state.update { it.copy(transientError = "Failed to move item: ${e.message ?: "unknown error"}") }
        }
    }

    fun clearQueue() {
        try {
            facade.clearQueue()
            if (!_state.value.isLocal) {
                remoteQueueFlow.value = emptyList()
            }
        } catch (e: Exception) {
            _state.update { it.copy(transientError = "Failed to clear queue: ${e.message ?: "unknown error"}") }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }
}
