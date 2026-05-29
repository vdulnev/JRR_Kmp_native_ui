package com.jrr.jrrkmp_native_ui.data.repository

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.data.api.BrowseItem
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.DownloadJobEntity
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

private val log = Logger.withTag("repo:Library")

// ---- album-name normalisation -----------------------------------------------
//
// Most multi-disc rips in real libraries carry the disc marker IN THE ALBUM
// NAME (e.g. `100 Hits (Disc 1)`, `... CD 2 (2015, 2CD+DVD, ...)`), not in
// `Total Discs` — that field is often left at 1 even for a 5-CD set. So we
// can't rely on `totalDiscs > 1` alone to detect the multi-disc case; we have
// to recognise the marker in the name and normalise it away to compute a
// stable group key.

// Disc-marker keywords across the variants observed in real libraries —
// English ('Disc' / 'Disk' / 'CD') and Russian ('Диск'). 'One', 'Two', ...
// (written-out numbers) fall out naturally because the suffix part is
// matched as `[A-Za-z0-9А-Яа-я]+`.
private const val DISC_WORDS = """(?:Disc|Disk|CD|Диск)"""

// (Disc N) / (Disk N) / (CD N) / (Disc A) / (Disc One) when the bracket
// content is JUST a disc marker. NOT anchored to end-of-string — strips
// the marker even when followed by another parens block carrying release
// info, e.g. `Garage Inc. (Disc 1) [SHM-CD, UICY-94670]`.
private val DISC_PAREN_ONLY = Regex(
    """\s*[(\[]\s*$DISC_WORDS\s*[A-Za-zА-Яа-я0-9]+\s*[)\]]""",
    RegexOption.IGNORE_CASE,
)

// `, CD N` / `, Disc N` embedded inside a larger parenthesised block like
// `(1998, SPV, ..., CD 1)`. We strip just the disc marker (and its leading
// comma) so the rest of the release metadata is preserved as a distinguishing
// feature between releases of the same multi-disc album.
private val DISC_INSIDE_LARGER_PARENS = Regex(
    """\s*,\s*$DISC_WORDS\s*\d+\b""",
    RegexOption.IGNORE_CASE,
)

// Mid-name `\s+CDN(\s+|\()` — e.g. `... In Tokyo CD1 (2015, …)`. Replaced
// with a single space so the trailing parens block stays attached cleanly.
private val DISC_MID_NAME = Regex(
    """\s+$DISC_WORDS\s*\d+(?=\s|\(|$)""",
    RegexOption.IGNORE_CASE,
)

// Trailing bare `CDNN` (no parens, no preceding space) — e.g.
// `100,000,000 BON JOVI Fans...CD01`. Word-boundary handles the `.→C`
// transition; doesn't match catalog tokens like `HNECD032` (no digit AFTER).
private val DISC_TRAILING_BARE = Regex(
    """\b$DISC_WORDS\s*\d+\s*$""",
    RegexOption.IGNORE_CASE,
)

// A folder whose own name is JUST a disc bucket — `CD 1`, `CD01`, `Disc 2`,
// `Disk 3`, `Диск 4`. Plenty of real-world rips put the disc split ONLY in
// sibling subfolders (…/KuschelRock 28 [3CD] (2014)/CD 1, …/CD 2, …/CD 3)
// while leaving the album NAME free of a per-disc marker and Total Discs at 1.
// Recognising that folder lets us fold those siblings the same way we fold
// name-marked discs.
private val DISC_FOLDER_LEAF = Regex(
    """^$DISC_WORDS\s*\d+$""",
    RegexOption.IGNORE_CASE,
)

// Same shape as DISC_FOLDER_LEAF but captures the number, so we can recover a
// disc index from a `CD 3` folder when the file tags don't carry `Disc #`.
private val DISC_FOLDER_NUM = Regex(
    """^$DISC_WORDS\s*(\d+)$""",
    RegexOption.IGNORE_CASE,
)

private val MULTI_SPACE = Regex("""\s{2,}""")

