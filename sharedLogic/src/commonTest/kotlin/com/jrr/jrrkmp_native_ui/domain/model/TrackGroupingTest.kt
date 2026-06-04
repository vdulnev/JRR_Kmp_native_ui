package com.jrr.jrrkmp_native_ui.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrackGroupingTest {

    private fun track(
        fileKey: String,
        name: String,
        album: String,
        albumArtist: String,
        folderPath: String,
        trackNumber: Int = 1,
        discNumber: Int = 1,
        totalDiscs: Int = 1,
    ) = Track(
        fileKey = fileKey,
        name = name,
        artist = albumArtist,
        album = album,
        albumArtist = albumArtist,
        date = "2026",
        genre = "Rock",
        durationMs = 1000,
        trackNumber = trackNumber,
        discNumber = discNumber,
        totalDiscs = totalDiscs,
        totalTracks = 10,
        bitrate = 320,
        bitDepth = 16,
        sampleRate = 44100,
        channels = 2,
        fileType = "mp3",
        filePath = "$folderPath/$fileKey.mp3",
        folderPath = folderPath,
    )

    @Test
    fun groupsByArtistThenAlbum() {
        val tracks = listOf(
            track("1", "B-song", album = "Beta", albumArtist = "Zebra", folderPath = "/m/zebra/beta"),
            track("2", "A-song", album = "Alpha", albumArtist = "Apple", folderPath = "/m/apple/alpha"),
            track("3", "C-song", album = "Beta", albumArtist = "Zebra", folderPath = "/m/zebra/beta", trackNumber = 2),
        )

        val groups = tracks.groupByArtistAndAlbum()

        // Artists sorted case-insensitively: Apple before Zebra.
        assertEquals(listOf("Apple", "Zebra"), groups.map { it.artist })
        val zebra = groups.first { it.artist == "Zebra" }
        assertEquals(1, zebra.albums.size)
        assertEquals("Beta", zebra.albums.first().name)
        assertEquals(listOf("1", "3"), zebra.albums.first().tracks.map { it.fileKey })
    }

    @Test
    fun emptyArtistFallsBackToUnknown() {
        val groups = listOf(
            track("1", "song", album = "X", albumArtist = "", folderPath = "/m/x"),
        ).groupByArtistAndAlbum()

        assertEquals("Unknown Artist", groups.single().artist)
        assertEquals("X", groups.single().albums.single().name)
    }

    @Test
    fun mergesMultiDiscAlbumIntoOneGroupOrderedByDiscThenTrack() {
        // Two discs of the same album live in sibling folders under a shared
        // parent; albumGroupId (totalDiscs > 1) folds them into one album.
        val tracks = listOf(
            track("d2t1", "Disc2Track1", album = "Opus", albumArtist = "Composer",
                folderPath = "/m/opus/disc2", trackNumber = 1, discNumber = 2, totalDiscs = 2),
            track("d1t2", "Disc1Track2", album = "Opus", albumArtist = "Composer",
                folderPath = "/m/opus/disc1", trackNumber = 2, discNumber = 1, totalDiscs = 2),
            track("d1t1", "Disc1Track1", album = "Opus", albumArtist = "Composer",
                folderPath = "/m/opus/disc1", trackNumber = 1, discNumber = 1, totalDiscs = 2),
        )

        val albums = tracks.groupByArtistAndAlbum().single().albums
        assertEquals(1, albums.size, "multi-disc album should be a single group")
        assertEquals(
            listOf("d1t1", "d1t2", "d2t1"),
            albums.single().tracks.map { it.fileKey },
            "tracks ordered by disc then track number",
        )
    }

    @Test
    fun sameAlbumNameInDifferentFoldersStaysSeparate() {
        val tracks = listOf(
            track("1", "song", album = "Greatest Hits", albumArtist = "VA", folderPath = "/m/a/hits"),
            track("2", "song", album = "Greatest Hits", albumArtist = "VA", folderPath = "/m/b/hits"),
        )

        val albums = tracks.groupByArtistAndAlbum().single().albums
        assertEquals(2, albums.size)
        assertTrue(albums.all { it.name == "Greatest Hits" })
    }
}
