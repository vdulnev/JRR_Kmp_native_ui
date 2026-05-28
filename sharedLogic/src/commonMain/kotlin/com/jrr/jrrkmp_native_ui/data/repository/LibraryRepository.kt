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

/**
 * Collapse a list of disc-level [Album]s into one representative per
 * `albumGroupId`. The representative is the disc with the lowest
 * non-zero `discNumber` (typically Disc 1), so its `folderPath` /
 * `artworkFileKey` come from that disc — and crucially its
 * `totalDiscs` and `parentFolderPath` carry the multi-disc signal
 * that [LibraryRepository.getAlbumTracks] later uses to fetch *all*
 * discs at once.
 *
 * Untagged disc numbers (`discNumber == 0`) sort to the end of the
 * candidate list so a properly-tagged Disc 1 wins when available.
 *
 * Exposed as `internal` so unit tests in the same module can drive it
 * without standing up a full repository instance. The public API is
 * [LibraryRepository.getAlbumsByArtist], which calls this helper.
 */
internal fun groupAlbumsByGroupId(albums: List<Album>): List<Album> =
    albums
        .groupBy { it.albumGroupId }
        .map { (_, discs) ->
            discs.minByOrNull { disc ->
                if (disc.discNumber > 0) disc.discNumber else Int.MAX_VALUE
            } ?: discs.first()
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
            "[Media Type]=Audio ([Name] contains \"$query\" OR [Artist] contains \"$query\" OR [Album] contains \"$query\")"
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
                .sortedWith(compareBy { it.name.lowercase() })
                .also { log.d { "getAlbumsByArtist offline → ${it.size} from cache" } }
        }
        val mcwsQuery =
            "[Album Artist (auto)]=[${esc(artistName)}] ~limit=-1,1,[Album],[Filename (path)] ~sort=[Album]"
        val raw = mcwsClient.searchTracks(mcwsQuery).map { track -> Album(track) }
        groupAlbumsByGroupId(raw)
            .sortedWith(compareBy { it.name.lowercase() })
            .also { log.d { "getAlbumsByArtist → ${it.size} groups (from ${raw.size} disc-level entries)" } }
    }

    suspend fun getAlbumTracks(album: Album): List<Track> = withContext(Dispatchers.IO) {
        val offline = isOfflineProvider.isOffline()
        val isMultiDisc = album.totalDiscs > 1
        log.d {
            "getAlbumTracks(${album.name}) offline=$offline multiDisc=$isMultiDisc " +
                if (isMultiDisc) "parentFolderPath=${album.parentFolderPath}" else "folderPath=${album.folderPath}"
        }
        if (offline) {
            val db = database ?: return@withContext emptyList()
            return@withContext db.downloadedTrackDao().getAllTracks()
                .filter { entity ->
                    val sameAlbum = entity.album.equals(album.name, ignoreCase = true)
                    val folderMatches = if (isMultiDisc) {
                        // Each disc lives in its own subfolder under
                        // parentFolderPath — match anything under it.
                        Track.parentPath(entity.folderPath)
                            .equals(album.parentFolderPath, ignoreCase = true)
                    } else {
                        entity.folderPath.equals(album.folderPath, ignoreCase = true)
                    }
                    sameAlbum && folderMatches
                }
                .map { it.toTrack() }
                .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
                .also { log.d { "getAlbumTracks offline → ${it.size} from cache" } }
        }
        // Online: JRiver's "[Filename (path)]=\"$path\"" form is a prefix
        // match — a parent folder path matches all files in any subfolder
        // beneath it, which is exactly what we want for multi-disc.
        val pathFilter = when {
            isMultiDisc && album.parentFolderPath.isNotEmpty() ->
                "[Filename (path)]=\"${esc(album.parentFolderPath)}\""
            album.folderPath.isNotEmpty() ->
                "[Filename (path)]=\"${esc(album.folderPath)}\""
            else -> ""
        }
        val mcwsQuery =
            "[Album]=[${esc(album.name)}] $pathFilter ~sort=[Disc #],[Track #]"
        mcwsClient.searchTracks(mcwsQuery)
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
