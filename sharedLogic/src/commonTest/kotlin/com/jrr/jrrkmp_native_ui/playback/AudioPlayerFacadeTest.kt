package com.jrr.jrrkmp_native_ui.playback

import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AudioPlayerFacadeTest {

    private fun createMockTrack(
        fileKey: String,
        name: String,
        artist: String,
        album: String,
        durationMs: Long
    ) = Track(
        fileKey = fileKey,
        name = name,
        artist = artist,
        album = album,
        albumArtist = artist,
        date = "2026",
        genre = "Rock",
        durationMs = durationMs,
        trackNumber = 1,
        discNumber = 1,
        totalDiscs = 1,
        totalTracks = 1,
        bitrate = 320,
        bitDepth = 16,
        sampleRate = 44100,
        channels = 2,
        fileType = "mp3",
        filePath = "/path/to/$fileKey.mp3",
        folderPath = "/path/to"
    )

    private class MockLocalPlayerEngine(private val testClass: AudioPlayerFacadeTest) : LocalPlayerEngine {
        override val playbackState = MutableStateFlow(PlaybackState.STOPPED)
        val currentTrack = MutableStateFlow<Track?>(null)
        override val currentIndex = MutableStateFlow(-1)
        override val volume = MutableStateFlow(1.0f)
        override val shuffleMode = MutableStateFlow(ShuffleMode.OFF)
        override val repeatMode = MutableStateFlow(RepeatMode.OFF)
        override val queue = MutableStateFlow<List<Track>>(emptyList())

        private val queueList = mutableListOf<Track>()
        var playCalled = false
        var pauseCalled = false
        var stopCalled = false
        var nextCalled = false
        var prevCalled = false
        var seekPosition: Long = -1L
        var setVolumeVal: Float = -1.0f
        var playByIndexVal: Int = -1

        override fun getCurrentPosition(): Long = 1000L
        override fun getDuration(): Long = 200000L

        override fun setQueue(tracks: List<Track>, startIndex: Int) {
            queueList.clear()
            queueList.addAll(tracks)
            queue.value = tracks
            currentIndex.value = startIndex
            currentTrack.value = if (startIndex in tracks.indices) tracks[startIndex] else null
        }

        override fun play() {
            playCalled = true
            playbackState.value = PlaybackState.PLAYING
        }

        override fun pause() {
            pauseCalled = true
            playbackState.value = PlaybackState.PAUSED
        }

        override fun stop() {
            stopCalled = true
            playbackState.value = PlaybackState.STOPPED
        }

        override fun playNext() {
            nextCalled = true
        }

        override fun playPrevious() {
            prevCalled = true
        }

        override fun seekTo(positionMs: Long) {
            seekPosition = positionMs
        }

        override fun setVolume(level: Float) {
            setVolumeVal = level
            volume.value = level
        }

        override fun setShuffleMode(mode: ShuffleMode) {
            shuffleMode.value = mode
        }

        override fun setRepeatMode(mode: RepeatMode) {
            repeatMode.value = mode
        }

        override fun removeTrack(index: Int) {
            if (index in queueList.indices) {
                queueList.removeAt(index)
                queue.value = queueList.toList()
            }
        }

        override fun moveTrack(from: Int, to: Int) {
            if (from in queueList.indices && to in queueList.indices) {
                val item = queueList.removeAt(from)
                queueList.add(to, item)
                queue.value = queueList.toList()
            }
        }

        override fun clearQueue() {
            queueList.clear()
            queue.value = emptyList()
            currentIndex.value = -1
            currentTrack.value = null
        }

        override fun getQueue(): List<Track> = queueList
        override fun getQueueSize(): Int = queueList.size

        override fun playByIndex(index: Int) {
            playByIndexVal = index
        }
    }

    @Test
    fun testAudioPlayerFacade_localPlaybackControl() {
        val mockEngine = MockLocalPlayerEngine(this)
        var savedZoneId: String? = null
        val facade = AudioPlayerFacade(
            database = null,
            localPlayerEngine = mockEngine,
            saveLastActiveZoneId = { savedZoneId = it },
            loadLastActiveZoneId = { Zone.Local.id },
            mainDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        )

        // Verify default zone setup from settings loader callback
        assertEquals(Zone.Local, facade.activeZone.value)

        // Set queue and play
        val tracks = listOf(
            createMockTrack("key1", "Track 1", "Artist 1", "Album 1", 120000L),
            createMockTrack("key2", "Track 2", "Artist 2", "Album 2", 180000L)
        )
        facade.setQueue(tracks, 0)
        assertTrue(mockEngine.playCalled)
        assertEquals(2, mockEngine.getQueueSize())

        // Test basic playback controls routing
        facade.play()
        assertTrue(mockEngine.playCalled)

        facade.pause()
        assertTrue(mockEngine.pauseCalled)

        facade.stop()
        assertTrue(mockEngine.stopCalled)

        facade.next()
        assertTrue(mockEngine.nextCalled)

        facade.previous()
        assertTrue(mockEngine.prevCalled)

        facade.seekTo(5000L)
        assertEquals(5000L, mockEngine.seekPosition)

        facade.setVolume(0.5f)
        assertEquals(0.5f, mockEngine.setVolumeVal)

        facade.playByIndex(1)
        assertEquals(1, mockEngine.playByIndexVal)

        facade.removeQueueTrack(0)
        assertEquals(1, mockEngine.getQueueSize())

        facade.clearQueue()
        assertEquals(0, mockEngine.getQueueSize())
    }
}
