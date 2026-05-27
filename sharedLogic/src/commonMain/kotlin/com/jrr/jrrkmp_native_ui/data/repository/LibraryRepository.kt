package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.data.api.BrowseItem
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.entity.DownloadJobEntity
import com.jrr.jrrkmp_native_ui.data.db.entity.DownloadedTrackEntity
import com.jrr.jrrkmp_native_ui.domain.model.Album
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

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
        if (isOfflineProvider.isOffline()) {
            val db = database ?: return@withContext emptyList()
            return@withContext db.downloadedTrackDao().getAllTracks()
                .filter {
                    it.name.contains(query, ignoreCase = true) ||
                            it.artist.contains(query, ignoreCase = true) ||
                            it.album.contains(query, ignoreCase = true)
                }
                .map { it.toTrack() }
        }
        val mcwsQuery =
            "[Media Type]=Audio ([Name] contains \"$query\" OR [Artist] contains \"$query\" OR [Album] contains \"$query\")"
        mcwsClient.searchTracks(mcwsQuery)
    }

    suspend fun getArtists(): List<String> = withContext(Dispatchers.IO) {
        if (isOfflineProvider.isOffline()) {
            val artistsSet = mutableSetOf<String>()
            val db = database ?: return@withContext emptyList()
            db.downloadedTrackDao().getAllTracks().forEach {
                val artist = it.artist.trim()
                if (artist.isNotEmpty()) {
                    artistsSet.add(artist)
                }
            }
            return@withContext artistsSet.sortedWith(compareBy { it.lowercase() })
        }
        val mcwsQuery =
            "[Media Type]=Audio ~limit=-1,1,[Album Artist (auto)] ~sort=[Album Artist (auto)]"
        mcwsClient.searchTracks(mcwsQuery).map { track -> track.albumArtist }
    }

    suspend fun getAlbumsByArtist(artistName: String): List<Album> = withContext(Dispatchers.IO) {
        if (isOfflineProvider.isOffline()) {
            val albumsMap = mutableMapOf<String, DownloadedTrackEntity>()
            val db = database ?: return@withContext emptyList()
            db.downloadedTrackDao().getAllTracks().forEach {
                if (it.artist.equals(artistName, ignoreCase = true)) {
                    val albumName = it.album.trim()
                    if (albumName.isNotEmpty() && !albumsMap.containsKey(albumName.lowercase())) {
                        albumsMap[albumName.lowercase()] = it
                    }
                }
            }
            return@withContext albumsMap.values.map {
                Album(it.toTrack())
            }.distinct().sortedWith(compareBy { it.name.lowercase() })
        }
        val mcwsQuery =
            "[Album Artist (auto)]=[${esc(artistName)}] ~limit=-1,1,[Album],[Filename (path)] ~sort=[Album]"
        mcwsClient.searchTracks(mcwsQuery).map { track -> Album(track) }
    }

    suspend fun getAlbumTracks(album: Album): List<Track> = withContext(Dispatchers.IO) {
        if (isOfflineProvider.isOffline()) {
            val db = database ?: return@withContext emptyList()
            return@withContext db.downloadedTrackDao().getAllTracks()
                .filter {
                    it.folderPath.equals(
                        album.folderPath,
                        ignoreCase = true
                    ) && it.album.equals(album.name, ignoreCase = true)
                }
                .map { it.toTrack() }
                .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
        }
        val base = "[Album]=[${esc(album.name)}]"
        val filtered = if (album.folderPath.isNotEmpty()) {
            "$base [Filename (path)]=\"${esc(album.folderPath)}\""
        } else {
            base
        }
        val mcwsQuery = "$filtered ~sort=[Disc #],[Track #]"
        mcwsClient.searchTracks(mcwsQuery)
            .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
    }

    suspend fun getRandomAlbums(limit: Int = 10): List<Album> = withContext(Dispatchers.IO) {
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
        val db = database ?: return@withContext null
        val jobDao = db.downloadJobDao()
        val trackDao = db.downloadedTrackDao()

        if (trackDao.getTrack(track.fileKey) != null) return@withContext null
        if (jobDao.getAllJobs().any { it.fileKey == track.fileKey }) return@withContext null

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
        onDownloadQueued?.invoke(track, jobId)
        jobId
    }
}
