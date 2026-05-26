package com.jrr.jrrkmp_native_ui.data.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.*
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import com.jrr.jrrkmp_native_ui.domain.model.*

import kotlinx.coroutines.flow.StateFlow
import com.jrr.jrrkmp_native_ui.data.repository.McwsServerData

private val jsonConfiguration = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

/**
 * Build the shared MCWS HttpClient. Used both by [McwsClient] (for talking to
 * the active server) and by [com.jrr.jrrkmp_native_ui.data.repository.ServerRepository]
 * (for one-off auth / alive checks before any server is active).
 */
fun createMcwsHttpClient(): HttpClient = createPlatformHttpClient().config {
    install(ContentNegotiation) {
        json(jsonConfiguration)
    }
}

/**
 * Parses an MCWS Files JSON response into a list of domain [Track]s.
 * Exposed as a top-level fun so it stays testable without needing a configured
 * [McwsClient] instance.
 */
internal fun parseMcwsTracksJson(jsonStr: String?): List<Track> {
    if (jsonStr.isNullOrEmpty()) return emptyList()
    return try {
        val dtos = jsonConfiguration.decodeFromString<List<McwsTrackDto>>(jsonStr)
        dtos.mapNotNull { it.toDomainTrack() }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

class McwsClient(
    val httpClient: HttpClient,
    private val activeServerFlow: StateFlow<McwsServerData?>,
) {
    private fun getActiveServer(): McwsServerData? = activeServerFlow.value

    fun getBaseUrl(): String? {
        val server = getActiveServer() ?: return null
        val host = server.host
        val scheme = if (server.useSsl) "https" else "http"
        val port = if (server.useSsl) server.sslPort else server.port
        return "$scheme://$host:$port/MCWS/v1"
    }

    fun buildImageUrl(fileKey: String): String {
        val base = getBaseUrl() ?: return ""
        val server = getActiveServer() ?: return ""
        val token = server.token ?: return ""
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
        val server = getActiveServer() ?: return null
        val token = server.token ?: return null

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
        val server = getActiveServer()
        val token = server?.token

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

    suspend fun searchTracks(query: String): List<Track> {
        val json = getMcwsJson("Files/Search", mapOf("Query" to query, "Fields" to "Calculated"))
        return parseMcwsTracksJson(json)
    }

    suspend fun getBrowseFiles(nodeId: String): List<Track> {
        val json = getMcwsJson("Browse/Files", mapOf("Fields" to "Calculated", "ID" to nodeId))
        return parseMcwsTracksJson(json)
    }

    suspend fun getRemoteQueue(): List<Track> {
        val json = getMcwsJson("Playback/Playlist", mapOf("Fields" to "Calculated"))
        return parseMcwsTracksJson(json)
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
                    list.add(Zone(id = id, name = name, guid = guid))
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

        val durationMs = items["DurationMS"]?.toLongOrNull() ?: 0L
        val positionMs = items["PositionMS"]?.toLongOrNull() ?: 0L
        val volumeVal = items["Volume"]?.toFloatOrNull() ?: 1.0f
        val volumeDisplay = items["VolumeDisplay"] ?: ""
        val isMuted = volumeDisplay.contains("mute", ignoreCase = true) || volumeVal == 0.0f

        val shuffleModeStr = items["Shuffle"]
        val shuffleMode = ShuffleMode.fromMcws(shuffleModeStr)

        val repeatModeStr = items["Repeat"]
        val repeatMode = RepeatMode.fromMcws(repeatModeStr)

        val trackAlbum = items["Album"]
        val trackArtist = items["Artist"]
        val trackName = items["Name"]
        val sampleRate = items["SampleRate"]?.toIntOrNull() ?: -1

        return PlayerStatus(
            zoneId = zoneId,
            zoneName = items["ZoneName"] ?: "Unknown Zone",
            state = playbackState,
            positionMs = positionMs,
            durationMs = durationMs,
            volume = volumeVal,
            isMuted = isMuted,
            shuffleMode = shuffleMode,
            repeatMode = repeatMode,
            playingNowPosition = items["PlayingNowPosition"]?.toIntOrNull() ?: -1,
            playingNowTracks = items["PlayingNowTracks"]?.toIntOrNull() ?: 0,
            trackAlbum = trackAlbum ?: "",
            trackArtist = trackArtist ?: "",
            trackName = trackName ?: "",
            sampleRate = sampleRate,
            trackFileKey = fileKey ?: ""
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
