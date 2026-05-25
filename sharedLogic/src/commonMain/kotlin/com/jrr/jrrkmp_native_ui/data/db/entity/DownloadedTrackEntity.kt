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
    val name: String,
    val artist: String,
    val album: String,
    val albumArtist: String,
    val date: String,
    val genre: String,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "track_number")
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
    fun toTrack(): Track {
        return Track(
            fileKey = fileKey,
            name = name,
            artist = artist,
            album = album,
            albumArtist = albumArtist,
            date = date,
            genre = genre,
            durationMs = durationMs,
            trackNumber = trackNumber,
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
