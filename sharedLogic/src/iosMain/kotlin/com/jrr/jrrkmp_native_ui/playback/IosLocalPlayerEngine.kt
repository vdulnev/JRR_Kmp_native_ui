package com.jrr.jrrkmp_native_ui.playback

import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.TrackInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface NativePlayerController {
    fun play()
    fun pause()
    fun stop()
    fun seekTo(positionMs: Long)
    fun setVolume(level: Float)
    fun setQueue(tracks: List<TrackInfo>, startIndex: Int)
    fun playByIndex(index: Int)
    fun removeTrack(index: Int)
    fun moveTrack(from: Int, to: Int)
    fun clearQueue()
    fun getCurrentPosition(): Long
    fun getDuration(): Long
}

class IosLocalPlayerEngine : LocalPlayerEngine {
    private var controller: NativePlayerController? = null

    fun setController(controller: NativePlayerController) {
        this.controller = controller
    }

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    override val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _currentTrack = MutableStateFlow<TrackInfo?>(null)
    override val currentTrack: StateFlow<TrackInfo?> = _currentTrack

    private val _currentIndex = MutableStateFlow(-1)
    override val currentIndex: StateFlow<Int> = _currentIndex

    private val _volume = MutableStateFlow(1.0f)
    override val volume: StateFlow<Float> = _volume

    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    override val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private val _queue = MutableStateFlow<List<TrackInfo>>(emptyList())
    override val queue: StateFlow<List<TrackInfo>> = _queue

    // Callbacks for Swift to update the state of the flows
    fun updatePlaybackState(state: PlaybackState) {
        _playbackState.value = state
    }

    fun updateCurrentTrack(track: TrackInfo?) {
        _currentTrack.value = track
    }

    fun updateCurrentIndex(index: Int) {
        _currentIndex.value = index
    }

    fun updateVolume(vol: Float) {
        _volume.value = vol
    }

    fun updateShuffleMode(mode: ShuffleMode) {
        _shuffleMode.value = mode
    }

    fun updateRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    fun updateQueue(tracks: List<TrackInfo>) {
        _queue.value = tracks
    }

    // LocalPlayerEngine implementation delegating to Swift controller
    override fun getCurrentPosition(): Long {
        return controller?.getCurrentPosition() ?: 0L
    }

    override fun getDuration(): Long {
        return controller?.getDuration() ?: 0L
    }

    override fun setQueue(tracks: List<TrackInfo>, startIndex: Int) {
        _queue.value = tracks
        _currentIndex.value = startIndex
        if (startIndex >= 0 && startIndex < tracks.size) {
            _currentTrack.value = tracks[startIndex]
        } else {
            _currentTrack.value = null
        }
        controller?.setQueue(tracks, startIndex)
    }

    override fun play() {
        controller?.play()
    }

    override fun pause() {
        controller?.pause()
    }

    override fun stop() {
        controller?.stop()
    }

    override fun playNext() {
        val nextIdx = _currentIndex.value + 1
        if (nextIdx < _queue.value.size) {
            playByIndex(nextIdx)
        }
    }

    override fun playPrevious() {
        val prevIdx = _currentIndex.value - 1
        if (prevIdx >= 0) {
            playByIndex(prevIdx)
        }
    }

    override fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    override fun setVolume(level: Float) {
        _volume.value = level
        controller?.setVolume(level)
    }

    override fun setShuffleMode(mode: ShuffleMode) {
        _shuffleMode.value = mode
    }

    override fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    override fun removeTrack(index: Int) {
        val list = _queue.value.toMutableList()
        if (index >= 0 && index < list.size) {
            list.removeAt(index)
            _queue.value = list
        }
        controller?.removeTrack(index)
    }

    override fun moveTrack(from: Int, to: Int) {
        val list = _queue.value.toMutableList()
        if (from >= 0 && from < list.size && to >= 0 && to < list.size) {
            val track = list.removeAt(from)
            list.add(to, track)
            _queue.value = list
        }
        controller?.moveTrack(from, to)
    }

    override fun clearQueue() {
        _queue.value = emptyList()
        _currentTrack.value = null
        _currentIndex.value = -1
        _playbackState.value = PlaybackState.STOPPED
        controller?.clearQueue()
    }

    override fun getQueue(): List<TrackInfo> {
        return _queue.value
    }

    override fun getQueueSize(): Int {
        return _queue.value.size
    }

    override fun playByIndex(index: Int) {
        _currentIndex.value = index
        if (index >= 0 && index < _queue.value.size) {
            _currentTrack.value = _queue.value[index]
        } else {
            _currentTrack.value = null
        }
        controller?.playByIndex(index)
    }
}
