package com.jrr.jrrkmp_native_ui.playback

import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.api.McwsXmlParser
import com.jrr.jrrkmp_native_ui.domain.model.PlaybackState
import com.jrr.jrrkmp_native_ui.domain.model.PlayerStatus
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode
import com.jrr.jrrkmp_native_ui.domain.model.TrackInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class McwsRemotePlayerHandler {

    private suspend fun executeGet(endpoint: String, params: Map<String, String>): Map<String, String>? = withContext(Dispatchers.IO) {
        val xml = McwsClient.getMcwsXml(endpoint, params) ?: return@withContext null
        return@withContext try {
            val parsed = McwsXmlParser.parseResponse(xml)
            if (parsed.status == "OK") parsed.items else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun play(zoneId: String) {
        executeGet("Playback/Play", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun pause(zoneId: String) {
        executeGet("Playback/Pause", mapOf("State" to "1", "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun resume(zoneId: String) {
        executeGet("Playback/Pause", mapOf("State" to "0", "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun stop(zoneId: String) {
        executeGet("Playback/Stop", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun next(zoneId: String) {
        executeGet("Playback/Next", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun previous(zoneId: String) {
        executeGet("Playback/Previous", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun seekTo(zoneId: String, positionMs: Long) {
        executeGet(
            "Playback/Position",
            mapOf("Position" to positionMs.toString(), "Mode" to "ms", "Zone" to zoneId, "ZoneType" to "ID")
        )
    }

    suspend fun setVolume(zoneId: String, level: Float) {
        executeGet("Playback/Volume", mapOf("Level" to level.toString(), "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun setMute(zoneId: String, mute: Boolean) {
        val setValue = if (mute) "1" else "0"
        executeGet("Playback/Mute", mapOf("Set" to setValue, "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun setShuffleMode(zoneId: String, mode: ShuffleMode) {
        executeGet("Playback/Shuffle", mapOf("Mode" to mode.mcwsMode, "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun setRepeatMode(zoneId: String, mode: RepeatMode) {
        executeGet("Playback/Repeat", mapOf("Mode" to mode.mcwsMode, "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun getPlaybackInfo(zoneId: String): PlayerStatus? {
        val items = executeGet("Playback/Info", mapOf("Zone" to zoneId, "ZoneType" to "ID")) ?: return null

        val stateVal = items["State"]?.toIntOrNull() ?: 0
        val playbackState = PlaybackState.fromMcws(stateVal)

        val fileKey = items["FileKey"]
        val trackInfo = if (!fileKey.isNullOrEmpty()) {
            val serverUrl = McwsClient.getBaseUrl() ?: ""
            val imageRelUrl = items["ImageURL"] ?: ""
            var imageUrl = if (imageRelUrl.isNotEmpty()) {
                val base = serverUrl.removeSuffix("/MCWS/v1").removeSuffix("/MCWS/v1/")
                if (imageRelUrl.startsWith("http")) imageRelUrl else "${base}/${imageRelUrl.removePrefix("/")}"
            } else ""

            val token = McwsClient.currentToken
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
}
