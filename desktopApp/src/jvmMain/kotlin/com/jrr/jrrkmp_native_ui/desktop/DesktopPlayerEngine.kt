package com.jrr.jrrkmp_native_ui.desktop

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.domain.model.LocalAudioQuality
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.playback.LocalPlayerEngine
import com.sun.jna.NativeLibrary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.component.AudioPlayerComponent
import java.io.File
import java.util.concurrent.Executors
import kotlin.random.Random

/**
 * Desktop [LocalPlayerEngine] backed by **VLCJ** (libvlc). This is the JVM
 * analogue of the Android [com.jrr.jrrkmp_native_ui.playback.LocalPlayerHandler]
 * (ExoPlayer): it streams the MCWS `File/GetFile` endpoint for the active
 * server, honouring the user's [LocalAudioQuality], and plays it on the local
 * audio device.
 *
 * Queue/index/repeat/shuffle bookkeeping lives here (libvlc's own media-list
 * player is deliberately not used so the model matches ExoPlayer's single-player
 * + app-managed queue). On natural track end, [onTrackFinished] advances the
 * index according to repeat/shuffle.
 *
 * Requires a system VLC install — libvlc is discovered at runtime by VLCJ. If it
 * is missing, [component] is null and the engine degrades to a no-op (remote
 * MCWS zone control is unaffected); a single Error is logged explaining the fix.
 *
 * Threading: libvlc fires events on a native callback thread, and re-entering
 * the player from that thread can deadlock. State writes (plain `StateFlow`
 * assignments) are safe, but anything that drives the player — i.e. advancing to
 * the next track — is bounced onto [advanceExecutor].
 */
