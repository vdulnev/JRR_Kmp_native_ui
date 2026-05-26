package com.jrr.jrrkmp_native_ui.playback

import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.domain.model.PlayerStatus
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode

class McwsRemotePlayerHandler(
    private val mcwsClient: McwsClient,
) {

    suspend fun play(zoneId: String) {
        mcwsClient.executeCommand("Playback/Play", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun pause(zoneId: String) {
        mcwsClient.executeCommand("Playback/Pause", mapOf("State" to "1", "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun resume(zoneId: String) {
        mcwsClient.executeCommand("Playback/Pause", mapOf("State" to "0", "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun stop(zoneId: String) {
        mcwsClient.executeCommand("Playback/Stop", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun next(zoneId: String) {
        mcwsClient.executeCommand("Playback/Next", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun previous(zoneId: String) {
        mcwsClient.executeCommand("Playback/Previous", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun seekTo(zoneId: String, positionMs: Long) {
        mcwsClient.executeCommand(
            "Playback/Position",
            mapOf("Position" to positionMs.toString(), "Mode" to "ms", "Zone" to zoneId, "ZoneType" to "ID")
        )
    }

    suspend fun setVolume(zoneId: String, level: Float) {
        mcwsClient.executeCommand("Playback/Volume", mapOf("Level" to level.toString(), "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun setMute(zoneId: String, mute: Boolean) {
        val setValue = if (mute) "1" else "0"
        mcwsClient.executeCommand("Playback/Mute", mapOf("Set" to setValue, "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun setShuffleMode(zoneId: String, mode: ShuffleMode) {
        mcwsClient.executeCommand("Playback/Shuffle", mapOf("Mode" to mode.mcwsMode, "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun setRepeatMode(zoneId: String, mode: RepeatMode) {
        mcwsClient.executeCommand("Playback/Repeat", mapOf("Mode" to mode.mcwsMode, "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun getPlaybackInfo(zoneId: String): PlayerStatus? {
        return mcwsClient.getPlaybackInfo(zoneId)
    }
}
