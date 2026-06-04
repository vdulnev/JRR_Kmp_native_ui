package com.jrr.jrrkmp_native_ui.desktop

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.playback.LocalPlayerEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Phase-2 placeholder local player for desktop. Holds queue/index/mode state so
 * the UI binds correctly, but performs no audio output — remote (MCWS) zone
 * control is fully functional; on-device playback arrives in Phase 4 (VLCJ).
 */
class DesktopPlayerEngine : LocalPlayerEngine {
    private val log = Logger.withTag("playback:Local")

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    override val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _currentIndex = MutableStateFlow(0)
    override val currentIndex: StateFlow<Int> = _currentIndex

    private val _volume = MutableStateFlow(1.0f)
    override val volume: StateFlow<Float> = _volume

    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    override val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    override val queue: StateFlow<List<Track>> = _queue

    override fun getCurrentPosition(): Long = 0L
    override fun getDuration(): Long = 0L

    override fun setQueue(tracks: List<Track>, startIndex: Int) {
        log.d { "setQueue(${tracks.size}, start=$startIndex) [desktop stub]" }
        _queue.value = tracks
        _currentIndex.value = startIndex.coerceIn(0, maxOf(0, tracks.lastIndex))
    }

    override fun play() { _playbackState.value = PlaybackState.PLAYING }
    override fun pause() { _playbackState.value = PlaybackState.PAUSED }
    override fun stop() { _playbackState.value = PlaybackState.STOPPED }

    override fun playNext() {
        if (_queue.value.isNotEmpty()) {
            _currentIndex.value = (_currentIndex.value + 1).coerceAtMost(_queue.value.lastIndex)
        }
    }

    override fun playPrevious() {
        _currentIndex.value = (_currentIndex.value - 1).coerceAtLeast(0)
    }

    override fun seekTo(positionMs: Long) { /* no-op until Phase 4 */ }
    override fun setVolume(level: Float) { _volume.value = level.coerceIn(0f, 1f) }
    override fun setShuffleMode(mode: ShuffleMode) { _shuffleMode.value = mode }
    override fun setRepeatMode(mode: RepeatMode) { _repeatMode.value = mode }

    override fun removeTrack(index: Int) {
        _queue.value = _queue.value.toMutableList().also { if (index in it.indices) it.removeAt(index) }
    }

    override fun moveTrack(from: Int, to: Int) {
        val list = _queue.value.toMutableList()
        if (from in list.indices && to in list.indices) {
            list.add(to, list.removeAt(from))
            _queue.value = list
        }
    }

    override fun clearQueue() {
        _queue.value = emptyList()
        _currentIndex.value = 0
        _playbackState.value = PlaybackState.STOPPED
    }

    override fun getQueue(): List<Track> = _queue.value
    override fun getQueueSize(): Int = _queue.value.size
    override fun playByIndex(index: Int) {
        if (index in _queue.value.indices) _currentIndex.value = index
    }

    override fun addTracks(tracks: List<Track>) { _queue.value = _queue.value + tracks }

    override fun insertTracksNext(tracks: List<Track>) {
        val list = _queue.value.toMutableList()
        val at = (_currentIndex.value + 1).coerceIn(0, list.size)
        list.addAll(at, tracks)
        _queue.value = list
    }
}
