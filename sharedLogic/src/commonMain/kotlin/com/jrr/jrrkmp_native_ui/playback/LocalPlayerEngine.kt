package com.jrr.jrrkmp_native_ui.playback

import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.TrackInfo
import kotlinx.coroutines.flow.StateFlow

interface LocalPlayerEngine {
    val playbackState: StateFlow<PlaybackState>
    val currentTrack: StateFlow<TrackInfo?>
    val currentIndex: StateFlow<Int>
    val volume: StateFlow<Float>
    val shuffleMode: StateFlow<ShuffleMode>
    val repeatMode: StateFlow<RepeatMode>
    val queue: StateFlow<List<TrackInfo>>

    fun getCurrentPosition(): Long
    fun getDuration(): Long
    fun setQueue(tracks: List<TrackInfo>, startIndex: Int)
    fun play()
    fun pause()
    fun stop()
    fun playNext()
    fun playPrevious()
    fun seekTo(positionMs: Long)
    fun setVolume(level: Float)
    fun setShuffleMode(mode: ShuffleMode)
    fun setRepeatMode(mode: RepeatMode)
    fun removeTrack(index: Int)
    fun moveTrack(from: Int, to: Int)
    fun clearQueue()
    fun getQueue(): List<TrackInfo>
    fun getQueueSize(): Int
    fun playByIndex(index: Int)
}
