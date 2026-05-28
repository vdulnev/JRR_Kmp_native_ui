package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.api.createMcwsHttpClient
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.playback.LocalPlayerEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelShuffleTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

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

    private class MockLocalPlayerEngine : LocalPlayerEngine {
        override val playbackState = MutableStateFlow(PlaybackState.STOPPED)
        override val currentIndex = MutableStateFlow(-1)
        override val volume = MutableStateFlow(1.0f)
        override val shuffleMode = MutableStateFlow(ShuffleMode.OFF)
        override val repeatMode = MutableStateFlow(RepeatMode.OFF)
        override val queue = MutableStateFlow<List<Track>>(emptyList())

        private val queueList = mutableListOf<Track>()
        var playCalled = false
        var setQueueCalled = false
        var lastSetQueue: List<Track> = emptyList()

        override fun getCurrentPosition(): Long = 0L
        override fun getDuration(): Long = 0L

        override fun setQueue(tracks: List<Track>, startIndex: Int) {
            setQueueCalled = true
            lastSetQueue = tracks
            queueList.clear()
            queueList.addAll(tracks)
            queue.value = tracks
            currentIndex.value = startIndex
        }

        override fun play() {
            playCalled = true
            playbackState.value = PlaybackState.PLAYING
        }

        override fun pause() {}
        override fun stop() {}
        override fun playNext() {}
        override fun playPrevious() {}
        override fun seekTo(positionMs: Long) {}
        override fun setVolume(level: Float) {}
        override fun setShuffleMode(mode: ShuffleMode) {
            shuffleMode.value = mode
        }
        override fun setRepeatMode(mode: RepeatMode) {}
        override fun removeTrack(index: Int) {}
        override fun moveTrack(from: Int, to: Int) {}
        override fun clearQueue() {}
        override fun getQueue(): List<Track> = queueList
        override fun getQueueSize(): Int = queueList.size
        override fun playByIndex(index: Int) {}
        override fun addTracks(tracks: List<Track>) {}
        override fun insertTracksNext(tracks: List<Track>) {}
    }

    @Test
    fun testLibraryViewModel_playTracksShuffled_shufflesAllTracks() {
        val mockEngine = MockLocalPlayerEngine()
        val mcwsClient = McwsClient(
            httpClient = createMcwsHttpClient(),
            activeServerFlow = MutableStateFlow(null),
        )
        val facade = AudioPlayerFacade(
            database = null,
            localPlayerEngine = mockEngine,
            mcwsClient = mcwsClient,
            mainDispatcher = kotlinx.coroutines.Dispatchers.Unconfined,
            ioDispatcher = kotlinx.coroutines.Dispatchers.Unconfined
        )
        val repository = LibraryRepository(
            database = null,
            mcwsClient = mcwsClient,
            isOfflineProvider = { true }
        )
        val viewModel = LibraryViewModel(repository, facade)

        val tracks = (1..10).map {
            createMockTrack("key$it", "Track $it", "Artist", "Album", 1000L * it)
        }

        viewModel.playTracksShuffled(tracks)

        assertTrue(mockEngine.setQueueCalled)
        assertTrue(mockEngine.playCalled)
        assertEquals(10, mockEngine.lastSetQueue.size)
        assertEquals(tracks.map { it.fileKey }.toSet(), mockEngine.lastSetQueue.map { it.fileKey }.toSet())
        assertNotEquals(tracks, mockEngine.lastSetQueue)
    }
}
