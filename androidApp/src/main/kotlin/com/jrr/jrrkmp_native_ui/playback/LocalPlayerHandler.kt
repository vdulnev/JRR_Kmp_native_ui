package com.jrr.jrrkmp_native_ui.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import kotlinx.coroutines.runBlocking

class LocalPlayerHandler(
    private val context: Context,
    private val serverRepository: ServerRepository,
    private val checkLocalFileProvider: (String) -> String?
) : LocalPlayerEngine {
    private var exoPlayer: ExoPlayer? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack

    private val _currentIndex = MutableStateFlow(-1)
    override val currentIndex: StateFlow<Int> = _currentIndex

    private val _playbackState = MutableStateFlow(PlaybackState.STOPPED)
    override val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _shuffleMode = MutableStateFlow(ShuffleMode.OFF)
    override val shuffleMode: StateFlow<ShuffleMode> = _shuffleMode

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    override val repeatMode: StateFlow<RepeatMode> = _repeatMode

    private val _volume = MutableStateFlow(1.0f)
    override val volume: StateFlow<Float> = _volume

    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    override val queue: StateFlow<List<Track>> = _queue

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            updateState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            updateState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val player = exoPlayer ?: return
            val index = player.currentMediaItemIndex
            _currentIndex.value = index
            if (index >= 0 && index < _queue.value.size) {
                _currentTrack.value = _queue.value[index]
            } else {
                _currentTrack.value = null
            }
        }
    }

    private fun ensurePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context.applicationContext).build().apply {
                addListener(playerListener)
                volume = this@LocalPlayerHandler._volume.value
                repeatMode = when (this@LocalPlayerHandler._repeatMode.value) {
                    RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                    RepeatMode.TRACK -> Player.REPEAT_MODE_ONE
                    RepeatMode.PLAYLIST -> Player.REPEAT_MODE_ALL
                }
                shuffleModeEnabled = this@LocalPlayerHandler._shuffleMode.value == ShuffleMode.ON
            }
        }
    }

    private fun updateState() {
        val player = exoPlayer ?: return
        _playbackState.value = when {
            player.playbackState == Player.STATE_BUFFERING -> PlaybackState.PLAYING
            player.playbackState == Player.STATE_READY && player.isPlaying -> PlaybackState.PLAYING
            player.playbackState == Player.STATE_READY && !player.isPlaying -> PlaybackState.PAUSED
            player.playbackState == Player.STATE_ENDED -> PlaybackState.STOPPED
            else -> PlaybackState.STOPPED
        }
    }

    fun getUnderlyingPlayer(): ExoPlayer {
        ensurePlayer()
        return exoPlayer!!
    }

    override fun setQueue(tracks: List<Track>, startIndex: Int) {
        ensurePlayer()
        _queue.value = tracks
        val player = exoPlayer ?: return
        player.stop()
        player.clearMediaItems()

        val mediaItems = tracks.map { track ->
            val localPath = checkLocalFileProvider(track.fileKey)
            val uri = if (localPath != null && File(localPath).exists()) {
                Uri.fromFile(File(localPath))
            } else {
                val active = serverRepository.activeServer.value
                val (serverUrl, token) = if (active != null) {
                    val host = active.host
                    val scheme = if (active.useSsl) "https" else "http"
                    val port = if (active.useSsl) active.sslPort else active.port
                    Pair("$scheme://$host:$port/MCWS/v1", active.token ?: "")
                } else {
                    val activeServer = runBlocking {
                        serverRepository.getLastUsedServer()
                    }
                    if (activeServer != null) {
                        val host = activeServer.host
                        val scheme = if (activeServer.useSsl) "https" else "http"
                        val port = if (activeServer.useSsl) activeServer.sslPort else activeServer.port
                        Pair("$scheme://$host:$port/MCWS/v1", activeServer.authToken ?: "")
                    } else {
                        Pair("", "")
                    }
                }
                Uri.parse("${serverUrl}/File/GetFile?File=${track.fileKey}&Playback=1&Token=${token}")
            }

            MediaItem.Builder()
                .setUri(uri)
                .setMediaId(track.fileKey)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.name)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setArtworkUri(Uri.parse("content://com.jrr.jrrkmp_native_ui.fileprovider/downloads/art_${track.fileKey}.jpg"))
                        .build()
                )
                .build()
        }

        player.setMediaItems(mediaItems)
        if (startIndex >= 0 && startIndex < mediaItems.size) {
            player.seekTo(startIndex, 0L)
            _currentIndex.value = startIndex
            _currentTrack.value = tracks[startIndex]
        }
        player.prepare()
    }

    override fun play() {
        ensurePlayer()
        exoPlayer?.play()
    }

    override fun pause() {
        exoPlayer?.pause()
    }

    override fun stop() {
        exoPlayer?.stop()
        _playbackState.value = PlaybackState.STOPPED
    }

    override fun seekTo(positionMs: Long) {
        exoPlayer?.seekTo(positionMs)
    }

    override fun setVolume(level: Float) {
        _volume.value = level
        exoPlayer?.volume = level
    }

    override fun setShuffleMode(mode: ShuffleMode) {
        _shuffleMode.value = mode
        exoPlayer?.shuffleModeEnabled = (mode == ShuffleMode.ON)
    }

    override fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
        exoPlayer?.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.TRACK -> Player.REPEAT_MODE_ONE
            RepeatMode.PLAYLIST -> Player.REPEAT_MODE_ALL
        }
    }

    override fun playNext() {
        val player = exoPlayer ?: return
        if (player.hasNextMediaItem()) {
            player.seekToNext()
        }
    }

    override fun playPrevious() {
        val player = exoPlayer ?: return
        if (player.hasPreviousMediaItem()) {
            player.seekToPrevious()
        }
    }

    override fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }

    override fun getDuration(): Long {
        val duration = exoPlayer?.duration ?: 0L
        return if (duration < 0) 0L else duration
    }

    fun release() {
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun getQueue(): List<Track> = _queue.value

    override fun getQueueSize(): Int = _queue.value.size

    override fun playByIndex(index: Int) {
        val player = exoPlayer ?: return
        if (index >= 0 && index < player.mediaItemCount) {
            player.seekTo(index, 0L)
            player.play()
        }
    }

    override fun removeTrack(index: Int) {
        val player = exoPlayer ?: return
        if (index >= 0 && index < player.mediaItemCount) {
            player.removeMediaItem(index)
            val list = _queue.value.toMutableList()
            if (index < list.size) {
                list.removeAt(index)
                _queue.value = list
            }
        }
    }

    override fun moveTrack(from: Int, to: Int) {
        val player = exoPlayer ?: return
        if (from >= 0 && from < player.mediaItemCount && to >= 0 && to < player.mediaItemCount) {
            player.moveMediaItem(from, to)
            val list = _queue.value.toMutableList()
            if (from < list.size && to < list.size) {
                val item = list.removeAt(from)
                list.add(to, item)
                _queue.value = list
            }
        }
    }

    override fun clearQueue() {
        val player = exoPlayer ?: return
        player.clearMediaItems()
        _queue.value = emptyList()
        _currentTrack.value = null
        _currentIndex.value = -1
    }
}