/**
 * Strip disc-suffix markers from an album name so the stable "base" name can
 * be used as a group key. Examples:
 *
 * ```
 * "100 Hits (Disc 1)"                                 -> "100 Hits"
 * "40 Years - Decades Of Decibels (Disc1)"            -> "40 Years - Decades Of Decibels"
 * "13 (Deluxe Edition, 3735427, CD1)"                 -> "13 (Deluxe Edition, 3735427)"
 * "...To The Rising Sun. In Tokyo CD1 (2015, ..., …)" -> "...To The Rising Sun. In Tokyo (2015, ..., …)"
 * "100,000,000 BON JOVI Fans...CD01"                  -> "100,000,000 BON JOVI Fans..."
 * "'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany, CD 1)"
 *                                                     -> "'98 Live Meltdown (1998, SPV, SPV 089-18542 CD, Germany)"
 * ```
 *
 * Catalog tokens containing "CD" without a trailing digit (e.g. the
 * `SPV 089-18542 CD` chunk above, or `1916 [HNECD032, 2014]`) are
 * preserved.
 *
 * Exposed as `internal` so tests can hammer it with real-world cases.
 */
internal fun normalizeAlbumName(name: String): String {
    var result = name
    // Replace with " " (not "") so a disc-marker block in the middle leaves
    // a clean separator between surrounding tokens; collapsed below.
    result = DISC_PAREN_ONLY.replace(result, " ")
    result = DISC_INSIDE_LARGER_PARENS.replace(result, "")
    result = DISC_MID_NAME.replace(result, " ")
    result = DISC_TRAILING_BARE.replace(result, "")
    result = MULTI_SPACE.replace(result, " ")
    // Drop trailing whitespace/punctuation artefacts left behind by stripping
    // inside parens (e.g. `(... , )` → `(... ,)` → `(...)`).
    result = result.trim().trimEnd(',', '-', ':', ';', ' ')
    return result
}

/**
 * The last path segment of [folderPath], with any trailing separator dropped.
 * Handles both Windows (`\`) and POSIX (`/`) separators. Exposed as `internal`
 * for tests.
 */
internal fun leafFolderName(folderPath: String): String {
    if (folderPath.isEmpty()) return ""
    val trimmed = folderPath.trimEnd('\\', '/')
    val sep = maxOf(trimmed.lastIndexOf('\\'), trimmed.lastIndexOf('/'))
    return if (sep >= 0) trimmed.substring(sep + 1) else trimmed
}

/**
 * True when [folderPath]'s own folder is a disc bucket like `CD 1` / `Disc 2`,
 * i.e. the album is split across sibling disc subfolders under a common parent.
 */
internal fun isDiscSubfolder(folderPath: String): Boolean =
    DISC_FOLDER_LEAF.matches(leafFolderName(folderPath).trim())

/**
 * Recover a 1-based disc index from a disc-bucket folder (`…/CD 3` → 3), or
 * `null` if the folder isn't a disc bucket. Used to repair `Disc #` for tracks
 * whose multi-disc split lives only in sibling subfolders — without it, every
 * disc reads as disc 1 and their track numbers collide in the detail view.
 */
internal fun discNumberFromFolder(folderPath: String): Int? =
    DISC_FOLDER_NUM.matchEntire(leafFolderName(folderPath).trim())
        ?.groupValues?.get(1)?.toIntOrNull()

/**
 * Returns this track with its [Track.discNumber] taken from the enclosing disc
 * folder when present; otherwise unchanged. No-op for tracks not in a `CD N` /
 * `Disc N` folder, so it's safe to apply to every track.
 */
internal fun Track.withFolderDiscNumber(): Track =
    discNumberFromFolder(folderPath)?.let { copy(discNumber = it) } ?: this

// Strip ANY parenthesised content (one level deep) — used only for computing
// the group key, not for displayed names. Lets us fold discs of the same
// multi-disc release that have per-disc catalog numbers (e.g. Toshiba's
// XRCN-2039 / XRCN-2040 for Discs 1 and 2 of the same album) into one group.
private val PARENS_BLOCK = Regex("""\s*[(\[][^()\[\]]*[)\]]\s*""")

/**
 * Stable identifier for "this album group" — used by [groupAlbumsByGroupId]
 * to fold disc-level entries together.
 *
 * Strategy:
 * - **Normalize the name** so `100 Hits (Disc 1)` and `100 Hits (Disc 2)`
 *   reduce to the same key, plus additionally strip *all* parenthesised
 *   metadata (release year, catalog numbers, country, format) so that
 *   discs of the same release with per-disc catalog numbers still fold.
 *   The displayed name on the rep keeps the parenthesised release info so
 *   different releases of the same album stay visibly distinct.
 * - **Use `parentFolderPath` as the path key when the album appears to be
 *   multi-disc** — the name carried a disc marker, `totalDiscs > 1` is
 *   properly tagged, or the album's own folder is a disc bucket like `CD 1`
 *   (the disc split lives only in sibling subfolders). This groups discs that
 *   live in sibling subfolders under a common parent.
 * - **Use `folderPath` as the path key otherwise** — preserves the
 *   distinction between two genuinely different single-disc albums that
 *   happen to share a name (e.g. two "Greatest Hits" compilations in
 *   different folders).
 *
 * Includes `albumArtist` so different artists' albums don't collide on
 * shared root folders.
 */
