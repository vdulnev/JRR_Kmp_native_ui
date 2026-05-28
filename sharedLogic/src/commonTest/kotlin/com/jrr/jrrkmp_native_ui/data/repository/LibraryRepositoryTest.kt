package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.data.api.parseMcwsTracksJson
import com.jrr.jrrkmp_native_ui.domain.model.Album
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LibraryRepositoryTest {

    // ---- groupAlbumsByGroupId ----------------------------------------------

    private fun makeAlbum(
        name: String,
        folderPath: String,
        parentFolderPath: String = "",
        totalDiscs: Int = 1,
        discNumber: Int = 1,
        artworkFileKey: String = "${name}-key",
    ) = Album(
        name = name,
        albumArtist = "Pink Floyd",
        folderPath = folderPath,
        parentFolderPath = parentFolderPath,
        date = "1979",
        artworkFileKey = artworkFileKey,
        totalDiscs = totalDiscs,
        discNumber = discNumber,
    )

    @Test
    fun groupAlbums_singleDiscAlbumsPassThrough() {
        val a = makeAlbum("Wish You Were Here", "/m/pf/wywh/")
        val b = makeAlbum("The Wall", "/m/pf/wall/")

        val result = groupAlbumsByGroupId(listOf(a, b))

        assertEquals(2, result.size)
        assertTrue(a in result)
        assertTrue(b in result)
    }

    @Test
    fun groupAlbums_multiDiscFoldsToOneRepresentative() {
        val disc1 = makeAlbum(
            name = "The Wall",
            folderPath = "/m/pf/wall/disc 1/",
            parentFolderPath = "/m/pf/wall/",
            totalDiscs = 2,
            discNumber = 1,
            artworkFileKey = "wall-d1",
        )
        val disc2 = makeAlbum(
            name = "The Wall",
            folderPath = "/m/pf/wall/disc 2/",
            parentFolderPath = "/m/pf/wall/",
            totalDiscs = 2,
            discNumber = 2,
            artworkFileKey = "wall-d2",
        )

        val result = groupAlbumsByGroupId(listOf(disc2, disc1))

        assertEquals(1, result.size)
        val rep = result.single()
        assertEquals("The Wall", rep.name)
        // Disc 1 must win — its folderPath + artwork become the representative
        // so AlbumDetail shows Disc 1's art.
        assertEquals(1, rep.discNumber)
        assertEquals("wall-d1", rep.artworkFileKey)
        // And totalDiscs is preserved so getAlbumTracks knows to fetch both
        // discs via parentFolderPath.
        assertEquals(2, rep.totalDiscs)
    }

    @Test
    fun groupAlbums_untaggedDiscNumberStaysSeparate() {
        // Defensive: Album.albumGroupId requires discNumber > 0 to opt into
        // the multi-disc grouping path. A track with totalDiscs=2 but
        // discNumber=0 is treated as its own group because we can't trust
        // the disc metadata enough to fold it into the album.
        //
        // This locks down current behaviour. If we ever want to be more
        // lenient (group by parentFolderPath whenever totalDiscs > 1
        // regardless of discNumber), update Album.albumGroupId AND this test.
        val disc1 = makeAlbum(
            name = "The Wall",
            folderPath = "/m/pf/wall/disc 1/",
            parentFolderPath = "/m/pf/wall/",
            totalDiscs = 2,
            discNumber = 1,
        )
        val untagged = makeAlbum(
            name = "The Wall",
            folderPath = "/m/pf/wall/disc 2/",
            parentFolderPath = "/m/pf/wall/",
            totalDiscs = 2,
            discNumber = 0,
        )

        val result = groupAlbumsByGroupId(listOf(untagged, disc1))

        assertEquals(2, result.size)
    }

    @Test
    fun groupAlbums_pickWinningDiscBySmallestDiscNumber() {
        // When multiple discs are correctly tagged (1, 2, 3, ...), the rep
        // should be the lowest-numbered one. Important so AlbumDetail picks
        // up Disc 1's artwork and folder path.
        val disc1 = makeAlbum(
            name = "Use Your Illusion",
            folderPath = "/m/gnr/uyi/i/",
            parentFolderPath = "/m/gnr/uyi/",
            totalDiscs = 2,
            discNumber = 1,
            artworkFileKey = "uyi-1",
        )
        val disc2 = makeAlbum(
            name = "Use Your Illusion",
            folderPath = "/m/gnr/uyi/ii/",
            parentFolderPath = "/m/gnr/uyi/",
            totalDiscs = 2,
            discNumber = 2,
            artworkFileKey = "uyi-2",
        )

        // Pass in reverse order to make sure we don't accidentally rely on
        // input ordering.
        val result = groupAlbumsByGroupId(listOf(disc2, disc1))

        assertEquals("uyi-1", result.single().artworkFileKey)
    }

    @Test
    fun groupAlbums_differentParentFoldersStaySeparate() {
        // Same album name, but two genuinely different physical albums.
        val a = makeAlbum(
            name = "Greatest Hits",
            folderPath = "/m/abba/gh/",
            totalDiscs = 1,
            discNumber = 1,
        )
        val b = makeAlbum(
            name = "Greatest Hits",
            folderPath = "/m/queen/gh/",
            totalDiscs = 1,
            discNumber = 1,
        )

        val result = groupAlbumsByGroupId(listOf(a, b))

        assertEquals(2, result.size, "same name, different folders → distinct groups")
    }

    @Test
    fun groupAlbums_emptyListReturnsEmpty() {
        assertTrue(groupAlbumsByGroupId(emptyList()).isEmpty())
    }

    // ---- parseMcwsTracksJson -----------------------------------------------

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

        val tracks = parseMcwsTracksJson(json)

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
        assertTrue(parseMcwsTracksJson(null).isEmpty())
        assertTrue(parseMcwsTracksJson("").isEmpty())
        assertTrue(parseMcwsTracksJson("[]").isEmpty())
        assertTrue(parseMcwsTracksJson("invalid-json").isEmpty())
    }
}

