package com.jrr.jrrkmp_native_ui.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.jrr.jrrkmp_native_ui.domain.model.Track

@Entity(tableName = "downloaded_tracks")
data class DownloadedTrackEntity(
    @PrimaryKey
    @ColumnInfo(name = "file_key")
    val fileKey: String,
    @ColumnInfo(name = "file_path")
    val title: String,
    val artist: String,
    val album: String,
    val date: String,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "track_number")
    val trackNumber: Int,
    val genre: String,
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
    fun toTrack(): Track {
        return Track(
            fileKey = fileKey,
            name = title,
            artist = artist,
            album = album,
            albumArtist = artist,
            date = date,
            genre = genre ?: "Unknown",
            durationMs = durationMs,
            trackNumber = trackNumber ?: 0,
            discNumber = discNumber,
            totalDiscs = totalDiscs,
            totalTracks = totalTracks,
            bitrate = bitrate,
            bitDepth = bitDepth,
            sampleRate = sampleRate,
            channels = channels,
            fileType = fileType,
            filePath = filePath,
            folderPath = folderPath
        )
    }
}
