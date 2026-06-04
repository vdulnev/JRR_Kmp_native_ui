package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.domain.model.Track
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BrowseTrackGroupingTest {

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

        val groups = groupTracksByArtistAndAlbum(tracks)

        assertEquals(listOf("Apple", "Zebra"), groups.map { it.artist })
        val zebra = groups.first { it.artist == "Zebra" }
        assertEquals(1, zebra.albums.size)
        assertEquals("Beta", zebra.albums.first().name)
        assertEquals(listOf("1", "3"), zebra.albums.first().tracks.map { it.fileKey })
    }

    @Test
    fun emptyArtistFallsBackToUnknown() {
        val groups = groupTracksByArtistAndAlbum(
            listOf(track("1", "song", album = "X", albumArtist = "", folderPath = "/m/x")),
        )

        assertEquals("Unknown Artist", groups.single().artist)
        assertEquals("X", groups.single().albums.single().name)
    }

    @Test
    fun sameAlbumNameInDifferentFoldersStaysSeparate() {
        val tracks = listOf(
            track("1", "song", album = "Greatest Hits", albumArtist = "VA", folderPath = "/m/a/hits"),
            track("2", "song", album = "Greatest Hits", albumArtist = "VA", folderPath = "/m/b/hits"),
        )

        val albums = groupTracksByArtistAndAlbum(tracks).single().albums
        assertEquals(2, albums.size)
        assertTrue(albums.all { it.name == "Greatest Hits" })
    }

    @Test
    fun mergesDiscMarkerAlbumTagsIntoOneNormalisedGroup() {
        // Regression: the per-disc rows of a 2-CD set carry disc-suffixed album
        // tags ("KuschelRock 11 [CD1]" / "[CD2]") in sibling subfolders under a
        // shared parent. The naive albumGroupId keyed on the raw tag and split
        // them; the shared computeGroupKey normalises the name and folds on the
        // parent folder, so they must collapse into one "KuschelRock 11" group.
        val parent = "/m/KuschelRock 11 [2CD] (1997)"
        val tracks = listOf(
            track("d1t1", "I Don't Want To", album = "KuschelRock 11 [CD1]",
                albumArtist = "(Multiple Artists)", folderPath = "$parent/CD1", trackNumber = 1),
            track("d1t2", "Quit Playing Games", album = "KuschelRock 11 [CD1]",
                albumArtist = "(Multiple Artists)", folderPath = "$parent/CD1", trackNumber = 2),
            track("d2t1", "Truly Madly Deeply", album = "KuschelRock 11 [CD2]",
                albumArtist = "(Multiple Artists)", folderPath = "$parent/CD2", trackNumber = 1),
        )

        val albums = groupTracksByArtistAndAlbum(tracks).single().albums
        assertEquals(1, albums.size, "the two discs must fold into one album group")
        assertEquals("KuschelRock 11", albums.single().name)
        assertEquals(3, albums.single().tracks.size)
        // Disc numbers recovered from the CD1/CD2 subfolders, ordered disc/track.
        assertEquals(
            listOf("d1t1", "d1t2", "d2t1"),
            albums.single().tracks.map { it.fileKey },
        )
    }
}
