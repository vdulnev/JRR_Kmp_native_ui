package com.jrr.jrrkmp_native_ui.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.*
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import com.jrr.jrrkmp_native_ui.domain.model.*

object McwsClient {
    var currentHost: String? = null
    var currentPort: Int = 52199
    var currentUseSsl: Boolean = false
    var currentSslPort: Int = 52200
    var currentToken: String? = null

    private val jsonConfiguration = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    val httpClient = createPlatformHttpClient().config {
        install(ContentNegotiation) {
            json(jsonConfiguration)
        }
    }

    fun getBaseUrl(): String? {
        val host = currentHost ?: return null
        val scheme = if (currentUseSsl) "https" else "http"
        val port = if (currentUseSsl) currentSslPort else currentPort
        return "$scheme://$host:$port/MCWS/v1"
    }

    fun buildImageUrl(fileKey: String): String {
        val base = getBaseUrl() ?: return ""
        val token = currentToken ?: return ""
        return "$base/File/GetImage?File=$fileKey&Type=Thumbnail&Width=300&Height=300&Square=1&Token=$token"
    }

    suspend fun getRaw(url: String): String? {
        return try {
            val response: HttpResponse = httpClient.get(url)
            if (response.status.value in 200..299) {
                response.bodyAsText()
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private suspend fun getMcwsJson(endpoint: String, params: Map<String, String> = emptyMap()): String? {
        val base = getBaseUrl() ?: return null
        val token = currentToken ?: return null
        
        val url = URLBuilder(base).apply {
            val cleanEndpoint = if (endpoint.startsWith("/")) endpoint.substring(1) else endpoint
            appendPathSegments(cleanEndpoint)
            parameters.append("Action", "JSON")
            parameters.append("Token", token)
            params.forEach { (key, value) ->
                parameters.append(key, value)
            }
        }.buildString()

        return getRaw(url)
    }

    private suspend fun getMcwsXml(endpoint: String, params: Map<String, String> = emptyMap()): String? {
        val base = getBaseUrl() ?: return null
        val token = currentToken
        
        val url = URLBuilder(base).apply {
            val cleanEndpoint = if (endpoint.startsWith("/")) endpoint.substring(1) else endpoint
            appendPathSegments(cleanEndpoint)
            if (token != null) {
                parameters.append("Token", token)
            }
            params.forEach { (key, value) ->
                parameters.append(key, value)
            }
        }.buildString()

        return getRaw(url)
    }

    internal fun parseTracksJson(jsonStr: String?): List<Track> {
        if (jsonStr.isNullOrEmpty()) return emptyList()
        return try {
            val jsonArray = jsonConfiguration.parseToJsonElement(jsonStr).jsonArray
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
                    imageUrl = buildImageUrl(key),
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

    suspend fun searchTracks(query: String): List<Track> {
        val json = getMcwsJson("Files/Search", mapOf("Query" to query))
        return parseTracksJson(json)
    }

    suspend fun searchArtists(query: String): List<String> {
        val json = getMcwsJson("Files/Search", mapOf("Query" to query, "Fields" to "Artist"))
        if (json.isNullOrEmpty()) return emptyList()
        val artistsSet = mutableSetOf<String>()
        try {
            val jsonArray = jsonConfiguration.parseToJsonElement(json).jsonArray
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
        return artistsSet.sortedWith(compareBy { it.lowercase() })
    }

    suspend fun searchAlbums(query: String): List<Album> {
        val json = getMcwsJson("Files/Search", mapOf("Query" to query, "Fields" to "Key;Album;Artist;Filename;Date (readable)"))
        if (json.isNullOrEmpty()) return emptyList()
        val albums = mutableListOf<Album>()
        try {
            val jsonArray = jsonConfiguration.parseToJsonElement(json).jsonArray
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
                          imageUrl = if (key.isNotEmpty()) buildImageUrl(key) else ""
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return albums
    }

    suspend fun getBrowseFiles(nodeId: String): List<Track> {
        val json = getMcwsJson("Browse/Files", mapOf("ID" to nodeId))
        return parseTracksJson(json)
    }

    suspend fun getRemoteQueue(): List<Track> {
        val json = getMcwsJson("Playback/Playlist")
        return parseTracksJson(json)
    }

    suspend fun getZones(): List<Zone> {
        val xml = getMcwsXml("Playback/Zones") ?: return emptyList()
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
        return list
    }

    suspend fun getBrowseChildren(parentId: String): Map<String, String> {
        val xml = getMcwsXml("Browse/Children", mapOf("ID" to parentId, "Version" to "1", "ErrorOnMissing" to "0")) ?: return emptyMap()
        return try {
            val parsed = McwsXmlParser.parseResponse(xml)
            if (parsed.status == "OK") {
                parsed.items
            } else emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyMap()
        }
    }

    suspend fun getPlaybackInfo(zoneId: String): PlayerStatus? {
        val xml = getMcwsXml("Playback/Info", mapOf("Zone" to zoneId, "ZoneType" to "ID")) ?: return null
        val items = try {
            val parsed = McwsXmlParser.parseResponse(xml)
            if (parsed.status == "OK") parsed.items else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } ?: return null

        val stateVal = items["State"]?.toIntOrNull() ?: 0
        val playbackState = PlaybackState.fromMcws(stateVal)

        val fileKey = items["FileKey"]
        val trackInfo = if (!fileKey.isNullOrEmpty()) {
            val serverUrl = getBaseUrl() ?: ""
            val imageRelUrl = items["ImageURL"] ?: ""
            var imageUrl = if (imageRelUrl.isNotEmpty()) {
                val base = serverUrl.removeSuffix("/MCWS/v1").removeSuffix("/MCWS/v1/")
                if (imageRelUrl.startsWith("http")) imageRelUrl else "${base}/${imageRelUrl.removePrefix("/")}"
            } else ""

            val token = currentToken
            if (!token.isNullOrEmpty() && imageUrl.isNotEmpty() && !imageUrl.contains("Token=")) {
                imageUrl = if (imageUrl.contains("?")) {
                    "$imageUrl&Token=$token"
                } else {
                    "$imageUrl?Token=$token"
                }
            }

            TrackInfo(
                fileKey = fileKey,
                name = items["Name"] ?: "Unknown",
                artist = items["Artist"] ?: "Unknown",
                album = items["Album"] ?: "Unknown",
                imageUrl = imageUrl,
                bitrate = items["Bitrate"]?.toIntOrNull() ?: 0,
                bitDepth = items["Bitdepth"]?.toIntOrNull() ?: 0,
                sampleRate = items["SampleRate"]?.toIntOrNull() ?: 0,
                channels = items["Channels"]?.toIntOrNull() ?: 0,
                durationMs = items["DurationMS"]?.toLongOrNull() ?: 0L
            )
        } else null

        val durationMs = items["DurationMS"]?.toLongOrNull() ?: 0L
        val positionMs = items["PositionMS"]?.toLongOrNull() ?: 0L
        val volumeVal = items["Volume"]?.toFloatOrNull() ?: 1.0f
        val volumeDisplay = items["VolumeDisplay"] ?: ""
        val isMuted = volumeDisplay.contains("mute", ignoreCase = true) || volumeVal == 0.0f

        val shuffleModeStr = items["Shuffle"]
        val shuffleMode = ShuffleMode.fromMcws(shuffleModeStr)

        val repeatModeStr = items["Repeat"]
        val repeatMode = RepeatMode.fromMcws(repeatModeStr)

        return PlayerStatus(
            zoneId = zoneId,
            zoneName = items["ZoneName"] ?: "Unknown Zone",
            state = playbackState,
            trackInfo = trackInfo,
            positionMs = positionMs,
            durationMs = durationMs,
            volume = volumeVal,
            isMuted = isMuted,
            shuffleMode = shuffleMode,
            repeatMode = repeatMode,
            playingNowPosition = items["PlayingNowPosition"]?.toIntOrNull() ?: -1,
            playingNowTracks = items["PlayingNowTracks"]?.toIntOrNull() ?: 0
        )
    }

    suspend fun executeCommand(endpoint: String, params: Map<String, String>): Boolean {
        val xml = getMcwsXml(endpoint, params) ?: return false
        return try {
            val parsed = McwsXmlParser.parseResponse(xml)
            parsed.status == "OK"
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

