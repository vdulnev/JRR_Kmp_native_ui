package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LibraryRepositoryTest {

    @Test
    fun testParseTracksJson_success() {
        val json = """
        [
            {
                "Key": "12345",
                "Name": "Track 1",
                "Artist": "Artist A",
                "Album": "Album X",
                "Album Artist (auto)": "Artist A",
                "Genre": "Rock",
                "Duration": 240.5,
                "Track #": 3,
                "Disc #": 1,
                "Total Discs": 1,
                "Total Tracks": 12,
                "Bitrate": 320,
                "Bit depth": 16,
                "Sample Rate": 44100,
                "Channels": 2,
                "File Type": "mp3",
                "Filename": "/path/to/file.mp3"
            },
            {
                "Key": "67890",
                "Name": "Track 2",
                "Artist": "Artist B",
                "Album": "Album Y",
                "Duration": "180.0",
                "Track #": "4",
                "Filename": "/path/to/file2.mp3"
            }
        ]
        """.trimIndent()

        val tracks = McwsClient.parseTracksJson(json)

        assertEquals(2, tracks.size)

        val track1 = tracks[0]
        assertEquals("12345", track1.fileKey)
        assertEquals("Track 1", track1.name)
        assertEquals("Artist A", track1.artist)
        assertEquals("Album X", track1.album)
        assertEquals("Rock", track1.genre)
        assertEquals(240500L, track1.durationMs)
        assertEquals(3, track1.trackNumber)
        assertEquals(16, track1.bitDepth)
        assertEquals("/path/to/file.mp3", track1.filePath)

        val track2 = tracks[1]
        assertEquals("67890", track2.fileKey)
        assertEquals("Track 2", track2.name)
        assertEquals("Artist B", track2.artist)
        assertEquals("Album Y", track2.album)
        assertEquals("Unknown", track2.genre) // Default fallback
        assertEquals(180000L, track2.durationMs) // String to double to long parsing
        assertEquals(4, track2.trackNumber) // String to int parsing
    }

    @Test
    fun testParseTracksJson_emptyOrInvalid() {
        assertTrue(McwsClient.parseTracksJson(null).isEmpty())
        assertTrue(McwsClient.parseTracksJson("").isEmpty())
        assertTrue(McwsClient.parseTracksJson("[]").isEmpty())
        assertTrue(McwsClient.parseTracksJson("invalid-json").isEmpty())
    }
}