private fun computeGroupKey(album: Album): String {
    val normalized = normalizeAlbumName(album.name)
    val hasDiscMarker = normalized != album.name
    val taggedMultiDisc = album.totalDiscs > 1 && album.discNumber > 0
    val folderIsDisc = isDiscSubfolder(album.folderPath)
    val pathKey = if (hasDiscMarker || taggedMultiDisc || folderIsDisc) {
        album.parentFolderPath
    } else {
        album.folderPath
    }
    val groupingName = PARENS_BLOCK.replace(normalized, " ").trim()
    return "${groupingName.lowercase()}|${album.albumArtist.lowercase()}|${pathKey.lowercase()}"
}

/**
 * Collapse a list of disc-level [Album]s into one representative per group.
 *
 * The rep is the disc with the lowest non-zero `discNumber` (typically Disc 1).
 * When the group has more than one disc, the rep is **rewritten** so that:
 *
 * - `name` becomes the normalised name (`"100 Hits"`, not `"100 Hits (Disc 1)"`)
 * - `folderPath` becomes `parentFolderPath`, so [LibraryRepository.getAlbumTracks]
 *   prefix-matches across every disc subfolder
 * - `totalDiscs` reflects the observed group size (matters because real-world
 *   data often has `Total Discs = 1` even for 5-CD sets — see [normalizeAlbumName]'s
 *   docstring).
 *
 * Untagged disc numbers (`discNumber == 0`) sort to the end of the candidate
 * list so a properly-tagged Disc 1 wins when available.
 *
 * Exposed as `internal` so unit tests in the same module can drive it without
 * standing up a full repository instance.
 */
internal fun groupAlbumsByGroupId(albums: List<Album>): List<Album> =
    albums
        .groupBy { computeGroupKey(it) }
        .map { (_, discs) ->
            val rep = discs.minByOrNull { d ->
                if (d.discNumber > 0) d.discNumber else Int.MAX_VALUE
            } ?: discs.first()
            // Fold when we saw multiple discs, OR when the rep's own folder is
            // a disc bucket (`…/CD 1`) — the latter covers the case where the
            // server collapsed the discs to a single row but the layout still
            // tells us this is a multi-disc set whose siblings live under the
            // shared parent.
            val foldsByFolder = isDiscSubfolder(rep.folderPath)
            if (discs.size > 1 || foldsByFolder) {
                rep.copy(
                    name = normalizeAlbumName(rep.name),
                    folderPath = rep.parentFolderPath,
                    // ≥2 so getAlbumTracks treats it as grouped and prefix-
                    // matches every disc folder under the parent.
                    totalDiscs = maxOf(
                        discs.size,
                        rep.totalDiscs,
                        if (foldsByFolder) 2 else 1,
                    ),
                )
            } else {
                rep
            }
        }

/**
 * Resolves whether the app is currently in offline mode. Modelled as a SAM
 * interface so Swift can implement it directly without the
 * `() -> Boolean` -> `() -> KotlinBoolean` boxing dance the closure form
 * forces over the Kotlin/Native boundary. SKIE 0.10.x does not fix this
 * for closures returning primitives, so the interface stays.
 */
fun interface OfflineModeProvider {
    fun isOffline(): Boolean
}

