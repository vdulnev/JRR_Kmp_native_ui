package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.logged
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase

private val log = Logger.withTag("vm:Queue")

private fun QueueViewState.summary(): String = buildString {
    append("tracks=${queueTracks.size}")
    append(" downloaded=${downloadedTrackKeys.size}")
    append(" active=$activeIndex")
    append(" playing=$isPlaying")
    append(if (isLocal) " local" else " remote")
    if (isLoading) append(" loading")
    if (transientError != null) append(" err=$transientError")
}

data class QueueViewState(
    val queueTracks: List<Track> = emptyList(),
    val downloadedTrackKeys: Set<String> = emptySet(),
    val activeIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val isLocal: Boolean = true,
    val transientError: String? = null
)

class QueueViewModel(
    private val facade: AudioPlayerFacade,
    private val libraryRepository: LibraryRepository,
    private val database: JrrDatabase?
) : ViewModel() {

    constructor(
        facade: AudioPlayerFacade,
        libraryRepository: LibraryRepository
    ) : this(facade, libraryRepository, null)

    private val _state = MutableStateFlow(QueueViewState())
    val state: StateFlow<QueueViewState> = _state.asStateFlow()

    private val remoteQueueFlow = MutableStateFlow<List<Track>>(emptyList())
    private val isRemoteLoadingFlow = MutableStateFlow(false)

    init {
        log.d { "init" }
        state.logged(log, "state") { it.summary() }.launchIn(viewModelScope)
        // Observe local vs remote configuration and build states
        val baseStateFlow = combine(
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
        }

        combine(
            baseStateFlow,
            database?.downloadedTrackDao()?.getAllTracksFlow() ?: flowOf(emptyList())
        ) { state, downloadedTracks ->
            state.copy(downloadedTrackKeys = downloadedTracks.map { it.fileKey }.toSet())
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
            log.d { "loadRemoteQueue()" }
            isRemoteLoadingFlow.value = true
            try {
                val remoteTracks = libraryRepository.getRemoteQueue()
                log.d { "loaded ${remoteTracks.size} remote-queue tracks" }
                remoteQueueFlow.value = remoteTracks
            } catch (e: Exception) {
                log.e(e) { "loadRemoteQueue failed" }
                _state.update { it.copy(transientError = "Failed to load remote queue: ${e.message ?: "unknown error"}") }
            } finally {
                isRemoteLoadingFlow.value = false
            }
        }
    }

    fun playByIndex(index: Int) {
        log.d { "playByIndex($index)" }
        try {
            facade.playByIndex(index)
        } catch (e: Exception) {
            log.e(e) { "playByIndex failed index=$index" }
            _state.update { it.copy(transientError = "Failed to play item: ${e.message ?: "unknown error"}") }
        }
    }

    fun removeQueueTrack(index: Int) {
        log.d { "removeQueueTrack($index)" }
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
            log.e(e) { "removeQueueTrack failed index=$index" }
            _state.update { it.copy(transientError = "Failed to remove item: ${e.message ?: "unknown error"}") }
        }
    }

    fun moveQueueTrack(from: Int, to: Int) {
        log.d { "moveQueueTrack($from → $to)" }
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
            log.e(e) { "moveQueueTrack failed from=$from to=$to" }
            _state.update { it.copy(transientError = "Failed to move item: ${e.message ?: "unknown error"}") }
        }
    }

    fun clearQueue() {
        log.d { "clearQueue()" }
        try {
            facade.clearQueue()
            if (!_state.value.isLocal) {
                remoteQueueFlow.value = emptyList()
            }
        } catch (e: Exception) {
            log.e(e) { "clearQueue failed" }
            _state.update { it.copy(transientError = "Failed to clear queue: ${e.message ?: "unknown error"}") }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }
}
