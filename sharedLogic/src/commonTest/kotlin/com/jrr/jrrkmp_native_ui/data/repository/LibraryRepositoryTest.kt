package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.data.api.parseMcwsTracksJson
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LibraryRepositoryTest {

    // ---- normalizeAlbumName ------------------------------------------------

    @Test
    fun normalize_trailingParensDiscMarker() {
        assertEquals("100 Hits", normalizeAlbumName("100 Hits (Disc 1)"))
        assertEquals("100 Hits", normalizeAlbumName("100 Hits (Disc 5)"))
        assertEquals("101", normalizeAlbumName("101 (Disc A)"))
        assertEquals("101", normalizeAlbumName("101 (Disc B)"))
        assertEquals(
            "40 Years - Decades Of Decibels",
            normalizeAlbumName("40 Years - Decades Of Decibels (Disc1)"),
        )
    }

    @Test
    fun normalize_trailingParensCdMarker() {
        assertEquals("090909 Sampler", normalizeAlbumName("090909 Sampler (CD 1)"))
        assertEquals("090909 Sampler", normalizeAlbumName("090909 Sampler (CD 2)"))
        assertEquals("25", normalizeAlbumName("25 (CD1)"))
        assertEquals("25", normalizeAlbumName("25 (CD 3)"))
    }

    @Test
    fun normalize_hyphenatedCdMarker_keepsCatalogParens() {
        // `(CD-1)` is a disc marker (drop it); `(SRCS 7321~3)` is a catalog
        // number and must be preserved as a distinguishing release token.
        assertEquals(
            "Pandora's Box (SRCS 7321~3)",
            normalizeAlbumName("Pandora's Box (SRCS 7321~3) (CD-1)"),
        )
        assertEquals(
            "Pandora's Box (SRCS 7321~3)",
            normalizeAlbumName("Pandora's Box (SRCS 7321~3) (CD-3)"),
        )
        assertEquals("25", normalizeAlbumName("25 (CD-2)"))
        assertEquals("25", normalizeAlbumName("25 (CD.2)"))
        assertEquals("25", normalizeAlbumName("25 (Disc-2)"))
    }

    @Test
    fun normalize_embeddedInsideLargerParens() {
        assertEquals(
            "13 (Deluxe Edition, 3735427)",
            normalizeAlbumName("13 (Deluxe Edition, 3735427, CD1)"),
        )
        assertEquals(
            "13 (Deluxe Edition, 3735427)",
            normalizeAlbumName("13 (Deluxe Edition, 3735427, CD2)"),
        )
        assertEquals(
            "'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany)",
            normalizeAlbumName("'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany, CD 1)"),
        )
        assertEquals(
            "'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany)",
            normalizeAlbumName("'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany, CD 2)"),
        )
    }

    @Test
    fun normalize_midNameMarker() {
        assertEquals(
            "...To The Rising Sun. In Tokyo (2015, 2CD+DVD, 0210547EMU)",
            normalizeAlbumName("...To The Rising Sun. In Tokyo CD1 (2015, 2CD+DVD, 0210547EMU)"),
        )
        assertEquals(
            "...To The Rising Sun. In Tokyo (2015, 2CD+DVD, 0210547EMU)",
            normalizeAlbumName("...To The Rising Sun. In Tokyo CD2 (2015, 2CD+DVD, 0210547EMU)"),
        )
        assertEquals(
            "30: Very Best Of (1998, 2CD, 724349680821)",
            normalizeAlbumName("30: Very Best Of CD1 (1998, 2CD, 724349680821)"),
        )
    }

    @Test
    fun normalize_trailingBareMarkerNoParens() {
        assertEquals(
            "100,000,000 BON JOVI Fans...",
            normalizeAlbumName("100,000,000 BON JOVI Fans...CD01"),
        )
        assertEquals(
            "100,000,000 BON JOVI Fans...",
            normalizeAlbumName("100,000,000 BON JOVI Fans...CD04"),
        )
    }

    @Test
    fun normalize_preservesCatalogTokensWithoutTrailingDigit() {
        // "HNECD032" is a catalog number, not a disc marker — the "CD"
        // is followed by a digit but is glued into a longer token. We
        // rely on this being preserved.
        assertEquals(
            "1916 [HNECD032, 2014]",
            normalizeAlbumName("1916 [HNECD032, 2014]"),
        )
        // "...2CD..." inside parens: "2CD" has no preceding " " so doesn't
        // match the mid-name pattern, and no `\d+` after "CD" so doesn't
        // match the embedded pattern. Preserved as a release-format hint.
        assertEquals(
            "Greatest Hits (2CD, Japan)",
            normalizeAlbumName("Greatest Hits (2CD, Japan)"),
        )
    }

    @Test
    fun normalize_noMarkerReturnsUnchanged() {
        assertEquals("The Wall", normalizeAlbumName("The Wall"))
        assertEquals("Wish You Were Here", normalizeAlbumName("Wish You Were Here"))
        assertEquals("Animals", normalizeAlbumName("Animals"))
    }

    @Test
    fun normalize_discMarkerFollowedByMoreParensOrBrackets() {
        // Beatles box sets: (Disc N) followed by a catalog-number block.
        assertEquals(
            "1962 - 1966 (0602455920768)",
            normalizeAlbumName("1962 - 1966 (Disc 1) (0602455920768)"),
        )
        assertEquals(
            "1962 - 1966 (0602455920768)",
            normalizeAlbumName("1962 - 1966 (Disc 2) (0602455920768)"),
        )
        // Metallica reissue series: (Disc N) followed by square-bracket
        // catalog metadata.
        assertEquals(
            "Garage Inc. [SHM-CD, UICY-94670]",
            normalizeAlbumName("Garage Inc. (Disc 1) [SHM-CD, UICY-94670]"),
        )
        assertEquals(
            "Garage Inc. [Brazil Vertigo 538 351-2]",
            normalizeAlbumName("Garage Inc. (Disc 1) [Brazil Vertigo 538 351-2]"),
        )
        // Pink Floyd reissue: (Disc N) followed by edition tag in parens.
        assertEquals(
            "The Wall (EU Shine On)",
            normalizeAlbumName("The Wall (Disc 1) (EU Shine On)"),
        )
    }

    @Test
    fun normalize_writtenOutDiscNumbers() {
        assertEquals("Anthology", normalizeAlbumName("Anthology (Disc One)"))
        assertEquals("Anthology", normalizeAlbumName("Anthology (Disc Two)"))
        assertEquals("Finyl Vinyl", normalizeAlbumName("Finyl Vinyl (Disc Two)"))
        // No space variant
        assertEquals("The Wall", normalizeAlbumName("The Wall (DiscTwo)"))
    }

    @Test
    fun normalize_cyrillicDiskKeyword() {
        // Russian-language libraries: «Диск» is the Cyrillic equivalent of
        // 'Disk'. Same patterns as the English variants.
        assertEquals(
            "Ленинград 1984 [2009, Отделение Выход, В 139]",
            normalizeAlbumName("Ленинград 1984 (Диск 1) [2009, Отделение Выход, В 139]"),
        )
        assertEquals(
            "Ленинград 1984 [2009, Отделение Выход, В 140]",
            normalizeAlbumName("Ленинград 1984 (Диск 2) [2009, Отделение Выход, В 140]"),
        )
    }

    @Test
    fun normalize_twoDigitDiscNumbers() {
        // Beatles Mono Box has discs numbered up to ~30.
        assertEquals("All You Need Is Love", normalizeAlbumName("All You Need Is Love (Disc 17)"))
        assertEquals("Get Back", normalizeAlbumName("Get Back (Disc 21)"))
        // Zero-padded CD numbers.
        assertEquals(
            "Eine Musikalische Traumreise - Traumhaftes Europa",
            normalizeAlbumName("Eine Musikalische Traumreise - Traumhaftes Europa (CD 01)"),
        )
    }

    // ---- groupAlbumsByGroupId ----------------------------------------------

    private fun makeAlbum(
        name: String,
        folderPath: String,
        parentFolderPath: String = "",
        totalDiscs: Int = 1,
        discNumber: Int = 1,
        artworkFileKey: String = "${name}-key",
        albumArtist: String = "Pink Floyd",
    ) = Album(
        name = name,
        albumArtist = albumArtist,
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
    fun groupAlbums_untaggedDiscNumberInPlainFoldersStaysSeparate() {
        // When disc metadata is untrustworthy (discNumber=0) AND the layout
        // gives no other signal — the subfolders aren't disc-named and the
        // name has no marker — we can't safely fold, so the entries stay
        // separate. (Contrast with disc-named subfolders, which DO fold even
        // when the disc number is untagged — see
        // groupAlbums_discSubfoldersFoldWithoutNameMarker.)
        val disc1 = makeAlbum(
            name = "The Wall",
            folderPath = "/m/pf/wall/part one/",
            parentFolderPath = "/m/pf/wall/",
            totalDiscs = 1,
            discNumber = 1,
        )
        val untagged = makeAlbum(
            name = "The Wall",
            folderPath = "/m/pf/wall/part two/",
            parentFolderPath = "/m/pf/wall/",
            totalDiscs = 1,
            discNumber = 0,
        )

        val result = groupAlbumsByGroupId(listOf(untagged, disc1))

        assertEquals(2, result.size)
    }

    @Test
    fun groupAlbums_discSubfoldersFoldWithoutNameMarker() {
        // KuschelRock 28: three CDs split into sibling "CD 1" / "CD 2" / "CD 3"
        // subfolders. The album NAME carries only a "[3CD]" count hint (not a
        // per-disc marker) and Total Discs is left at 1 — so the only signal
        // that this is multi-disc is the disc-named subfolders. They must
        // still fold into one representative spanning all three discs.
        val parent =
            "D:/music/_cd_rip/VA - Kuschelrock Vol. 1-33/KuschelRock 28 [3CD] (2014)/"
        val discs = (1..3).map { n ->
            makeAlbum(
                name = "KuschelRock 28 [3CD] (2014)",
                folderPath = "${parent}CD $n/",
                parentFolderPath = parent,
                totalDiscs = 1,   // ← untagged
                discNumber = 1,   // ← untagged (parser defaults to 1)
                albumArtist = "Kuschelrock",
                artworkFileKey = "kr28-cd$n",
            )
        }

        val result = groupAlbumsByGroupId(discs.shuffled())

        assertEquals(1, result.size, "three disc subfolders should fold to one rep")
        val rep = result.single()
        assertEquals("KuschelRock 28 [3CD] (2014)", rep.name)
        assertEquals(
            parent,
            rep.folderPath,
            "folderPath rewritten to the parent so getAlbumTracks prefix-matches every disc",
        )
        assertEquals(3, rep.totalDiscs, "totalDiscs reflects the observed disc-folder count")
    }

    @Test
    fun groupAlbums_singleReturnedDiscSubfolderStillFolds() {
        // Defensive: if the server collapses the multi-disc set to a single
        // representative row whose folder is still a disc bucket (…/CD 1), we
        // must rewrite it to the parent and flag it multi-disc so
        // getAlbumTracks pulls the sibling discs in.
        val only = makeAlbum(
            name = "KuschelRock 28 [3CD] (2014)",
            folderPath = "D:/music/KuschelRock 28 [3CD] (2014)/CD 1/",
            parentFolderPath = "D:/music/KuschelRock 28 [3CD] (2014)/",
            totalDiscs = 1,
            discNumber = 1,
            albumArtist = "Kuschelrock",
        )

        val rep = groupAlbumsByGroupId(listOf(only)).single()

        assertEquals("D:/music/KuschelRock 28 [3CD] (2014)/", rep.folderPath)
        assertTrue(rep.totalDiscs >= 2, "must read as grouped so all discs load")
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
    fun groupAlbums_realWorldDiscMarkersInName() {
        // Tom Jones - 100 Hits is the canonical case: 5 discs, each with its
        // own "(Disc N)" suffixed name, in sibling subfolders under a common
        // parent. Total Discs is wrongly tagged as 1.
        val discs = (1..5).map { n ->
            makeAlbum(
                name = "100 Hits (Disc $n)",
                folderPath = "D:/music/Tom Jones lossless/2012 - 100 hits (5 CD)/CD $n/",
                parentFolderPath = "D:/music/Tom Jones lossless/2012 - 100 hits (5 CD)/",
                totalDiscs = 1,            // ← the real-world tagging quirk
                discNumber = n,
                albumArtist = "Tom Jones",
                artworkFileKey = "th-d$n",
            )
        }

        val result = groupAlbumsByGroupId(discs.shuffled())

        assertEquals(1, result.size, "all 5 discs should collapse to one rep")
        val rep = result.single()
        assertEquals("100 Hits", rep.name, "name should be normalised")
        assertEquals(
            "D:/music/Tom Jones lossless/2012 - 100 hits (5 CD)/",
            rep.folderPath,
            "folderPath should be rewritten to the parent so getAlbumTracks prefix-matches all discs",
        )
        assertEquals(5, rep.totalDiscs, "totalDiscs should reflect the observed group size")
        // Disc 1 wins the rep selection so its artwork carries through.
        assertEquals("th-d1", rep.artworkFileKey)
        assertEquals(1, rep.discNumber)
    }

    @Test
    fun groupAlbums_letterDiscMarkersFold() {
        // Depeche Mode "101" ships its two discs as "(Disc A)" / "(Disc B)"
        // rather than numbers. The normaliser strips the letter marker, and
        // the two discs share a parent folder, so they must fold into one rep.
        val discA = makeAlbum(
            name = "101 (Disc A)",
            folderPath = "/m/dm/101/disc a/",
            parentFolderPath = "/m/dm/101/",
            discNumber = 1,
            albumArtist = "Depeche Mode",
            artworkFileKey = "101-a",
        )
        val discB = makeAlbum(
            name = "101 (Disc B)",
            folderPath = "/m/dm/101/disc b/",
            parentFolderPath = "/m/dm/101/",
            discNumber = 2,
            albumArtist = "Depeche Mode",
            artworkFileKey = "101-b",
        )

        val result = groupAlbumsByGroupId(listOf(discB, discA))

        assertEquals(1, result.size, "both letter discs should collapse to one rep")
        val rep = result.single()
        assertEquals("101", rep.name)
        assertEquals(2, rep.totalDiscs)
        assertEquals("101-a", rep.artworkFileKey)
    }

    @Test
    fun groupAlbums_embeddedDiscMarkerInsideLargerParens() {
        // "'98 Live Meltdown" with release metadata in parens; two physical
        // releases (SPV / Toshiba) each with two discs — should produce
        // exactly TWO groups (one per release), not one or four.
        val spvDisc1 = makeAlbum(
            name = "'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany, CD 1)",
            folderPath = "/m/megadeth/spv/cd1/",
            parentFolderPath = "/m/megadeth/spv/",
            discNumber = 1,
            albumArtist = "Megadeth",
        )
        val spvDisc2 = spvDisc1.copy(
            name = "'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany, CD 2)",
            folderPath = "/m/megadeth/spv/cd2/",
            discNumber = 2,
        )
        val toshibaDisc1 = makeAlbum(
            name = "'98 Live Meltdown (1998, Zero / Toshiba, XRCN-2039, Japan, CD 1)",
            folderPath = "/m/megadeth/toshiba/cd1/",
            parentFolderPath = "/m/megadeth/toshiba/",
            discNumber = 1,
            albumArtist = "Megadeth",
        )
        val toshibaDisc2 = toshibaDisc1.copy(
            name = "'98 Live Meltdown (1998, Zero / Toshiba, XRCN-2040, Japan, CD 2)",
            folderPath = "/m/megadeth/toshiba/cd2/",
            discNumber = 2,
        )

        val result = groupAlbumsByGroupId(
            listOf(toshibaDisc2, spvDisc1, toshibaDisc1, spvDisc2),
        )

        assertEquals(2, result.size)
        // Each rep should keep its release-distinguishing metadata in the
        // normalised name (the catalog-number-bearing parens block).
        val names = result.map { it.name }.toSet()
        assertTrue(names.any { it.contains("SPV 089-18542 CD") })
        assertTrue(names.any { it.contains("XRCN-2039") || it.contains("Toshiba") })
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

    // ---- discNumberFromFolder ----------------------------------------------

    @Test
    fun discNumberFromFolder_parsesDiscBucketLeaf() {
        assertEquals(1, discNumberFromFolder("D:/music/KuschelRock 28 [3CD] (2014)/CD 1/"))
        assertEquals(3, discNumberFromFolder("D:/music/KuschelRock 28 [3CD] (2014)/CD 3"))
        assertEquals(2, discNumberFromFolder("/m/pf/wall/Disc 2/"))
        assertEquals(12, discNumberFromFolder("/m/box/CD12/"))
        // Cyrillic keyword + Windows separators.
        assertEquals(4, discNumberFromFolder("""D:\music\box\Диск 4\"""))
    }

    @Test
    fun discNumberFromFolder_parsesHyphenatedDiscBuckets() {
        // Pandora's Box (C3K 86567): discs live in `CD-1` / `CD-2` / `CD-3`
        // folders. Without recovering the disc index here, getAlbumTracks can't
        // split the set by disc and the tracks render interleaved.
        assertEquals(
            1,
            discNumberFromFolder(
                """D:\music\_cd_rip\Aerosmith\...\2002 - Pandora's Box (C3K 86567) USA\CD-1\""",
            ),
        )
        assertEquals(2, discNumberFromFolder("/m/box/CD-2/"))
        assertEquals(3, discNumberFromFolder("/m/box/Disc-3/"))
        assertEquals(2, discNumberFromFolder("/m/box/CD.2/"))
    }

    @Test
    fun discNumberFromFolder_nullForNonDiscFolders() {
        // Not a disc bucket — the album's real folder.
        assertEquals(null, discNumberFromFolder("D:/music/KuschelRock 28 [3CD] (2014)/"))
        assertEquals(null, discNumberFromFolder("/m/pf/wall/"))
        // Catalog-ish token, not a "CD <n>" bucket.
        assertEquals(null, discNumberFromFolder("/m/box/HNECD032/"))
        assertEquals(null, discNumberFromFolder(""))
    }

    @Test
    fun leafFolderName_handlesBothSeparators() {
        assertEquals("CD 1", leafFolderName("D:/music/album/CD 1/"))
        assertEquals("CD 2", leafFolderName("""D:\music\album\CD 2"""))
        assertEquals("album", leafFolderName("album"))
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
    fun testParseTracksJson_numberPlays() {
        val json = """
        [
            {
                "Key": "1",
                "Name": "T1",
                "Filename": "f1.mp3",
                "Number Plays": 5
            },
            {
                "Key": "2",
                "Name": "T2",
                "Filename": "f2.mp3",
                "Number Plays": "0"
            },
            {
                "Key": "3",
                "Name": "T3",
                "Filename": "f3.mp3"
            }
        ]
        """.trimIndent()
        val tracks = parseMcwsTracksJson(json)
        assertEquals(3, tracks.size)
        assertEquals(5, tracks[0].numberPlays)
        assertEquals(0, tracks[1].numberPlays)
        assertEquals(0, tracks[2].numberPlays)
    }

    @Test
    fun testParseTracksJson_emptyOrInvalid() {
        assertTrue(parseMcwsTracksJson(null).isEmpty())
        assertTrue(parseMcwsTracksJson("").isEmpty())
        assertTrue(parseMcwsTracksJson("[]").isEmpty())
        assertTrue(parseMcwsTracksJson("invalid-json").isEmpty())
    }

    // ---- isVariousArtistsSet ----------------------------------------------

    @Test
    fun various_mixedArtistsWithSentinel_isVarious() {
        // The Pandora's Box case: CD1/CD3 → "(Multiple Artists)", CD2 → "Aerosmith".
        assertTrue(
            isVariousArtistsSet(setOf("Aerosmith", MULTIPLE_ARTISTS_SENTINEL)),
        )
    }

    @Test
    fun various_sentinelMatchIsCaseInsensitive() {
        assertTrue(isVariousArtistsSet(setOf("Aerosmith", "(multiple artists)")))
    }

    @Test
    fun various_singleArtistMultiDisc_isNotVarious() {
        // A normal 2-CD album by one artist must stay under that artist.
        assertFalse(isVariousArtistsSet(setOf("Aerosmith")))
    }

    @Test
    fun various_onlySentinel_isNotVarious() {
        // A single internally-mixed compilation already lives under the sentinel;
        // there's no second artist to fold away from.
        assertFalse(isVariousArtistsSet(setOf(MULTIPLE_ARTISTS_SENTINEL)))
    }

    @Test
    fun various_artistSplitWithoutSentinel_isNotVarious() {
        // CD1 = Artist A, CD2 = Artist B, neither disc internally mixed and no
        // sentinel present: keep it visible under each artist rather than hiding.
        assertFalse(isVariousArtistsSet(setOf("Artist A", "Artist B")))
    }

    @Test
    fun various_emptySet_isNotVarious() {
        assertFalse(isVariousArtistsSet(emptySet()))
    }

    // ---- assignDiscsBySubfolder --------------------------------------------

    private fun trk(folderPath: String, trackNumber: Int): Track = Track(
        fileKey = "", name = "", artist = "", album = "", albumArtist = "",
        date = "", genre = "", durationMs = 0, trackNumber = trackNumber,
        discNumber = 0, totalDiscs = 0, totalTracks = 0, bitrate = 0, bitDepth = 0,
        sampleRate = 0, channels = 0, fileType = "", filePath = "", folderPath = folderPath,
    )

    // ---- filterNotPlayedTracks ---------------------------------------------

    @Test
    fun filterNotPlayed_keepsOnlyUnsetOrZeroPlays() {
        val tracks = listOf(
            trk("/m", 1).copy(name = "a", numberPlays = 0),
            trk("/m", 2).copy(name = "b", numberPlays = 5),
            trk("/m", 3).copy(name = "c", numberPlays = 0),
            trk("/m", 4).copy(name = "d", numberPlays = 1),
        )
        assertEquals(listOf("a", "c"), filterNotPlayedTracks(tracks).map { it.name })
    }

    @Test
    fun filterNotPlayed_emptyWhenAllPlayed() {
        val tracks = listOf(
            trk("/m", 1).copy(numberPlays = 2),
            trk("/m", 2).copy(numberPlays = 1),
        )
        assertTrue(filterNotPlayedTracks(tracks).isEmpty())
    }

    @Test
    fun lastNumberIn_extractsTrailingNumber() {
        assertEquals(2, lastNumberIn("Golden_Vol_2"))
        assertEquals(10, lastNumberIn("CD 10"))
        assertEquals(1, lastNumberIn("Disc-1"))
        assertEquals(null, lastNumberIn("Golden"))
    }

    @Test
    fun assignDiscs_splitsAcrossArbitrarilyNamedSubfolders() {
        // The Romantic Collection / Golden case: discs in Golden_Vol_1 /
        // Golden_Vol_2 with no per-track Disc # tags.
        val base = "/m/Romantic Collection/02. Golden"
        val tracks = listOf(
            trk("$base/Golden_Vol_1", 1),
            trk("$base/Golden_Vol_1", 2),
            trk("$base/Golden_Vol_2", 1),
            trk("$base/Golden_Vol_2", 2),
        )
        assertEquals(listOf(1, 1, 2, 2), assignDiscsBySubfolder(tracks).map { it.discNumber })
    }

    @Test
    fun assignDiscs_ordersByTrailingNumberNotLexicographically() {
        val byFolder = assignDiscsBySubfolder(
            listOf(trk("/m/box/CD 10", 1), trk("/m/box/CD 2", 1)),
        ).associate { it.folderPath to it.discNumber }
        assertEquals(1, byFolder["/m/box/CD 2"])
        assertEquals(2, byFolder["/m/box/CD 10"])
    }

    @Test
    fun assignDiscs_singleFolderIsNoop() {
        val tracks = listOf(trk("/m/a", 1), trk("/m/a", 2))
        assertEquals(tracks, assignDiscsBySubfolder(tracks))
    }
}

