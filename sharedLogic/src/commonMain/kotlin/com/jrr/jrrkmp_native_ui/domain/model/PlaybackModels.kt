package com.jrr.jrrkmp_native_ui.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlaybackState(val mcwsValue: Int) {
    STOPPED(0),
    PAUSED(1),
    PLAYING(2);

    companion object {
        fun fromMcws(value: Int): PlaybackState {
            return entries.firstOrNull { it.mcwsValue == value } ?: STOPPED
        }
    }
}

@Serializable
enum class ShuffleMode(val mcwsMode: String) {
    OFF("Off"),
    ON("On"),
    AUTOMATIC("Automatic");

    companion object {
        fun fromMcws(value: String?): ShuffleMode {
            if (value == null) return OFF
            return entries.firstOrNull { it.mcwsMode.equals(value, ignoreCase = true) } ?: OFF
        }
    }
}

@Serializable
enum class RepeatMode(val mcwsMode: String) {
    OFF("Off"),
    PLAYLIST("Playlist"),
    TRACK("Track");

    companion object {
        fun fromMcws(value: String?): RepeatMode {
            if (value == null) return OFF
            return entries.firstOrNull { it.mcwsMode.equals(value, ignoreCase = true) } ?: OFF
        }
    }
}

@Serializable
data class TrackInfo(
    val fileKey: String,
    val name: String,
    val artist: String,
    val album: String,
    val imageUrl: String,
    val bitrate: Int = 0,
    val bitDepth: Int = 0,
    val sampleRate: Int = 0,
    val channels: Int = 0,
    val durationMs: Long = 0
)

@Serializable
data class PlayerStatus(
    val zoneId: String,
    val zoneName: String,
    val state: PlaybackState,
    val trackInfo: TrackInfo?,
    val positionMs: Long,
    val durationMs: Long,
    val volume: Float,
    val isMuted: Boolean,
    val shuffleMode: ShuffleMode,
    val repeatMode: RepeatMode,
    val playingNowPosition: Int,
    val playingNowTracks: Int
)

@Serializable
data class Zone(
    val id: String,
    val name: String,
    val guid: String,
    val isDLNA: Boolean = false,
    val isLocal: Boolean = false,
    val isOffline: Boolean = false,
    val isAndroidAuto: Boolean = false
) {
    companion object {
        val Local = Zone(id = "local", name = "Local Device", guid = "local_guid", isLocal = true)
        val Offline = Zone(id = "offline", name = "Offline", guid = "offline_guid", isOffline = true)
        val AndroidAuto = Zone(id = "android_auto", name = "Android Auto", guid = "android_auto_guid", isAndroidAuto = true)
    }
}

@Serializable
data class ServerInfo(
    val id: String,
    val name: String,
    val version: String,
    val platform: String,
    val address: String
)

@Serializable
data class PlayingNowItem(
    val index: Int,
    val fileKey: String,
    val name: String,
    val artist: String,
    val album: String
)

@Serializable
data class Track(
    val fileKey: String,
    val name: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val genre: String,
    val durationMs: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val totalDiscs: Int,
    val totalTracks: Int,
    val imageUrl: String,
    val bitrate: Int,
    val bitDepth: Int,
    val sampleRate: Int,
    val channels: Int,
    val fileType: String,
    val filePath: String
) {
    fun toTrackInfo(): TrackInfo {
        return TrackInfo(
            fileKey = fileKey,
            name = name,
            artist = artist,
            album = album,
            imageUrl = imageUrl,
            bitrate = bitrate,
            bitDepth = bitDepth,
            sampleRate = sampleRate,
            channels = channels,
            durationMs = durationMs
        )
    }
}

@Serializable
data class Album(
    val name: String,
    val artist: String,
    val folderPath: String = "",
    val year: String = "",
    val imageUrl: String = ""
)
