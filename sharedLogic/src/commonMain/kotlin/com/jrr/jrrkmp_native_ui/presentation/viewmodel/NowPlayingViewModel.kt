package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.logged
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import kotlinx.coroutines.flow.*

private val log = Logger.withTag("vm:NowPlaying")

private fun NowPlayingViewState.summary(): String = buildString {
    append("'$trackTitle'")
    append(" by '$artistName'")
    append(" playing=$isPlaying")
    append(" zone='$activeZoneName'")
    if (shuffleMode != ShuffleMode.OFF) append(" shuffle=$shuffleMode")
    if (repeatMode != RepeatMode.OFF) append(" repeat=$repeatMode")
    if (isMuted) append(" muted")
    if (transientError != null) append(" err=$transientError")
}

/**
 * Hot playback progress, deliberately kept OUT of [NowPlayingViewState]: the
 * facade ticks position every 500ms–1s, and folding it into the main state
 * made every consumer recompose/re-render per tick. UIs collect this flow
 * only in the leaf that draws the progress bar; everything else observes the
 * (now change-only) [NowPlayingViewModel.state].
 */
data class PlaybackPosition(
    val positionMs: Long = 0L,
    val durationMs: Long = 0L
)

data class NowPlayingViewState(
    val trackTitle: String = "Idle",
    val artistName: String = "Unknown Artist",
    val albumTitle: String = "Unknown Album",
    val isPlaying: Boolean = false,
    val volume: Float = 0.5f,
    val isMuted: Boolean = false,
    val shuffleMode: ShuffleMode = ShuffleMode.OFF,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val sampleRate: Int = 0,
    val activeZoneName: String = "No Zone Selected",
    val transientError: String? = null,
    val imageUrl: String = ""
)

class NowPlayingViewModel(
    private val facade: AudioPlayerFacade,
    private val mcwsClient: McwsClient,
) : ViewModel() {

    private val _state = MutableStateFlow(NowPlayingViewState())
    val state: StateFlow<NowPlayingViewState> = _state.asStateFlow()

    // Hot path: position ticks land here, not in [state]. MutableStateFlow
    // dedupes equal values, so [state] now only emits on real changes
    // (track switch, play/pause, volume, …).
    private val _position = MutableStateFlow(PlaybackPosition())
    val position: StateFlow<PlaybackPosition> = _position.asStateFlow()

    init {
        log.d { "init" }
        state.logged(log, "state") { it.summary() }.launchIn(viewModelScope)
        facade.playerStatus.onEach { status ->
            _position.value = PlaybackPosition(
                positionMs = status?.positionMs ?: 0L,
                durationMs = status?.durationMs ?: 0L,
            )
        }.launchIn(viewModelScope)
        // Observe facade state and update ViewModel state
        combine(
            facade.playerStatus,
            facade.activeZone,
            mcwsClient.activeServerFlow
        ) { status, activeZone, _ ->
            if (status != null) {
                NowPlayingViewState(
                    trackTitle = status.trackName.ifEmpty { "Idle" },
                    artistName = status.trackArtist.ifEmpty { "Unknown Artist" },
                    albumTitle = status.trackAlbum.ifEmpty { "Unknown Album" },
                    isPlaying = status.state == PlaybackState.PLAYING,
                    volume = status.volume,
                    isMuted = status.isMuted,
                    shuffleMode = status.shuffleMode,
                    repeatMode = status.repeatMode,
                    sampleRate = status.sampleRate,
                    activeZoneName = activeZone.name,
                    imageUrl = if (status.trackFileKey.isNotEmpty()) {
                        mcwsClient.buildImageUrl(status.trackFileKey)
                    } else {
                        ""
                    }
                )
            } else {
                NowPlayingViewState(
                    activeZoneName = activeZone.name
                )
            }
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)
    }

    fun play() {
        log.d { "play()" }
        try {
            facade.play()
        } catch (e: Exception) {
            log.e(e) { "play failed" }
            _state.update { it.copy(transientError = "Play failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun pause() {
        log.d { "pause()" }
        try {
            facade.pause()
        } catch (e: Exception) {
            log.e(e) { "pause failed" }
            _state.update { it.copy(transientError = "Pause failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun stop() {
        log.d { "stop()" }
        try {
            facade.stop()
        } catch (e: Exception) {
            log.e(e) { "stop failed" }
            _state.update { it.copy(transientError = "Stop failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun next() {
        log.d { "next()" }
        try {
            facade.next()
        } catch (e: Exception) {
            log.e(e) { "next failed" }
            _state.update { it.copy(transientError = "Next track failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun previous() {
        log.d { "previous()" }
        try {
            facade.previous()
        } catch (e: Exception) {
            log.e(e) { "previous failed" }
            _state.update { it.copy(transientError = "Previous track failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun seekTo(positionMs: Long) {
        log.d { "seekTo(${positionMs}ms)" }
        try {
            facade.seekTo(positionMs)
        } catch (e: Exception) {
            log.e(e) { "seekTo failed positionMs=$positionMs" }
            _state.update { it.copy(transientError = "Seek failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun setVolume(level: Float) {
        log.d { "setVolume($level)" }
        try {
            facade.setVolume(level)
        } catch (e: Exception) {
            log.e(e) { "setVolume failed level=$level" }
            _state.update { it.copy(transientError = "Set volume failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun toggleShuffle() {
        try {
            val currentMode = _state.value.shuffleMode
            val nextMode = when (currentMode) {
                ShuffleMode.OFF -> ShuffleMode.ON
                ShuffleMode.ON -> ShuffleMode.AUTOMATIC
                ShuffleMode.AUTOMATIC -> ShuffleMode.OFF
            }
            log.d { "toggleShuffle $currentMode → $nextMode" }
            facade.setShuffleMode(nextMode)
        } catch (e: Exception) {
            log.e(e) { "toggleShuffle failed" }
            _state.update { it.copy(transientError = "Shuffle mode change failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun toggleRepeat() {
        try {
            val currentMode = _state.value.repeatMode
            val nextMode = when (currentMode) {
                RepeatMode.OFF -> RepeatMode.PLAYLIST
                RepeatMode.PLAYLIST -> RepeatMode.TRACK
                RepeatMode.TRACK -> RepeatMode.OFF
            }
            log.d { "toggleRepeat $currentMode → $nextMode" }
            facade.setRepeatMode(nextMode)
        } catch (e: Exception) {
            log.e(e) { "toggleRepeat failed" }
            _state.update { it.copy(transientError = "Repeat mode change failed: ${e.message ?: "unknown error"}") }
        }
    }

    fun clearTransientError() {
        _state.update { it.copy(transientError = null) }
    }
}
