package com.jrr.jrrkmp_native_ui.presentation.viewmodel

import com.jrr.jrrkmp_native_ui.domain.model.Track
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Covers [overlayPlayCounts] — the VM-layer merge that folds live server play
 * counts into a track list so the played icon reflects the authoritative
 * `[Number Plays]` while all screen state stays on the ViewModel's StateFlow.
 */
class PlayCountOverlayTest {

    private fun track(fileKey: String, numberPlays: Int) = Track(
        fileKey = fileKey,
        name = "Track $fileKey",
        artist = "Artist",
        album = "Album",
        albumArtist = "Artist",
        date = "2026",
        genre = "Rock",
        durationMs = 1000L,
        trackNumber = 1,
        discNumber = 1,
        totalDiscs = 1,
        totalTracks = 1,
        bitrate = 320,
        bitDepth = 16,
        sampleRate = 44100,
        channels = 2,
        fileType = "mp3",
        filePath = "/p/$fileKey.mp3",
        folderPath = "/p",
        numberPlays = numberPlays,
    )

    @Test
    fun patchesNumberPlaysForMatchingKeys() {
        val tracks = listOf(track("a", 0), track("b", 2))

        val result = tracks.overlayPlayCounts(mapOf("a" to 3))

        assertEquals(3, result[0].numberPlays)
        assertEquals(2, result[1].numberPlays, "unmatched track keeps its baked count")
    }

    @Test
    fun returnsSameInstanceWhenOverlayIsEmpty() {
        val tracks = listOf(track("a", 1))

        assertSame(tracks, tracks.overlayPlayCounts(emptyMap()))
    }

    @Test
    fun returnsSameInstanceWhenNoCountChanged() {
        val tracks = listOf(track("a", 5))

        // Count present but identical to the baked value — nothing to patch.
        assertSame(tracks, tracks.overlayPlayCounts(mapOf("a" to 5)))
    }
}
