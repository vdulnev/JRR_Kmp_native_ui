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
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

private val log = Logger.withTag("repo:Library")

/**
 * One resolved level of the browse tree: either child nodes to drill into, or
 * (for a leaf) the files at that node. See [LibraryRepository.getBrowseNode].
 */
sealed interface BrowseContent {
    data class Nodes(val items: List<BrowseItem>) : BrowseContent
    data class Files(val tracks: List<Track>) : BrowseContent
}

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

// Separator tolerated between the disc keyword and its index: nothing,
// whitespace, hyphen, or dot — so `CD 1`, `CD1`, `CD-1`, `CD.1`, `Disc-2`
// all parse. Hyphenated folder/name markers are common in real rips.
private const val DISC_SEP = """[\s.\-]*"""

// (Disc N) / (Disk N) / (CD N) / (CD-1) / (Disc A) / (Disc One) when the
// bracket content is JUST a disc marker. NOT anchored to end-of-string —
// strips the marker even when followed by another parens block carrying
// release info, e.g. `Garage Inc. (Disc 1) [SHM-CD, UICY-94670]`.
private val DISC_PAREN_ONLY = Regex(
    """\s*[(\[]\s*$DISC_WORDS$DISC_SEP[A-Za-zА-Яа-я0-9]+\s*[)\]]""",
    RegexOption.IGNORE_CASE,
)

// `, CD N` / `, Disc N` embedded inside a larger parenthesised block like
// `(1998, SPV, ..., CD 1)`. We strip just the disc marker (and its leading
// comma) so the rest of the release metadata is preserved as a distinguishing
// feature between releases of the same multi-disc album.
private val DISC_INSIDE_LARGER_PARENS = Regex(
    """\s*,\s*$DISC_WORDS$DISC_SEP\d+\b""",
    RegexOption.IGNORE_CASE,
)

// Mid-name `\s+CDN(\s+|\()` — e.g. `... In Tokyo CD1 (2015, …)`. Replaced
// with a single space so the trailing parens block stays attached cleanly.
private val DISC_MID_NAME = Regex(
    """\s+$DISC_WORDS$DISC_SEP\d+(?=\s|\(|$)""",
    RegexOption.IGNORE_CASE,
)

// Trailing bare `CDNN` (no parens, no preceding space) — e.g.
// `100,000,000 BON JOVI Fans...CD01`. Word-boundary handles the `.→C`
// transition; doesn't match catalog tokens like `HNECD032` (no digit AFTER).
private val DISC_TRAILING_BARE = Regex(
    """\b$DISC_WORDS$DISC_SEP\d+\s*$""",
    RegexOption.IGNORE_CASE,
)

// A folder whose own name is JUST a disc bucket — `CD 1`, `CD01`, `CD-1`,
// `Disc 2`, `Disk 3`, `Диск 4`. Plenty of real-world rips put the disc split
// ONLY in sibling subfolders (…/KuschelRock 28 [3CD] (2014)/CD 1, …/CD 2, …)
// while leaving the album NAME free of a per-disc marker and Total Discs at 1.
// Recognising that folder lets us fold those siblings the same way we fold
// name-marked discs.
private val DISC_FOLDER_LEAF = Regex(
    """^$DISC_WORDS$DISC_SEP\d+$""",
    RegexOption.IGNORE_CASE,
)