class DesktopPlayerEngine(
    private val serverRepository: ServerRepository,
    private val localAudioQualityProvider: () -> LocalAudioQuality = { LocalAudioQuality.LOSSLESS },
) : LocalPlayerEngine {
    private val log = Logger.withTag("playback:Local")

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

    private val advanceExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "vlcj-advance").apply { isDaemon = true }
    }

    private val eventListener = object : MediaPlayerEventAdapter() {
        override fun playing(mediaPlayer: MediaPlayer) {
            // libvlc resets the audio volume per-media; reapply our level.
            mediaPlayer.audio().setVolume((_volume.value * 100).toInt().coerceIn(0, 100))
            _playbackState.value = PlaybackState.PLAYING
        }

        override fun paused(mediaPlayer: MediaPlayer) {
            _playbackState.value = PlaybackState.PAUSED
        }

        override fun stopped(mediaPlayer: MediaPlayer) {
            // Emitted on explicit stop and at end-of-media; finished() handles the
            // natural-end advance, so only reflect a true stop here.
            if (_playbackState.value != PlaybackState.PLAYING) {
                _playbackState.value = PlaybackState.STOPPED
            }
        }

        override fun finished(mediaPlayer: MediaPlayer) {
            advanceExecutor.execute { onTrackFinished() }
        }

        override fun error(mediaPlayer: MediaPlayer) {
            log.e { "vlcj playback error on index ${_currentIndex.value}" }
        }
    }

    /**
     * Lazily constructed so a missing libvlc only surfaces (once, as an Error)
     * when playback is first attempted — the app and remote control still work.
     * Held as an explicit [Lazy] so [release] can avoid forcing initialisation
     * (closing the app should not spin up libvlc just to tear it down).
     */
    private val componentLazy: Lazy<AudioPlayerComponent?> = lazy { tryCreateComponent() }
    private val component: AudioPlayerComponent? get() = componentLazy.value
    private val mediaPlayer: MediaPlayer? get() = component?.mediaPlayer()

    private fun tryCreateComponent(): AudioPlayerComponent? = try {
        configureBundledNatives()
        AudioPlayerComponent().also {
            it.mediaPlayer().events().addMediaPlayerEventListener(eventListener)
            log.i { "VLCJ audio player initialised" }
        }
    } catch (t: Throwable) {
        log.e(t) {
            "libvlc not found — local playback disabled. Install VLC (64-bit) or " +
                "build with bundled natives (syncVlcNatives). Remote zone control is unaffected."
        }
        null
    }

    /**
     * Point VLCJ/JNA at the libvlc natives bundled into the app image (staged by
     * the `syncVlcNatives` Gradle task under `<resources>/vlc/`). When running
     * unpackaged (no `compose.application.resources.dir`, or natives not staged),
     * this is a no-op and VLCJ falls back to discovering a system VLC install.
     * libvlc locates its `plugins/` relative to the DLL on Windows, so no plugin
     * path needs to be set explicitly.
     */
    private fun configureBundledNatives() {
        val resourcesDir = System.getProperty("compose.application.resources.dir") ?: return
        val vlcDir = File(resourcesDir, "vlc")
        if (File(vlcDir, "libvlc.dll").exists()) {
            log.i { "using bundled libvlc at ${vlcDir.absolutePath}" }
            NativeLibrary.addSearchPath("libvlc", vlcDir.absolutePath)
            NativeLibrary.addSearchPath("libvlccore", vlcDir.absolutePath)
            System.setProperty("jna.library.path", vlcDir.absolutePath)
        }
    }

    // --- Stream URL ---------------------------------------------------------

    /**
     * Builds the MCWS streaming URL for [track] against the active server,
     * mirroring the Android handler. Returns null when no server is connected
     * (offline) — there is nothing to stream from.
     */
    private fun streamUrl(track: Track): String? {
        val active = serverRepository.activeServer.value ?: run {
            log.w { "streamUrl: no active server — cannot stream ${track.fileKey}" }
            return null
        }
        val scheme = if (active.useSsl) "https" else "http"
        val port = if (active.useSsl) active.sslPort else active.port
        val base = "$scheme://${active.host}:$port/MCWS/v1"
        val token = active.token ?: ""
        val quality = localAudioQualityProvider()
        return "$base/File/GetFile?File=${track.fileKey}&FileType=Key&Playback=1&" +
            "${quality.mcwsParams}&Token=$token"
    }

    private fun loadAndPlay(index: Int) {
        val q = _queue.value
        if (index !in q.indices) {
            log.w { "loadAndPlay($index) out of bounds (size=${q.size})" }
            return
        }
        _currentIndex.value = index
        val url = streamUrl(q[index]) ?: return
        val player = mediaPlayer ?: return
        log.d { "loadAndPlay(index=$index) ${q[index].name}" }
        // media().play(mrl) parses and starts playback asynchronously.
        player.media().play(url)
    }

    private fun onTrackFinished() {
        val q = _queue.value
        val idx = _currentIndex.value
        log.d { "onTrackFinished(index=$idx, repeat=${_repeatMode.value}, shuffle=${_shuffleMode.value})" }
        when {
            _repeatMode.value == RepeatMode.TRACK -> loadAndPlay(idx)
            _shuffleMode.value == ShuffleMode.ON && q.size > 1 -> {
                var next = Random.nextInt(q.size)
                if (next == idx) next = (idx + 1) % q.size
                loadAndPlay(next)
            }
            idx < q.lastIndex -> loadAndPlay(idx + 1)
            _repeatMode.value == RepeatMode.PLAYLIST && q.isNotEmpty() -> loadAndPlay(0)
            else -> _playbackState.value = PlaybackState.STOPPED
        }
    }

    // --- Position / duration ------------------------------------------------

    // Guard on isInitialized so the NowPlaying VM's position poll doesn't force
    // libvlc to load for users who only ever do remote (MCWS zone) control.
    override fun getCurrentPosition(): Long =
        if (componentLazy.isInitialized()) (mediaPlayer?.status()?.time() ?: 0L).coerceAtLeast(0L) else 0L

    override fun getDuration(): Long =
        if (componentLazy.isInitialized()) (mediaPlayer?.status()?.length() ?: 0L).coerceAtLeast(0L) else 0L

    // --- Queue control ------------------------------------------------------

    override fun setQueue(tracks: List<Track>, startIndex: Int) {
        log.d { "setQueue(${tracks.size}, start=$startIndex)" }
        _queue.value = tracks
        if (tracks.isEmpty()) {
            _currentIndex.value = -1
            if (componentLazy.isInitialized()) mediaPlayer?.controls()?.stop()
            _playbackState.value = PlaybackState.STOPPED
            return
        }
        loadAndPlay(startIndex.coerceIn(0, tracks.lastIndex))
    }

    override fun addTracks(tracks: List<Track>) {
        log.d { "addTracks(${tracks.size})" }
        val wasEmpty = _queue.value.isEmpty()
        _queue.value = _queue.value + tracks
        if (wasEmpty && _queue.value.isNotEmpty()) loadAndPlay(0)
    }

    override fun insertTracksNext(tracks: List<Track>) {
        log.d { "insertTracksNext(${tracks.size})" }
        val list = _queue.value.toMutableList()
        val wasEmpty = list.isEmpty()
        val at = (_currentIndex.value + 1).coerceIn(0, list.size)
        list.addAll(at, tracks)
        _queue.value = list
        if (wasEmpty && list.isNotEmpty()) loadAndPlay(0)
    }

    override fun play() {
        log.d { "play()" }
        val player = mediaPlayer ?: return
        // Resume if a media is loaded; otherwise start the current queue entry.
        if (_currentIndex.value in _queue.value.indices && _playbackState.value == PlaybackState.PAUSED) {
            player.controls().play()
        } else if (_currentIndex.value in _queue.value.indices) {
            loadAndPlay(_currentIndex.value)
        }
    }

    override fun pause() {
        log.d { "pause()" }
        // Nothing to pause if libvlc was never started.
        if (componentLazy.isInitialized()) mediaPlayer?.controls()?.setPause(true)
    }

    override fun stop() {
        log.d { "stop()" }
        if (componentLazy.isInitialized()) mediaPlayer?.controls()?.stop()
        _playbackState.value = PlaybackState.STOPPED
    }

    override fun playNext() {
        val q = _queue.value
        val idx = _currentIndex.value
        if (idx < q.lastIndex) loadAndPlay(idx + 1)
        else if (_repeatMode.value == RepeatMode.PLAYLIST && q.isNotEmpty()) loadAndPlay(0)
    }

    override fun playPrevious() {
        val idx = _currentIndex.value
        if (idx > 0) loadAndPlay(idx - 1)
    }

    override fun seekTo(positionMs: Long) {
        log.d { "seekTo(${positionMs}ms)" }
        mediaPlayer?.controls()?.setTime(positionMs.coerceAtLeast(0L))
    }

    override fun setVolume(level: Float) {
        val clamped = level.coerceIn(0f, 1f)
        _volume.value = clamped
        // Pushed to libvlc only if already running; otherwise the value is
        // reapplied from _volume in the `playing` event when playback starts.
        if (componentLazy.isInitialized()) {
            mediaPlayer?.audio()?.setVolume((clamped * 100).toInt().coerceIn(0, 100))
        }
    }

    override fun setShuffleMode(mode: ShuffleMode) { _shuffleMode.value = mode }
    override fun setRepeatMode(mode: RepeatMode) { _repeatMode.value = mode }

    override fun removeTrack(index: Int) {
        val list = _queue.value.toMutableList()
        if (index !in list.indices) return
        list.removeAt(index)
        _queue.value = list
        // Keep currentIndex pointing at the same logical track when a preceding
        // entry is removed; playback itself is not interrupted.
        if (index < _currentIndex.value) _currentIndex.value = _currentIndex.value - 1
    }

    override fun moveTrack(from: Int, to: Int) {
        val list = _queue.value.toMutableList()
        if (from !in list.indices || to !in list.indices) return
        list.add(to, list.removeAt(from))
        _queue.value = list
        // Track the currently-playing item across the move.
        val cur = _currentIndex.value
        _currentIndex.value = when (cur) {
            from -> to
            in (from + 1)..to -> cur - 1
            in to until from -> cur + 1
            else -> cur
        }
    }

    override fun clearQueue() {
        log.d { "clearQueue()" }
        if (componentLazy.isInitialized()) mediaPlayer?.controls()?.stop()
        _queue.value = emptyList()
        _currentIndex.value = -1
        _playbackState.value = PlaybackState.STOPPED
    }

    override fun getQueue(): List<Track> = _queue.value
    override fun getQueueSize(): Int = _queue.value.size
    override fun playByIndex(index: Int) {
        if (index in _queue.value.indices) loadAndPlay(index)
    }

    /**
     * Releases libvlc resources. Call from the window-close hook. Does nothing if
     * playback was never started (libvlc was never initialised).
     */
    fun release() {
        log.i { "release()" }
        if (componentLazy.isInitialized()) componentLazy.value?.release()
        advanceExecutor.shutdownNow()
    }
}