class LibraryRepository(
    private val database: JrrDatabase?,
    private val mcwsClient: McwsClient,
    private val isOfflineProvider: OfflineModeProvider,
) {
    var onDownloadQueued: ((track: Track, jobId: Int) -> Unit)? = null

    private val ESC_REGEX = Regex("""[\[\]()\-]""")
    fun esc(value: String): String =
        value.replace(ESC_REGEX) { "/${it.value}" }

    suspend fun searchFiles(query: String): List<Track> = withContext(Dispatchers.IO) {
        val offline = isOfflineProvider.isOffline()
        log.d { "searchFiles(q='$query') offline=$offline" }
        if (offline) {
            val db = database ?: return@withContext emptyList()
            return@withContext db.downloadedTrackDao().getAllTracks()
                .filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.artist.contains(query, ignoreCase = true) ||
                            it.album.contains(query, ignoreCase = true)
                }
                .map { it.toTrack() }
                .also { log.d { "searchFiles offline → ${it.size} from cache" } }
        }
        val mcwsQuery =
            "[Media Type]=Audio ([Name]=\"$query\" OR [Artist]=\"$query\" OR [Album]=\"$query\")"
        mcwsClient.searchTracks(mcwsQuery)
    }

    suspend fun getDownloadedTracks(): List<Track> = withContext(Dispatchers.IO) {
        val db = database ?: return@withContext emptyList()
        db.downloadedTrackDao().getAllTracks().map { it.toTrack() }
            .also { log.d { "getDownloadedTracks → ${it.size}" } }
    }

    suspend fun getArtists(): List<String> = withContext(Dispatchers.IO) {
        val offline = isOfflineProvider.isOffline()
        log.d { "getArtists() offline=$offline" }
        if (offline) {
            val artistsSet = mutableSetOf<String>()
            val db = database ?: return@withContext emptyList()
            db.downloadedTrackDao().getAllTracks().forEach {
                val artist = it.artist.trim()
                if (artist.isNotEmpty()) {
                    artistsSet.add(artist)
                }
            }
            return@withContext artistsSet.sortedWith(compareBy { it.lowercase() })
                .also { log.d { "getArtists offline → ${it.size} from cache" } }
        }
        val mcwsQuery =
            "[Media Type]=Audio ~limit=-1,1,[Album Artist (auto)] ~sort=[Album Artist (auto)]"
        mcwsClient.searchTracks(mcwsQuery).map { track -> track.albumArtist }
    }

    suspend fun getAlbumsByArtist(artistName: String): List<Album> = withContext(Dispatchers.IO) {
        val offline = isOfflineProvider.isOffline()
        log.d { "getAlbumsByArtist($artistName) offline=$offline" }
        if (offline) {
            val db = database ?: return@withContext emptyList()
            val albums = db.downloadedTrackDao().getAllTracks()
                .asSequence()
                .filter { it.artist.equals(artistName, ignoreCase = true) }
                .filter { it.album.trim().isNotEmpty() }
                .map { Album(it.toTrack()) }
                .toList()
            return@withContext groupAlbumsByGroupId(albums)
                .sortedWith(compareBy<Album>({ it.date }, { it.name.lowercase() }))
                .also { log.d { "getAlbumsByArtist offline → ${it.size} from cache" } }
        }
        val mcwsQuery =
            "[Album Artist (auto)]=[${esc(artistName)}] ~limit=-1,1,[Album],[Filename (path)] ~sort=[Album]"
        val raw = mcwsClient.searchTracks(mcwsQuery).map { track -> Album(track) }
        groupAlbumsByGroupId(raw)
            .sortedWith(compareBy<Album>({ it.date }, { it.name.lowercase() }))
            .also { log.d { "getAlbumsByArtist → ${it.size} groups (from ${raw.size} disc-level entries)" } }
    }

    suspend fun getAlbumTracks(album: Album): List<Track> = withContext(Dispatchers.IO) {
        val offline = isOfflineProvider.isOffline()
        // A grouped representative carries `totalDiscs > 1` after
        // [groupAlbumsByGroupId] rewrites it — even when the source data
        // had `Total Discs = 1` per disc. So this flag is the signal that
        // `album.folderPath` is now the parent and `album.name` is the
        // normalised base name; we have to relax the per-disc filters
        // accordingly.
        val isGrouped = album.totalDiscs > 1
        log.d {
            "getAlbumTracks(${album.name}) offline=$offline grouped=$isGrouped " +
                "path=${album.folderPath}"
        }
        if (offline) {
            val db = database ?: return@withContext emptyList()
            return@withContext db.downloadedTrackDao().getAllTracks()
                .filter { entity ->
                    val sameAlbum = if (isGrouped) {
                        // Per-disc rows carry the disc-suffixed name like
                        // "100 Hits (Disc 1)"; normalise both sides.
                        normalizeAlbumName(entity.album)
                            .equals(album.name, ignoreCase = true)
                    } else {
                        entity.album.equals(album.name, ignoreCase = true)
                    }
                    val folderMatches = if (isGrouped) {
                        // album.folderPath has been rewritten to the parent —
                        // prefix-match every disc subfolder.
                        entity.folderPath.startsWith(album.folderPath, ignoreCase = true)
                    } else {
                        entity.folderPath.equals(album.folderPath, ignoreCase = true)
                    }
                    sameAlbum && folderMatches
                }
                .map { it.toTrack().withFolderDiscNumber() }
                .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
                .also { log.d { "getAlbumTracks offline → ${it.size} from cache" } }
        }
        // Online: JRiver's `[Filename (path)]="<path>"` form is a prefix
        // match — perfect for the grouped case where folderPath is the
        // parent. For grouped multi-disc we skip the `[Album]=` filter
        // (per-disc names don't match the normalised name) and rely on
        // the parent-folder prefix + [Media Type]=Audio to scope the
        // result — sibling albums would live under a different parent.
        val mcwsQuery = if (isGrouped && album.folderPath.isNotEmpty()) {
            "[Media Type]=Audio [Filename (path)]=\"${esc(album.folderPath)}\" " +
                "~sort=[Disc #],[Track #]"
        } else {
            val pathFilter = if (album.folderPath.isNotEmpty()) {
                "[Filename (path)]=\"${esc(album.folderPath)}\""
            } else {
                ""
            }
            "[Album]=[${esc(album.name)}] $pathFilter ~sort=[Disc #],[Track #]"
        }
        mcwsClient.searchTracks(mcwsQuery)
            .map { it.withFolderDiscNumber() }
            .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
    }

    suspend fun getRandomAlbums(limit: Int = 10): List<Album> = withContext(Dispatchers.IO) {
        log.d { "getRandomAlbums(limit=$limit)" }
        val mcwsQuery = "[Media Type]=Audio ~limit=$limit,-1,[Album],[Filename (path)] ~n=$limit"
        mcwsClient.searchTracks(mcwsQuery).map { track -> Album(track) }
    }

    suspend fun getZones(): List<Zone> = withContext(Dispatchers.IO) {
        mcwsClient.getZones()
    }

    suspend fun getBrowseChildren(parentId: String): List<BrowseItem> =
        withContext(Dispatchers.IO) {
            mcwsClient.getBrowseChildren(parentId)
        }

    suspend fun getBrowseFiles(nodeId: String): List<Track> = withContext(Dispatchers.IO) {
        mcwsClient.getBrowseFiles(nodeId)
    }

    suspend fun getRemoteQueue(): List<Track> = withContext(Dispatchers.IO) {
        mcwsClient.getRemoteQueue()
    }

    suspend fun startDownload(track: Track): Int? = withContext(Dispatchers.IO) {
        log.i { "startDownload(${track.fileKey} / ${track.name})" }
        val db = database ?: return@withContext null
        val jobDao = db.downloadJobDao()
        val trackDao = db.downloadedTrackDao()

        if (trackDao.getTrack(track.fileKey) != null) {
            log.d { "startDownload skipped: track already downloaded fileKey=${track.fileKey}" }
            return@withContext null
        }
        if (jobDao.getAllJobs().any { it.fileKey == track.fileKey }) {
            log.d { "startDownload skipped: job already queued fileKey=${track.fileKey}" }
            return@withContext null
        }

        val job = DownloadJobEntity(
            fileKey = track.fileKey,
            state = "QUEUED",
            bytesDownloaded = 0L,
            bytesTotal = 0L,
            enqueuedAt = getTimeMillis(),
            name = track.name,
            artist = track.artist,
            album = track.album,
            albumArtist = track.albumArtist,
            durationMs = track.durationMs,
            trackNumber = track.trackNumber,
            genre = track.genre,
            fileType = track.fileType,
            date = track.date,
            discNumber = track.discNumber,
            totalDiscs = track.totalDiscs,
            totalTracks = track.totalTracks,
            folderPath = track.folderPath,
            bitrate = track.bitrate,
            bitDepth = track.bitDepth,
            sampleRate = track.sampleRate,
            channels = track.channels,
            filePath = track.filePath
        )
        val jobId = jobDao.insert(job).toInt()
        log.d { "startDownload queued jobId=$jobId fileKey=${track.fileKey}" }
        onDownloadQueued?.invoke(track, jobId)
        jobId
    }
}
