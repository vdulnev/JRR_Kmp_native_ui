package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.api.McwsXmlParser
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
import kotlinx.serialization.json.*

class LibraryRepository(
    private val database: JrrDatabase?,
    private val isOfflineProvider: () -> Boolean
) {
    var onDownloadQueued: ((track: Track, jobId: Int) -> Unit)? = null

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

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

    internal fun parseTracksJson(jsonStr: String?): List<Track> {
        if (jsonStr.isNullOrEmpty()) return emptyList()
        return try {
            val jsonArray = jsonParser.parseToJsonElement(jsonStr).jsonArray
            jsonArray.mapNotNull { element ->
                val obj = element.jsonObject
                val key = obj["Key"]?.jsonPrimitive?.content ?: return@mapNotNull null
                if (key.isEmpty()) return@mapNotNull null
                
                val name = obj["Name"]?.jsonPrimitive?.content ?: "Unknown"
                val artist = obj["Artist"]?.jsonPrimitive?.content ?: "Unknown"
                val album = obj["Album"]?.jsonPrimitive?.content ?: "Unknown"
                val albumArtist = obj["Album Artist (auto)"]?.jsonPrimitive?.content 
                    ?: obj["Artist"]?.jsonPrimitive?.content 
                    ?: "Unknown"
                val genre = obj["Genre"]?.jsonPrimitive?.content ?: "Unknown"
                val durationSec = obj["Duration"]?.jsonPrimitive?.contentOrNull?.toDoubleOrNull() ?: 0.0
                val durationMs = (durationSec * 1000).toLong()
                val trackNum = obj["Track #"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
                val discNum = obj["Disc #"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 1
                val totalDiscs = obj["Total Discs"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 1
                val totalTracks = obj["Total Tracks"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 1
                val bitrate = obj["Bitrate"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
                val bitDepth = obj["Bit depth"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
                val sampleRate = obj["Sample Rate"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 0
                val channels = obj["Channels"]?.jsonPrimitive?.contentOrNull?.toIntOrNull() ?: 2
                val fileType = obj["File Type"]?.jsonPrimitive?.content ?: "Unknown"
                val filePath = obj["Filename"]?.jsonPrimitive?.content ?: ""

                Track(
                    fileKey = key,
                    name = name,
                    artist = artist,
                    album = album,
                    albumArtist = albumArtist,
                    genre = genre,
                    durationMs = durationMs,
                    trackNumber = trackNum,
                    discNumber = discNum,
                    totalDiscs = totalDiscs,
                    totalTracks = totalTracks,
                    imageUrl = McwsClient.buildImageUrl(key),
                    bitrate = bitrate,
                    bitDepth = bitDepth,
                    sampleRate = sampleRate,
                    channels = channels,
                    fileType = fileType,
                    filePath = filePath
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
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
        val json = McwsClient.getMcwsJson("Files/Search", mapOf("Query" to mcwsQuery))
        parseTracksJson(json)
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
        val json = McwsClient.getMcwsJson("Files/Search", mapOf("Query" to mcwsQuery, "Fields" to "Artist"))
        if (json.isNullOrEmpty()) return@withContext emptyList()
        val artistsSet = mutableSetOf<String>()
        try {
            val jsonArray = jsonParser.parseToJsonElement(json).jsonArray
            for (element in jsonArray) {
                val obj = element.jsonObject
                val artist = obj["Artist"]?.jsonPrimitive?.content?.trim() ?: ""
                if (artist.isNotEmpty()) {
                    artistsSet.add(artist)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        artistsSet.sortedWith(compareBy { it.lowercase() })
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
        val json = McwsClient.getMcwsJson("Files/Search", mapOf("Query" to mcwsQuery, "Fields" to "Key;Album;Artist;Filename;Date (readable)"))
        if (json.isNullOrEmpty()) return@withContext emptyList()
        val albums = mutableListOf<Album>()
        try {
            val jsonArray = jsonParser.parseToJsonElement(json).jsonArray
            for (element in jsonArray) {
                val obj = element.jsonObject
                val albumName = obj["Album"]?.jsonPrimitive?.content?.trim() ?: ""
                val key = obj["Key"]?.jsonPrimitive?.content ?: ""
                if (albumName.isNotEmpty()) {
                    albums.add(
                        Album(
                            name = albumName,
                            artist = obj["Artist"]?.jsonPrimitive?.content ?: artistName,
                            folderPath = obj["Filename"]?.jsonPrimitive?.content ?: "",
                            year = obj["Date (readable)"]?.jsonPrimitive?.content ?: "",
                            imageUrl = if (key.isNotEmpty()) McwsClient.buildImageUrl(key) else ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        albums
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
        val json = McwsClient.getMcwsJson("Files/Search", mapOf("Query" to mcwsQuery))
        parseTracksJson(json).sortedWith(compareBy({ it.discNumber }, { it.trackNumber }))
    }

    suspend fun getRandomAlbums(limit: Int = 10): List<Album> = withContext(Dispatchers.IO) {
        val mcwsQuery = "[Media Type]=Audio ~limit=$limit,-1,[Album],[Filename (path)] ~n=$limit"
        val json = McwsClient.getMcwsJson("Files/Search", mapOf("Query" to mcwsQuery, "Fields" to "Key;Album;Artist;Filename;Date (readable)"))
        if (json.isNullOrEmpty()) return@withContext emptyList()
        val albums = mutableListOf<Album>()
        try {
            val jsonArray = jsonParser.parseToJsonElement(json).jsonArray
            for (element in jsonArray) {
                val obj = element.jsonObject
                val albumName = obj["Album"]?.jsonPrimitive?.content?.trim() ?: ""
                val key = obj["Key"]?.jsonPrimitive?.content ?: ""
                if (albumName.isNotEmpty()) {
                    albums.add(
                        Album(
                            name = albumName,
                            artist = obj["Artist"]?.jsonPrimitive?.content ?: "Unknown",
                            folderPath = obj["Filename"]?.jsonPrimitive?.content ?: "",
                            year = obj["Date (readable)"]?.jsonPrimitive?.content ?: "",
                            imageUrl = if (key.isNotEmpty()) McwsClient.buildImageUrl(key) else ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        albums
    }

    suspend fun getZones(): List<Zone> = withContext(Dispatchers.IO) {
        val xml = McwsClient.getMcwsXml("Playback/Zones")
        if (xml.isNullOrEmpty()) return@withContext emptyList()
        val list = mutableListOf<Zone>()
        try {
            val parsed = McwsXmlParser.parseResponse(xml)
            if (parsed.status == "OK") {
                val numZones = parsed.items["NumberZones"]?.toIntOrNull() ?: 0
                for (i in 0 until numZones) {
                    val id = parsed.items["ZoneID$i"] ?: continue
                    val name = parsed.items["ZoneName$i"] ?: "Zone $i"
                    val guid = parsed.items["ZoneGUID$i"] ?: ""
                    val dlna = parsed.items["ZoneDLNA$i"] == "1"
                    list.add(Zone(id = id, name = name, guid = guid, isDLNA = dlna))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        list
    }

    suspend fun getBrowseChildren(parentId: String): Map<String, String> = withContext(Dispatchers.IO) {
        val xml = McwsClient.getMcwsXml("Browse/Children", mapOf("ID" to parentId, "Version" to "1", "ErrorOnMissing" to "0"))
        if (xml.isNullOrEmpty()) return@withContext emptyMap()
        try {
            val parsed = McwsXmlParser.parseResponse(xml)
            if (parsed.status == "OK") {
                parsed.items
            } else emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    suspend fun getBrowseFiles(nodeId: String): List<Track> = withContext(Dispatchers.IO) {
        val json = McwsClient.getMcwsJson("Browse/Files", mapOf("ID" to nodeId))
        parseTracksJson(json)
    }

    suspend fun getRemoteQueue(): List<Track> = withContext(Dispatchers.IO) {
        val json = McwsClient.getMcwsJson("Playback/Playlist")
        parseTracksJson(json)
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
