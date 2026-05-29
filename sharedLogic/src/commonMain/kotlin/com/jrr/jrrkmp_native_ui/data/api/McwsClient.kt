package com.jrr.jrrkmp_native_ui.data.api

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.KtorLogBridge
import com.jrr.jrrkmp_native_ui.data.repository.McwsServerData
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.PlayerStatus
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.Track
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.URLBuilder
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json

private val log = Logger.withTag("net:Mcws")

private val jsonConfiguration = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    isLenient = true
}

/**
 * Build the shared MCWS HttpClient. Used both by [McwsClient] (for talking to
 * the active server) and by [com.jrr.jrrkmp_native_ui.data.repository.ServerRepository]
 * (for one-off auth / alive checks before any server is active).
 *
 * The [Logging] plugin is wired to [KtorLogBridge] so every HTTP call appears
 * under `net:Ktor` in the application log stream. Tokens in the query string
 * are redacted before logging.
 */
fun createMcwsHttpClient(): HttpClient = createPlatformHttpClient().config {
    install(ContentNegotiation) {
        json(jsonConfiguration)
    }
    install(Logging) {
        logger = KtorLogBridge
        level = LogLevel.HEADERS // URL + headers; bodies omitted to keep volume sane
        sanitizeHeader { name -> name.equals("Authorization", ignoreCase = true) }
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
        val tracks = dtos.mapNotNull { it.toDomainTrack() }
        if (tracks.isNotEmpty()) {
            log.d { "parseMcwsTracksJson: parsed ${tracks.size} tracks. Plays: ${tracks.take(10).map { "${it.name}=${it.numberPlays}" }}" }
        }
        tracks
    } catch (e: Exception) {
        log.e(e) { "parseMcwsTracksJson failed (len=${jsonStr.length})" }
        emptyList()
    }
}

data class BrowseItem(val key: String, val name: String)

/**
 * One entry on the library "browse" breadcrumb path. Replaces a
 * `Pair<String, String>` so Swift sees `[BrowseNode]` instead of
 * `[KotlinPair<AnyObject, AnyObject>]` and avoids unsafe casts.
 */
data class BrowseNode(val label: String, val nodeId: String)

class McwsClient(
    val httpClient: HttpClient,
    val activeServerFlow: StateFlow<McwsServerData?>,
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
                log.w { "getRaw: HTTP ${response.status.value} for ${url.redactUrlToken()}" }
                null
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            log.e(e) { "getRaw failed for ${url.redactUrlToken()}" }
            null
        }
    }

    private suspend fun getMcwsJson(
        endpoint: String,
        params: Map<String, String> = emptyMap()
    ): String? {
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

    private suspend fun getMcwsXml(
        endpoint: String,
        params: Map<String, String> = emptyMap()
    ): String? {
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
        log.d { "searchTracks(q='$query')" }
        val json = getMcwsJson("Files/Search", mapOf("Query" to query, "Fields" to "Calculated,Number Plays"))
        val tracks = parseMcwsTracksJson(json)
        log.d { "searchTracks → ${tracks.size} results" }
        return tracks
    }

    suspend fun getBrowseFiles(nodeId: String): List<Track> {
        log.d { "getBrowseFiles(nodeId=$nodeId)" }
        val json = getMcwsJson("Browse/Files", mapOf("Fields" to "Calculated,Number Plays", "ID" to nodeId))
        val tracks = parseMcwsTracksJson(json)
        log.d { "getBrowseFiles → ${tracks.size} tracks" }
        return tracks
    }

    suspend fun getRemoteQueue(): List<Track> {
        log.d { "getRemoteQueue()" }
        val json = getMcwsJson("Playback/Playlist", mapOf("Fields" to "Calculated,Number Plays"))
        val tracks = parseMcwsTracksJson(json)
        log.d { "getRemoteQueue → ${tracks.size} tracks" }
        return tracks
    }

    suspend fun getZones(): List<Zone> {
        log.d { "getZones()" }
        val xml = getMcwsXml("Playback/Zones") ?: return emptyList()
        val list = mutableListOf<Zone>()
        try {
            val parsed = parseMcwsResponse(xml)
            if (parsed.status == "OK") {
                val numZones = parsed.items["NumberZones"]?.toIntOrNull() ?: 0
                for (i in 0 until numZones) {
                    val id = parsed.items["ZoneID$i"] ?: continue
                    val name = parsed.items["ZoneName$i"] ?: "Zone $i"
                    val guid = parsed.items["ZoneGUID$i"] ?: ""
                    list.add(Zone(id = id, name = name, guid = guid))
                }
            } else {
                log.w { "getZones: MCWS responded status=${parsed.status}" }
            }
        } catch (e: Exception) {
            log.e(e) { "getZones: failed to parse response" }
        }
        log.d { "getZones → ${list.size} zones" }
        return list
    }

    suspend fun getBrowseChildren(parentId: String): List<BrowseItem> {
        log.d { "getBrowseChildren(parentId=$parentId)" }
        val xml = getMcwsXml(
            "Browse/Children",
            mapOf("ID" to parentId, "Version" to "1", "ErrorOnMissing" to "0")
        ) ?: return emptyList()
        return try {
            val parsed = parseMcwsResponse(xml)
            if (parsed.status == "OK") {
                parsed.items
            } else {
                log.w { "getBrowseChildren: MCWS responded status=${parsed.status}" }
                emptyMap()
            }
        } catch (e: Exception) {
            log.e(e) { "getBrowseChildren: failed to parse response parentId=$parentId" }
            emptyMap()
        }.toList().map { BrowseItem(key = it.second, name = it.first) }.sortedBy { it.name.lowercase() }
            .also { log.d { "getBrowseChildren → ${it.size} items" } }
    }

    suspend fun getPlaybackInfo(zoneId: String): PlayerStatus? {
        // Called from a 1Hz polling loop — log only on anomaly, not on every tick.
        val xml =
            getMcwsXml("Playback/Info", mapOf("Zone" to zoneId, "ZoneType" to "ID")) ?: return null
        val items = try {
            val parsed = parseMcwsResponse(xml)
            if (parsed.status == "OK") {
                parsed.items
            } else {
                log.w { "getPlaybackInfo: MCWS responded status=${parsed.status} zone=$zoneId" }
                null
            }
        } catch (e: Exception) {
            log.e(e) { "getPlaybackInfo: failed to parse response zone=$zoneId" }
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
        log.d { "executeCommand($endpoint) params=$params" }
        val xml = getMcwsXml(endpoint, params) ?: run {
            log.w { "executeCommand($endpoint): no response body" }
            return false
        }
        return try {
            val parsed = parseMcwsResponse(xml)
            val ok = parsed.status == "OK"
            if (!ok) log.w { "executeCommand($endpoint): MCWS responded status=${parsed.status}" }
            ok
        } catch (e: Exception) {
            log.e(e) { "executeCommand($endpoint): failed to parse response" }
            false
        }
    }
}

/**
 * Redact the `Token=…` query param value for safe logging. Independent of the
 * Ktor bridge so we can log raw URLs from `getRaw` / repository code without
 * leaking the bearer.
 *
 * Replaces the value with `xxxx` (not `***`) so the redacted URL remains
 * clickable in terminals / IDE log panes / markdown previews.
 */
private fun String.redactUrlToken(): String =
    replace(Regex("([?&]Token=)([^&\\s]+)"), "$1xxxx")
