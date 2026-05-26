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
data class PlayerStatus(
    val zoneId: String,
    val zoneName: String,
    val state: PlaybackState,
    val positionMs: Long,
    val durationMs: Long,
    val volume: Float,
    val isMuted: Boolean,
    val shuffleMode: ShuffleMode,
    val repeatMode: RepeatMode,
    val playingNowPosition: Int,
    val playingNowTracks: Int,
    val trackAlbum: String,
    val trackArtist: String,
    val trackName: String,
    val sampleRate: Int
)

@Serializable
data class Zone(
    val id: String,
    val name: String,
    val guid: String
) {
    val isDLNA: Boolean
        get() = !isLocal && !isOffline && !isAndroidAuto
    val isLocal: Boolean
        get() = id == "local"
    val isOffline: Boolean
        get() = id == "offline"
    val isAndroidAuto: Boolean
        get() = id == "android_auto"
    val isTransientZone: Boolean
        get() = isAndroidAuto

    companion object {
        val Local = Zone(id = "local", name = "Local Device", guid = "local_guid")
        val Offline = Zone(id = "offline", name = "Offline", guid = "offline_guid")
        val AndroidAuto = Zone(id = "android_auto", name = "Android Auto", guid = "android_auto_guid")
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
    val date: String,
    val genre: String,
    val durationMs: Long,
    val trackNumber: Int,
    val discNumber: Int,
    val totalDiscs: Int,
    val totalTracks: Int,
    val bitrate: Int,
    val bitDepth: Int,
    val sampleRate: Int,
    val channels: Int,
    val fileType: String,
    val filePath: String,
    val folderPath: String
) {
    val parentFolderPath: String
        get() = parentPath(folderPath)

    companion object {
        fun parentPath(path: String): String {
            if (path.isEmpty()) return ""

            // Strip trailing separator if present
            val trimmed = if (path.endsWith('\\') || path.endsWith('/')) {
                path.substring(0, path.length - 1)
            } else {
                path
            }

            if (trimmed.isEmpty()) return path
            if (trimmed.endsWith(':')) return path

            val lastBackslash = trimmed.lastIndexOf('\\')
            val lastSlash = trimmed.lastIndexOf('/')
            val sep = if (lastBackslash > lastSlash) lastBackslash else lastSlash

            return if (sep >= 0) trimmed.substring(0, sep + 1) else ""
        }

    }

    val albumGroupId: String
        get() = if (totalDiscs > 1 && discNumber > 0) {
            "${album.lowercase()}|${parentFolderPath.lowercase()}"
        } else {
            "${album.lowercase()}|${folderPath.lowercase()}"
        }

    //TODO: implement later
    val imageUrl = ""
}

@Serializable
data class Album(
    val name: String,
    val albumArtist: String,
    val folderPath: String,
    val parentFolderPath: String,
    val date: String,
    val artworkFileKey: String,
    val totalDiscs: Int,
    val discNumber: Int,
) {
    constructor(track: Track) : this(
        name = track.album,
        albumArtist = track.albumArtist,
        folderPath = track.folderPath,
        parentFolderPath = track.parentFolderPath,
        date = track.date,
        artworkFileKey = track.fileKey,
        totalDiscs = track.totalDiscs,
        discNumber = track.discNumber
    )
}
