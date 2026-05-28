package com.jrr.jrrkmp_native_ui.playback

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.domain.model.PlayerStatus
import com.jrr.jrrkmp_native_ui.domain.model.RepeatMode
import com.jrr.jrrkmp_native_ui.domain.model.ShuffleMode

private val log = Logger.withTag("playback:Remote")

class McwsRemotePlayerHandler(
    private val mcwsClient: McwsClient,
) {

    suspend fun play(zoneId: String) {
        log.d { "play(zone=$zoneId)" }
        mcwsClient.executeCommand("Playback/Play", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun pause(zoneId: String) {
        log.d { "pause(zone=$zoneId)" }
        mcwsClient.executeCommand("Playback/Pause", mapOf("State" to "1", "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun resume(zoneId: String) {
        log.d { "resume(zone=$zoneId)" }
        mcwsClient.executeCommand("Playback/Pause", mapOf("State" to "0", "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun stop(zoneId: String) {
        log.d { "stop(zone=$zoneId)" }
        mcwsClient.executeCommand("Playback/Stop", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun next(zoneId: String) {
        log.d { "next(zone=$zoneId)" }
        mcwsClient.executeCommand("Playback/Next", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun previous(zoneId: String) {
        log.d { "previous(zone=$zoneId)" }
        mcwsClient.executeCommand("Playback/Previous", mapOf("Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun seekTo(zoneId: String, positionMs: Long) {
        log.d { "seekTo(zone=$zoneId pos=${positionMs}ms)" }
        mcwsClient.executeCommand(
            "Playback/Position",
            mapOf("Position" to positionMs.toString(), "Mode" to "ms", "Zone" to zoneId, "ZoneType" to "ID")
        )
    }

    suspend fun setVolume(zoneId: String, level: Float) {
        log.d { "setVolume(zone=$zoneId level=$level)" }
        mcwsClient.executeCommand("Playback/Volume", mapOf("Level" to level.toString(), "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun setMute(zoneId: String, mute: Boolean) {
        log.d { "setMute(zone=$zoneId mute=$mute)" }
        val setValue = if (mute) "1" else "0"
        mcwsClient.executeCommand("Playback/Mute", mapOf("Set" to setValue, "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun setShuffleMode(zoneId: String, mode: ShuffleMode) {
        log.d { "setShuffleMode(zone=$zoneId mode=$mode)" }
        mcwsClient.executeCommand("Playback/Shuffle", mapOf("Mode" to mode.mcwsMode, "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun setRepeatMode(zoneId: String, mode: RepeatMode) {
        log.d { "setRepeatMode(zone=$zoneId mode=$mode)" }
        mcwsClient.executeCommand("Playback/Repeat", mapOf("Mode" to mode.mcwsMode, "Zone" to zoneId, "ZoneType" to "ID"))
    }

    suspend fun getPlaybackInfo(zoneId: String): PlayerStatus? {
        // Called from a 1Hz polling loop — log at Verbose only.
        return mcwsClient.getPlaybackInfo(zoneId)
    }
}