// Same shape as DISC_FOLDER_LEAF but captures the number, so we can recover a
// disc index from a `CD 3` / `CD-3` folder when the file tags don't carry
// `Disc #`.
private val DISC_FOLDER_NUM = Regex(
    """^$DISC_WORDS$DISC_SEP(\d+)$""",
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
/**
 * Tracks that have never been played — `Track.numberPlays` unset or zero.
 * Exposed as `internal` so unit tests can exercise it directly; the public
 * entry point is [LibraryRepository.notPlayedTracks].
 */
internal fun filterNotPlayedTracks(tracks: List<Track>): List<Track> =
    tracks.filter { it.numberPlays <= 0 }

/**
 * A shuffled copy of [tracks], deterministic for a given [seed]. The seed makes
 * the order stable across re-renders (the UI keeps one seed per shuffle), while
 * a new seed reshuffles. Exposed as `internal` for unit tests; the public entry
 * point is [LibraryRepository.shuffleTracks].
 */
internal fun shuffleTracksList(tracks: List<Track>, seed: Long): List<Track> =
    tracks.shuffled(Random(seed))

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

// The last run of digits in a string ("Golden_Vol_2" -> 2, "CD 10" -> 10),
// or null when there is none.
private val TRAILING_NUMBER = Regex("""(\d+)\D*$""")

internal fun lastNumberIn(text: String): Int? =
    TRAILING_NUMBER.find(text)?.groupValues?.get(1)?.toIntOrNull()

/**
 * Assign each track a disc number from the order of its containing subfolder.
 *
 * For a grouped multi-disc album whose discs live in sibling subfolders with
 * arbitrary names (`CD 1`, `Disc-2`, `Golden_Vol_1`, …), per-track `Disc #`
 * tags are often missing, so the discs would otherwise collapse into one and
 * the tracks interleave. Ordering by the trailing number in each subfolder's
 * leaf name (so `Vol 2` < `Vol 10`), falling back to a case-insensitive sort,
 * recovers the split regardless of the naming scheme.
 *
 * No-op unless the tracks actually span more than one subfolder, so single
 * folder / properly-tagged albums keep their existing disc numbers.
 */
internal fun assignDiscsBySubfolder(tracks: List<Track>): List<Track> {
    val folders = tracks.map { it.folderPath }.distinct()
    if (folders.size <= 1) return tracks
    val ordered = folders.sortedWith(
        compareBy({ lastNumberIn(leafFolderName(it)) ?: Int.MAX_VALUE }, { it.lowercase() }),
    )
    val discByFolder = ordered.withIndex().associate { (index, folder) -> folder to (index + 1) }
    return tracks.map { track -> track.copy(discNumber = discByFolder.getValue(track.folderPath)) }
}

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
internal fun computeGroupKey(album: Album): String {
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
internal fun groupAlbumsByGroupId(albums: List<Album>): List<Album> {
    log.d { "groupAlbumsByGroupId: grouping ${albums.size} albums" }
    val grouped = albums
        .groupBy { computeGroupKey(it) }
        .map { (groupKey, discs) ->
            val rep = discs.minByOrNull { d ->
                if (d.discNumber > 0) d.discNumber else Int.MAX_VALUE
            } ?: discs.first()
            if (discs.size > 1) {
                log.d { "groupAlbumsByGroupId: Group '$groupKey' has ${discs.size} discs. Rep is '${rep.name}' (discNumber=${rep.discNumber}, folder='${rep.folderPath}'). Discs: ${discs.map { "${it.name} (disc ${it.discNumber})" }}" }
            }
            // Fold when we saw multiple discs, OR when the rep's own folder is
            // a disc bucket (`…/CD 1`) — the latter covers the case where the
            // server collapsed the discs to a single row but the layout still
            // tells us this is a multi-disc set whose siblings live under the
            // shared parent.
            val foldsByFolder = isDiscSubfolder(rep.folderPath)
            val hasDiscMarker = normalizeAlbumName(rep.name) != rep.name
            // Fold disc-level rows into one album when we saw multiple discs, the
            // name carried a per-disc marker, the rep's folder is a disc bucket,
            // or it's tagged multi-disc.
            val shouldFold = discs.size > 1 || foldsByFolder || hasDiscMarker || rep.totalDiscs > 1
            // …but only point the album at its PARENT folder when the discs truly
            // live in sibling subfolders — i.e. they span more than one folder, or
            // the rep's own folder is a disc bucket (`…/CD 1`). Discs that share a
            // single folder (e.g. two per-disc FLAC images, or one folder tagged
            // Total Discs=2) must keep their own folder; otherwise the path climbs
            // to the library root and the various-artists filter hides the album.
            val multiFolder = discs.map { it.folderPath }.distinct().size > 1
            val useParentFolder = multiFolder || foldsByFolder
            if (shouldFold) {
                val copied = rep.copy(
                    name = normalizeAlbumName(rep.name),
                    folderPath = if (useParentFolder) rep.parentFolderPath else rep.folderPath,
                    // ≥2 so getAlbumTracks treats it as grouped and prefix-
                    // matches every disc folder under the parent.
                    totalDiscs = maxOf(
                        discs.size,
                        rep.totalDiscs,
                        if (foldsByFolder) 2 else 1,
                    ),
                )
                log.d { "groupAlbumsByGroupId: modified rep for '$groupKey': originalName='${rep.name}' -> name='${copied.name}', originalPath='${rep.folderPath}' -> path='${copied.folderPath}', originalTotalDiscs=${rep.totalDiscs} -> totalDiscs=${copied.totalDiscs}" }
                copied
            } else {
                rep
            }
        }
    log.d { "groupAlbumsByGroupId: grouped down to ${grouped.size} albums" }
    return grouped
}

/**
 * JRiver's auto album-artist sentinel for an album whose tracks span more than
 * one artist (`[Album Artist (auto)]`).
 */
internal const val MULTIPLE_ARTISTS_SENTINEL = "(Multiple Artists)"

/**
 * Whether [albumArtists] — the distinct `[Album Artist (auto)]` values observed
 * across a multi-disc box set's disc folders — mark it as a *various-artists*
 * set.
 *
 * True only when the discs disagree (more than one distinct artist) **and** at
 * least one disc already resolved to [MULTIPLE_ARTISTS_SENTINEL]. Requiring the
 * sentinel keeps a genuine artist split that JRiver never collapsed (e.g.
 * `CD1 = Artist A`, `CD2 = Artist B`, neither disc internally mixed) visible
 * under each artist, rather than making the set vanish from every list.
 *
 * Example (the bug this guards): a 3-CD set with `CD1 = (Multiple Artists)`,
 * `CD2 = Aerosmith`, `CD3 = (Multiple Artists)` → `{Aerosmith, (Multiple
 * Artists)}` → various, so it shows only under `(Multiple Artists)` and not also
 * under `Aerosmith`.
 */
internal fun isVariousArtistsSet(albumArtists: Set<String>): Boolean =
    albumArtists.size > 1 &&
        albumArtists.any { it.equals(MULTIPLE_ARTISTS_SENTINEL, ignoreCase = true) }

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

    suspend fun getDownloadedTracks(): List<Track> = withContext(Dispatchers.IO) {
        val db = database ?: return@withContext emptyList()
        db.downloadedTrackDao().getAllTracks().map { it.toTrack() }
            .also { log.d { "getDownloadedTracks → ${it.size}" } }
    }

    suspend fun getArtists(): List<String> = withContext(Dispatchers.IO) {
        val offline = isOfflineProvider.isOffline()
        log.d { "getArtists() offline=$offline" }
        if (offline) {
            return@withContext getOfflineArtists()
        }
        try {
            val mcwsQuery =
                "[Media Type]=Audio ~limit=-1,1,[Album Artist (auto)] ~sort=[Album Artist (auto)]"
            compilationsFirst(mcwsClient.searchTracks(mcwsQuery).map { track -> track.albumArtist })
        } catch (e: Exception) {
            log.w(e) { "getArtists online search failed, falling back to offline" }
            getOfflineArtists()
        }
    }

    private suspend fun getOfflineArtists(): List<String> {
        val artistsSet = mutableSetOf<String>()
        val db = database ?: return emptyList()
        db.downloadedTrackDao().getAllTracks().forEach {
            val artist = it.artist.trim()
            if (artist.isNotEmpty()) {
                artistsSet.add(artist)
            }
        }
        return compilationsFirst(
            artistsSet.sortedWith(compareBy { it.lowercase() }),
        ).also { log.d { "getOfflineArtists → ${it.size} from cache" } }
    }

    /**
     * Move the compilations entry ([MULTIPLE_ARTISTS_SENTINEL]) to the front so
     * compilations always appear first in the Artists list. Order of the rest is
     * preserved.
     */
    private fun compilationsFirst(artists: List<String>): List<String> {
        val (compilations, rest) =
            artists.partition { it.equals(MULTIPLE_ARTISTS_SENTINEL, ignoreCase = true) }
        return compilations + rest
    }

    /**
     * Distinct track-level artists that appear inside compilation albums (albums
     * whose `[Album Artist (auto)]` is [MULTIPLE_ARTISTS_SENTINEL]). Powers the
     * "artists from compilations" list. Sorted case-insensitively.
     */
    suspend fun getCompilationArtists(): List<String> = withContext(Dispatchers.IO) {
        val offline = isOfflineProvider.isOffline()
        log.d { "getCompilationArtists() offline=$offline" }
        if (offline) {
            return@withContext getOfflineCompilationArtists()
        }
        try {
            val mcwsQuery =
                "[Media Type]=Audio [Album Artist (auto)]=[${esc(MULTIPLE_ARTISTS_SENTINEL)}] " +
                        "~limit=-1,1,[Artist] ~sort=[Artist]"
            mcwsClient.searchTracks(mcwsQuery)
                .map { it.artist.trim() }
                .filter { it.isNotEmpty() }
                .distinct()
                .also { log.d { "getCompilationArtists → ${it.size}" } }
        } catch (e: Exception) {
            log.w(e) { "getCompilationArtists online search failed, falling back to offline" }
            getOfflineCompilationArtists()
        }
    }

    private suspend fun getOfflineCompilationArtists(): List<String> {
        val db = database ?: return emptyList()
        return db.downloadedTrackDao().getAllTracks()
            .asSequence()
            .filter { it.albumArtist.equals(MULTIPLE_ARTISTS_SENTINEL, ignoreCase = true) }
            .map { it.artist.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sortedWith(compareBy { it.lowercase() })
            .toList()
            .also { log.d { "getOfflineCompilationArtists → ${it.size}" } }
    }

    /**
     * Compilation albums (`[Album Artist (auto)]` = [MULTIPLE_ARTISTS_SENTINEL])
     * that contain at least one track by [artistName]. Grouped/sorted like
     * [getAlbumsByArtist].
     */
    suspend fun getCompilationAlbumsByArtist(artistName: String): List<Album> =
        withContext(Dispatchers.IO) {
            val offline = isOfflineProvider.isOffline()
            log.d { "getCompilationAlbumsByArtist($artistName) offline=$offline" }
            if (offline) {
                return@withContext getOfflineCompilationAlbumsByArtist(artistName)
            }
            try {
                val mcwsQuery =
                    "[Media Type]=Audio [Album Artist (auto)]=[${esc(MULTIPLE_ARTISTS_SENTINEL)}] " +
                            "[Artist]=[${esc(artistName)}] " +
                            "~limit=-1,1,[Album],[Filename (path)] ~sort=[Album]"
                val raw = mcwsClient.searchTracks(mcwsQuery).map { track -> Album(track) }
                groupAlbumsByGroupId(raw)
                    .sortedBy { it.name.lowercase() }
                    .also { log.d { "getCompilationAlbumsByArtist → ${it.size}" } }
            } catch (e: Exception) {
                log.w(e) { "getCompilationAlbumsByArtist online search failed for $artistName, falling back to offline" }
                getOfflineCompilationAlbumsByArtist(artistName)
            }
        }

    private suspend fun getOfflineCompilationAlbumsByArtist(artistName: String): List<Album> {
        val db = database ?: return emptyList()
        val matchingFolders = db.downloadedTrackDao().getAllTracks()
            .asSequence()
            .filter { it.albumArtist.equals(MULTIPLE_ARTISTS_SENTINEL, ignoreCase = true) }
            .filter { it.artist.equals(artistName, ignoreCase = true) }
            .map { Album(it.toTrack()) }
            .toList()
        return groupAlbumsByGroupId(matchingFolders)
            .sortedBy { it.name.lowercase() }
            .also { log.d { "getOfflineCompilationAlbumsByArtist → ${it.size}" } }
    }

    suspend fun getAlbumsByArtist(artistName: String): List<Album> = withContext(Dispatchers.IO) {
        val offline = isOfflineProvider.isOffline()
        log.d { "getAlbumsByArtist($artistName) offline=$offline" }
        if (offline) {
            return@withContext getOfflineAlbumsByArtist(artistName)
        }
        try {
            val mcwsQuery =
                "[Media Type]=Audio [Album Artist (auto)]=[${esc(artistName)}] ~limit=-1,1,[Album],[Filename (path)] ~sort=[Album]"
            val raw = mcwsClient.searchTracks(mcwsQuery).map { track -> Album(track) }
            val grouped = groupAlbumsByGroupId(raw).sortedBy { it.name.lowercase() }
            // A multi-disc box set whose discs carry mixed album artists otherwise
            // surfaces under each disc's artist (e.g. CD2 tagged "Aerosmith" next to
            // CD1/CD3 resolved to "(Multiple Artists)"). When browsing a specific
            // artist, drop such various-artists sets so they appear only under the
            // multiple-artists sentinel. One prefix-match query per folded set —
            // `[Filename (path)]="<parent>"` spans every disc subfolder.
            val filtered = if (artistName.equals(MULTIPLE_ARTISTS_SENTINEL, ignoreCase = true)) {
                grouped
            } else {
                coroutineScope {
                    val variousFlags = grouped.map { album ->
                        async {
                            album.totalDiscs > 1 &&
                                    isVariousArtistsSet(albumArtistsUnderFolder(album.folderPath))
                        }
                    }
                    grouped.filterIndexed { index, _ -> !variousFlags[index].await() }
                }
            }
            filtered.also {
                log.d {
                    "getAlbumsByArtist → ${it.size} groups (from ${raw.size} disc-level " +
                            "entries, ${grouped.size - it.size} various-artist sets hidden)"
                }
            }
        } catch (e: Exception) {
            log.w(e) { "getAlbumsByArtist online search failed for $artistName, falling back to offline" }
            getOfflineAlbumsByArtist(artistName)
        }
    }

    private suspend fun getOfflineAlbumsByArtist(artistName: String): List<Album> {
        val db = database ?: return emptyList()
        val allTracks = db.downloadedTrackDao().getAllTracks()
        val albums = allTracks
            .asSequence()
            .filter { it.artist.equals(artistName, ignoreCase = true) }
            .filter { it.album.trim().isNotEmpty() }
            .map { Album(it.toTrack()) }
            .toList()
        val grouped = groupAlbumsByGroupId(albums).sortedBy { it.name.lowercase() }
        val filtered = if (artistName.equals(MULTIPLE_ARTISTS_SENTINEL, ignoreCase = true)) {
            grouped
        } else {
            grouped.filterNot { album ->
                album.totalDiscs > 1 && isVariousArtistsSet(
                    allTracks.asSequence()
                        .filter {
                            it.folderPath.startsWith(album.folderPath, ignoreCase = true)
                        }
                        .map { it.albumArtist }
                        .toSet()
                )
            }
        }
        return filtered.also { log.d { "getOfflineAlbumsByArtist → ${it.size} from cache" } }
    }

    /**
     * Distinct `[Album Artist (auto)]` values across every disc under
     * [folderPath] (a box set's parent folder). `[Filename (path)]="<path>"` is
     * a prefix match in JRiver, so a single query spans all disc subfolders.
     */
    private suspend fun albumArtistsUnderFolder(folderPath: String): Set<String> {
        if (folderPath.isEmpty()) return emptySet()
        val query =
            "[Media Type]=Audio [Filename (path)]=\"${esc(folderPath)}\" " +
                "~limit=-1,1,[Album Artist (auto)]"
        return mcwsClient.searchTracks(query).map { it.albumArtist }.toSet()
    }

    /**
     * Resolve one album by its [Album.albumGroupId] — the unique album identity
     * (normalized name + folder path) used for favorites and media ids. Name and
     * artist alone are NOT unique (reissues, same-titled albums), so callers
     * carrying an album reference across a process/IPC boundary (e.g. Android
     * Auto media ids) should round-trip the groupId and resolve through here.
     *
     * [albumArtist] scopes the lookup to one artist's albums via
     * [getAlbumsByArtist].
     */
    suspend fun getAlbumByGroupId(albumArtist: String, groupId: String): Album? =
        withContext(Dispatchers.IO) {
            log.d { "getAlbumByGroupId(artist=$albumArtist, groupId=$groupId)" }
            getAlbumsByArtist(albumArtist).find { it.albumGroupId == groupId }
                .also { if (it == null) log.w { "getAlbumByGroupId: no album for artist=$albumArtist groupId=$groupId" } }
        }

    private suspend fun getOfflineAllAlbums(): List<Album> {
        val db = database ?: return emptyList()
        val allTracks = db.downloadedTrackDao().getAllTracks()
        val albums = allTracks
            .asSequence()
            .filter { it.album.trim().isNotEmpty() }
            .map { Album(it.toTrack()) }
            .toList()
        return groupAlbumsByGroupId(albums)
            .sortedBy { it.name.lowercase() }
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
            return@withContext getOfflineAlbumTracks(album, isGrouped)
        }
        try {
            // Online: JRiver's `[Filename (path)]="<path>"` form is a prefix
            // match — perfect for the grouped case where folderPath is the
            // parent. For grouped multi-disc we skip the `[Album]=` filter
            // (per-disc names don't match the normalised name) and rely on
            // the parent-folder prefix + [Media Type]=Audio to scope the
            // result — sibling albums would live under a different parent.
            // When the album carries a folder, scope the search by `[Filename
            // (path)]` in MCWS (a reliable prefix match) and match the album NAME
            // in Kotlin afterwards. JRiver's `[Album]=[…]` filter silently returns
            // nothing for some names — e.g. ones containing an apostrophe like
            // "The Beatles' Hits" — and a shared folder (such as a box set whose
            // EPs sit side-by-side under one parent) needs the name match to keep
            // out sibling albums. Doing the name match in code sidesteps both.
            val scopeByFolder = album.folderPath.isNotEmpty()
            val mcwsQuery = if (scopeByFolder) {
                "[Media Type]=Audio [Channels]=2 [Filename (path)]=\"${esc(album.folderPath)}\" " +
                        "~sort=[Disc #],[Track #]"
            } else {
                "[Media Type]=Audio [Channels]=2 [Album]=[${esc(album.name)}] ~sort=[Disc #],[Track #]"
            }
            mcwsClient.searchTracks(mcwsQuery)
                // Grouped reps deliberately span disc-suffixed names across sibling
                // folders, so only the non-grouped case filters by exact name.
                .let {
                    if (scopeByFolder && !isGrouped) {
                        // Match on the normalised name. A single folder can hold
                        // several disc-suffixed albums — e.g. a box set of EPs all
                        // tagged "… (Disc N)" side by side under one folder — whose
                        // grouped display name had the suffix stripped. Normalising
                        // both sides keeps only the selected album's tracks while
                        // excluding its folder-mates.
                        val target = normalizeAlbumName(album.name)
                        it.filter { track ->
                            normalizeAlbumName(track.album).equals(target, ignoreCase = true)
                        }
                    } else {
                        it
                    }
                }
                .map { it.withFolderDiscNumber() }
                .let { if (isGrouped) assignDiscsBySubfolder(it) else it }
                .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
        } catch (e: Exception) {
            log.w(e) { "getAlbumTracks online search failed for ${album.name}, falling back to offline" }
            getOfflineAlbumTracks(album, isGrouped)
        }
    }

    private suspend fun getOfflineAlbumTracks(album: Album, isGrouped: Boolean): List<Track> {
        val db = database ?: return emptyList()
        return db.downloadedTrackDao().getAllTracks()
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
            .let { if (isGrouped) assignDiscsBySubfolder(it) else it }
            .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
            .also { log.d { "getOfflineAlbumTracks → ${it.size} from cache" } }
    }

    suspend fun getRandomAlbums(limit: Int = 10): List<Album> = withContext(Dispatchers.IO) {
        log.d { "getRandomAlbums(limit=$limit)" }
        val mcwsQuery = "[Media Type]=Audio [Media Type]=Audio ~limit=$limit,-1,[Album],[Filename (path)] ~n=$limit"
        val raw = mcwsClient.searchTracks(mcwsQuery).map { track -> Album(track) }
        groupAlbumsByGroupId(raw)
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

    /**
     * Resolve one node of the JRiver browse tree. A node either has child nodes
     * (an inner category) or, when it has none, is a leaf whose files we fetch.
     * Fetching files on an inner node returns nothing, so the leaf check (zero
     * children) must gate it — this logic lives here, not in the UI.
     */
    suspend fun getBrowseNode(parentId: String): BrowseContent = withContext(Dispatchers.IO) {
        log.d { "getBrowseNode(parentId=$parentId)" }
        val children = mcwsClient.getBrowseChildren(parentId)
        if (children.isNotEmpty()) {
            BrowseContent.Nodes(children)
        } else {
            BrowseContent.Files(mcwsClient.getBrowseFiles(parentId))
        }
    }

    /**
     * Keep only the tracks that have never been played — JRiver's
     * `[Number Plays]` unset or zero. Powers the Browse "Show not played"
     * toggle. Pure filter over an already-loaded list (e.g. a Browse leaf /
     * playlist), so it needs no extra network round-trip.
     */
    fun notPlayedTracks(tracks: List<Track>): List<Track> = filterNotPlayedTracks(tracks)

    /**
     * Shuffle a track list (e.g. a Browse leaf / playlist). Deterministic for a
     * given [seed] so the order stays stable while the UI shows it; pass a new
     * seed to reshuffle. Powers the Browse "Shuffle" toggle.
     */
    fun shuffleTracks(tracks: List<Track>, seed: Long): List<Track> =
        shuffleTracksList(tracks, seed)

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
