package com.jrr.jrrkmp_native_ui.playback

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

private val log = Logger.withTag("playback:Local")

interface NativePlayerController {
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setVolume(level: Float)
    fun setQueue(tracks: List<Track>, startIndex: Int)
    fun playByIndex(index: Int)
    fun removeTrack(index: Int)
    fun moveTrack(from: Int, to: Int)
    fun clearQueue()
    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun addTracks(tracks: List<Track>)
    fun insertTracksNext(tracks: List<Track>)
}

class IosLocalPlayerEngine : LocalPlayerEngine {
    private var controller: NativePlayerController? = null

    fun setController(controller: NativePlayerController) {
        log.i { "setController (wired to Swift NativePlayerController)" }
        this.controller = controller
    }

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    override val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _currentIndex = MutableStateFlow(-1)
    override val currentIndex: StateFlow<Int> = _currentIndex

    private val _volume = MutableStateFlow(1.0f)
    override val volume: StateFlow<Float> = _volume

    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    override val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    override val queue: StateFlow<List<Track>> = _queue

    // Callbacks for Swift to update the state of the flows
    fun updatePlaybackState(state: PlaybackState) {
        log.v { "← updatePlaybackState($state)" }
        _playbackState.value = state
    }

    fun updateCurrentIndex(index: Int) {
        log.v { "← updateCurrentIndex($index)" }
        _currentIndex.value = index
    }

    fun updateVolume(vol: Float) {
        log.v { "← updateVolume($vol)" }
        _volume.value = vol
    }

    fun updateShuffleMode(mode: ShuffleMode) {
        log.d { "← updateShuffleMode($mode)" }
        _shuffleMode.value = mode
    }

    fun updateRepeatMode(mode: RepeatMode) {
        log.d { "← updateRepeatMode($mode)" }
        _repeatMode.value = mode
    }

    fun updateQueue(tracks: List<Track>) {
        log.v { "← updateQueue(${tracks.size} tracks)" }
        _queue.value = tracks
    }

    // LocalPlayerEngine implementation delegating to Swift controller
    override fun getCurrentPosition(): Long {
        return controller?.getCurrentPosition() ?: 0L
    }

    override fun getDuration(): Long {
        return controller?.getDuration() ?: 0L
    }

    override fun setQueue(tracks: List<Track>, startIndex: Int) {
        log.d { "setQueue(${tracks.size} tracks, startIndex=$startIndex)" }
        _queue.value = tracks
        _currentIndex.value = startIndex
        controller?.setQueue(tracks, startIndex)
    }

    override fun play() {
        log.d { "play()" }
        controller?.play()
    }

    override fun pause() {
        log.d { "pause()" }
        controller?.pause()
    }

    override fun stop() {
        log.d { "stop()" }
        controller?.stop()
    }

    override fun playNext() {
        val nextIdx = _currentIndex.value + 1
        log.d { "playNext() current=${_currentIndex.value} → $nextIdx (size=${_queue.value.size})" }
        if (nextIdx < _queue.value.size) {
            playByIndex(nextIdx)
        }
    }

    override fun playPrevious() {
        val prevIdx = _currentIndex.value - 1
        log.d { "playPrevious() current=${_currentIndex.value} → $prevIdx" }
        if (prevIdx >= 0) {
            playByIndex(prevIdx)
        }
    }

    override fun seekTo(positionMs: Long) {
        log.d { "seekTo(${positionMs}ms)" }
        controller?.seekTo(positionMs)
    }

    override fun setVolume(level: Float) {
        log.d { "setVolume($level)" }
        _volume.value = level
        controller?.setVolume(level)
    }

    override fun setShuffleMode(mode: ShuffleMode) {
        log.d { "setShuffleMode($mode)" }
        _shuffleMode.value = mode
    }

    override fun setRepeatMode(mode: RepeatMode) {
        log.d { "setRepeatMode($mode)" }
        _repeatMode.value = mode
    }

    override fun removeTrack(index: Int) {
        log.d { "removeTrack($index)" }
        val list = _queue.value.toMutableList()
        if (index >= 0 && index < list.size) {
            list.removeAt(index)
            _queue.value = list
        }
        controller?.removeTrack(index)
    }

    override fun moveTrack(from: Int, to: Int) {
        log.d { "moveTrack($from → $to)" }
        val list = _queue.value.toMutableList()
        if (from >= 0 && from < list.size && to >= 0 && to < list.size) {
            val track = list.removeAt(from)
            list.add(to, track)
            _queue.value = list
        }
        controller?.moveTrack(from, to)
    }

    override fun clearQueue() {
        log.d { "clearQueue()" }
        _queue.value = emptyList()
        _currentIndex.value = -1
        _playbackState.value = PlaybackState.STOPPED
        controller?.clearQueue()
    }

    override fun getQueue(): List<Track> {
        return _queue.value
    }

    override fun getQueueSize(): Int {
        return _queue.value.size
    }

    override fun playByIndex(index: Int) {
        log.d { "playByIndex($index)" }
        _currentIndex.value = index
        controller?.playByIndex(index)
    }

    override fun addTracks(tracks: List<Track>) {
        log.d { "addTracks(${tracks.size})" }
        val list = _queue.value.toMutableList()
        list.addAll(tracks)
        _queue.value = list
        controller?.addTracks(tracks)
    }

    override fun insertTracksNext(tracks: List<Track>) {
        log.d { "insertTracksNext(${tracks.size}) current=${_currentIndex.value}" }
        val list = _queue.value.toMutableList()
        val currentIdx = _currentIndex.value
        val insertIndex = if (currentIdx >= 0) currentIdx + 1 else 0
        if (insertIndex in 0..list.size) {
            list.addAll(insertIndex, tracks)
        } else {
            list.addAll(tracks)
        }
        _queue.value = list
        controller?.insertTracksNext(tracks)
    }
}
