package com.jrr.jrrkmp_native_ui.playback

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val log = Logger.withTag("playback:Local")
private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

class LocalPlayerHandler(
    private val context: Context,
    private val checkLocalFileProvider: (String) -> String?,
    /**
     * Builds the MCWS `GetFile` stream URL for a track. Sourced from
     * [AudioPlayerFacade.streamUrl] so the URL format (incl. `Channels=2`) and
     * the transcode quality live in one place — the facade — rather than being
     * rebuilt here. Defaults to empty (no server) for standalone construction.
     */
    private val streamUrlProvider: (Track) -> String = { "" },
    /**
     * Builds the MCWS thumbnail URL for a file key — sourced from
     * [AudioPlayerFacade.artworkUrl] for the same reason.
     */
    private val artworkUrlProvider: (String) -> String = { "" },
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

    private fun getTrackFromMediaItem(mediaItem: MediaItem?): Track? {
        if (mediaItem == null) return null
        return (mediaItem.localConfiguration?.tag as? Track)
            ?: mediaItem.mediaMetadata.extras?.getString("track_json")?.let {
                try {
                    json.decodeFromString<Track>(it)
                } catch (e: Exception) {
                    null
                }
            }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            log.v { "ExoPlayer onPlaybackStateChanged(state=$state)" }
            updateState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            log.v { "ExoPlayer onIsPlayingChanged($isPlaying)" }
            _isPlaying.value = isPlaying
            updateState()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            val player = exoPlayer ?: return
            if (mediaItem == null) {
                if (_queue.value.isEmpty()) {
                    log.d { "ExoPlayer onMediaItemTransition(null) — queue empty, reset" }
                    _currentIndex.value = -1
                    _currentTrack.value = null
                }
                return
            }
            val index = player.currentMediaItemIndex
            log.d { "ExoPlayer onMediaItemTransition(reason=$reason) → index=$index" }
            _currentIndex.value = index
            if (index >= 0 && index < _queue.value.size) {
                _currentTrack.value = _queue.value[index]
            } else {
                val track = getTrackFromMediaItem(mediaItem)
                _currentTrack.value = track
            }
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            log.v { "ExoPlayer onTimelineChanged(reason=$reason, windowCount=${timeline.windowCount})" }
            val tracks = mutableListOf<Track>()
            val window = androidx.media3.common.Timeline.Window()
            for (i in 0 until timeline.windowCount) {
                timeline.getWindow(i, window)
                val track = getTrackFromMediaItem(window.mediaItem)
                if (track != null) {
                    tracks.add(track)
                }
            }
            _queue.value = tracks
            val index = exoPlayer?.currentMediaItemIndex ?: -1
            _currentIndex.value = index
            if (index in tracks.indices) {
                _currentTrack.value = tracks[index]
            } else {
                _currentTrack.value = null
            }
            updateState()
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            log.e(error) { "ExoPlayer error code=${error.errorCode}" }
        }
    }

    private fun ensurePlayer() {
        if (exoPlayer == null) {
            log.d { "ensurePlayer: creating ExoPlayer instance" }
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
            exoPlayer = ExoPlayer.Builder(context.applicationContext)
                .setAudioAttributes(audioAttributes, true)
                .setHandleAudioBecomingNoisy(true)
                .build().apply {
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

    private fun createMediaItem(track: Track): MediaItem {
        val localPath = checkLocalFileProvider(track.fileKey)
        val uri = if (localPath != null && File(localPath).exists()) {
            log.v { "createMediaItem(${track.fileKey}) → local file" }
            Uri.fromFile(File(localPath))
        } else {
            log.v { "createMediaItem(${track.fileKey}) → remote stream" }
            Uri.parse(streamUrlProvider(track))
        }

        val extras = android.os.Bundle().apply {
            putString("track_json", json.encodeToString(track))
        }

        return MediaItem.Builder()
            .setUri(uri)
            .setMediaId(track.fileKey)
            .setTag(track)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(track.name)
                    .setArtist(track.artist)
                    .setAlbumTitle(track.album)
                    .setArtworkUri(artworkUriFor(track.fileKey))
                    .setExtras(extras)
                    .build()
            )
            .build()
    }

    /**
     * Prefer the downloaded art file (instant, offline); otherwise fall back to
     * the MCWS thumbnail (via [artworkUrlProvider] → the facade) so remote
     * tracks still show a cover on the Now Playing screen / head unit.
     */
    private fun artworkUriFor(fileKey: String): Uri {
        val localArt = File(context.filesDir, "downloads/art_$fileKey.jpg")
        if (localArt.exists()) {
            return Uri.parse(
                "content://com.jrr.jrrkmp_native_ui.fileprovider/downloads/art_$fileKey.jpg"
            )
        }
        val remote = artworkUrlProvider(fileKey)
        if (remote.isNotEmpty()) {
            return Uri.parse(remote)
        }
        return Uri.parse(
            "content://com.jrr.jrrkmp_native_ui.fileprovider/downloads/art_$fileKey.jpg"
        )
    }

    override fun setQueue(tracks: List<Track>, startIndex: Int) {
        log.d { "setQueue(${tracks.size} tracks, startIndex=$startIndex)" }
        ensurePlayer()
        _queue.value = tracks
        val player = exoPlayer ?: return
        player.stop()
        player.clearMediaItems()

        val mediaItems = tracks.map { createMediaItem(it) }

        player.setMediaItems(mediaItems)
        if (startIndex >= 0 && startIndex < mediaItems.size) {
            player.seekTo(startIndex, 0L)
            _currentIndex.value = startIndex
            _currentTrack.value = tracks[startIndex]
        }
        player.prepare()
    }

    override fun addTracks(tracks: List<Track>) {
        log.d { "addTracks(${tracks.size})" }
        ensurePlayer()
        val player = exoPlayer ?: return

        val mediaItems = tracks.map { createMediaItem(it) }

        val currentQueue = _queue.value.toMutableList()
        val wasEmpty = currentQueue.isEmpty()
        currentQueue.addAll(tracks)
        _queue.value = currentQueue

        player.addMediaItems(mediaItems)

        if (wasEmpty && currentQueue.isNotEmpty()) {
            log.d { "queue was empty — starting playback at index 0" }
            player.seekTo(0, 0L)
            _currentIndex.value = 0
            _currentTrack.value = tracks[0]
            player.prepare()
            player.play()
        }
    }

    override fun insertTracksNext(tracks: List<Track>) {
        log.d { "insertTracksNext(${tracks.size})" }
        ensurePlayer()
        val player = exoPlayer ?: return

        val currentQueue = _queue.value.toMutableList()
        val wasEmpty = currentQueue.isEmpty()
        val currentIndex = player.currentMediaItemIndex
        val insertIndex = if (currentIndex >= 0) currentIndex + 1 else 0

        val mediaItems = tracks.map { createMediaItem(it) }

        if (insertIndex in 0..currentQueue.size) {
            currentQueue.addAll(insertIndex, tracks)
        } else {
            currentQueue.addAll(tracks)
        }
        _queue.value = currentQueue

        player.addMediaItems(insertIndex, mediaItems)

        if (wasEmpty && currentQueue.isNotEmpty()) {
            log.d { "queue was empty — starting playback at index 0" }
            player.seekTo(0, 0L)
            _currentIndex.value = 0
            _currentTrack.value = tracks[0]
            player.prepare()
            player.play()
        }
    }

    override fun play() {
        log.d { "play()" }
        ensurePlayer()
        exoPlayer?.play()
    }

    override fun pause() {
        log.d { "pause()" }
        exoPlayer?.pause()
    }

    override fun stop() {
        log.d { "stop()" }
        exoPlayer?.stop()
        _playbackState.value = PlaybackState.STOPPED
    }

    override fun seekTo(positionMs: Long) {
        log.d { "seekTo(${positionMs}ms)" }
        exoPlayer?.seekTo(positionMs)
    }

    override fun setVolume(level: Float) {
        log.d { "setVolume($level)" }
        _volume.value = level
        exoPlayer?.volume = level
    }

    override fun setShuffleMode(mode: ShuffleMode) {
        log.d { "setShuffleMode($mode)" }
        _shuffleMode.value = mode
        exoPlayer?.shuffleModeEnabled = (mode == ShuffleMode.ON)
    }

    override fun setRepeatMode(mode: RepeatMode) {
        log.d { "setRepeatMode($mode)" }
        _repeatMode.value = mode
        exoPlayer?.repeatMode = when (mode) {
            RepeatMode.OFF -> Player.REPEAT_MODE_OFF
            RepeatMode.TRACK -> Player.REPEAT_MODE_ONE
            RepeatMode.PLAYLIST -> Player.REPEAT_MODE_ALL
        }
    }

    override fun playNext() {
        val player = exoPlayer ?: return
        log.d { "playNext() hasNext=${player.hasNextMediaItem()}" }
        if (player.hasNextMediaItem()) {
            player.seekToNext()
        }
    }

    override fun playPrevious() {
        val player = exoPlayer ?: return
        log.d { "playPrevious() hasPrev=${player.hasPreviousMediaItem()}" }
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
        log.i { "release()" }
        exoPlayer?.removeListener(playerListener)
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun getQueue(): List<Track> = _queue.value

    override fun getQueueSize(): Int = _queue.value.size

    override fun playByIndex(index: Int) {
        log.d { "playByIndex($index)" }
        val player = exoPlayer ?: return
        if (index >= 0 && index < player.mediaItemCount) {
            player.seekTo(index, 0L)
            player.play()
        }
    }

    override fun removeTrack(index: Int) {
        log.d { "removeTrack($index)" }
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
        log.d { "moveTrack($from → $to)" }
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
        log.d { "clearQueue()" }
        val player = exoPlayer ?: return
        player.clearMediaItems()
        _queue.value = emptyList()
        _currentTrack.value = null
        _currentIndex.value = -1
    }
}
