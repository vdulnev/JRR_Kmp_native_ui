package com.jrr.jrrkmp_native_ui.data.repository

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

class LibraryRepository(
    private val database: JrrDatabase?,
    private val isOfflineProvider: () -> Boolean
) {
    var onDownloadQueued: ((track: Track, jobId: Int) -> Unit)? = null

    private fun DownloadedTrackEntity.toTrack(): Track {
        return Track(
            fileKey = fileKey,
            name = title,
            artist = artist,
            album = album,
            albumArtist = artist,
            genre = genre ?: "Unknown",
            durationMs = durationMs,
            trackNumber = trackNumber ?: 0,
            discNumber = 1,
            totalDiscs = 1,
            totalTracks = 1,
            imageUrl = "",
            bitrate = 0,
            bitDepth = 0,
            sampleRate = 0,
            channels = 2,
            fileType = "",
            filePath = filePath
        )
    }

    suspend fun searchFiles(query: String): List<Track> = withContext(Dispatchers.IO) {
        if (isOfflineProvider()) {
            val db = database ?: return@withContext emptyList()
            return@withContext db.downloadedTrackDao().getAllTracks()
                .filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.artist.contains(query, ignoreCase = true) ||
                    it.album.contains(query, ignoreCase = true)
                }
                .map { it.toTrack() }
        }
        val mcwsQuery = "[Media Type]=Audio ([Name] contains \"$query\" OR [Artist] contains \"$query\" OR [Album] contains \"$query\")"
        McwsClient.searchTracks(mcwsQuery)
    }

    suspend fun getArtists(): List<String> = withContext(Dispatchers.IO) {
        if (isOfflineProvider()) {
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
        val mcwsQuery = "[Media Type]=Audio ~limit=-1,1,[Artist] ~sort=[Artist]"
        McwsClient.searchArtists(mcwsQuery)
    }

    suspend fun getAlbumsByArtist(artistName: String): List<Album> = withContext(Dispatchers.IO) {
        if (isOfflineProvider()) {
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
                Album(
                    name = it.album,
                    artist = it.artist,
                    folderPath = "",
                    year = "",
                    imageUrl = ""
                )
            }.sortedWith(compareBy { it.name.lowercase() })
        }
        val mcwsQuery = "[Media Type]=Audio [Artist]=\"$artistName\" ~limit=-1,1,[Album] ~sort=[Album]"
        McwsClient.searchAlbums(mcwsQuery)
    }

    suspend fun getAlbumTracks(albumName: String, artistName: String): List<Track> = withContext(Dispatchers.IO) {
        if (isOfflineProvider()) {
            val db = database ?: return@withContext emptyList()
            return@withContext db.downloadedTrackDao().getAllTracks()
                .filter { it.artist.equals(artistName, ignoreCase = true) && it.album.equals(albumName, ignoreCase = true) }
                .map { it.toTrack() }
                .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
        }
        val mcwsQuery = "[Media Type]=Audio [Album]=\"$albumName\" [Artist]=\"$artistName\""
        McwsClient.searchTracks(mcwsQuery).sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
    }

    suspend fun getRandomAlbums(limit: Int = 10): List<Album> = withContext(Dispatchers.IO) {
        val mcwsQuery = "[Media Type]=Audio ~limit=$limit,-1,[Album],[Filename (path)] ~n=$limit"
        McwsClient.searchAlbums(mcwsQuery)
    }

    suspend fun getZones(): List<Zone> = withContext(Dispatchers.IO) {
        McwsClient.getZones()
    }

    suspend fun getBrowseChildren(parentId: String): Map<String, String> = withContext(Dispatchers.IO) {
        McwsClient.getBrowseChildren(parentId)
    }

    suspend fun getBrowseFiles(nodeId: String): List<Track> = withContext(Dispatchers.IO) {
        McwsClient.getBrowseFiles(nodeId)
    }

    suspend fun getRemoteQueue(): List<Track> = withContext(Dispatchers.IO) {
        McwsClient.getRemoteQueue()
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
            title = track.name,
            artist = track.artist,
            album = track.album,
            durationMs = track.durationMs,
            trackNumber = track.trackNumber,
            genre = track.genre,
            fileType = track.fileType
        )
        val jobId = jobDao.insert(job).toInt()
        onDownloadQueued?.invoke(track, jobId)
        jobId
    }
}
