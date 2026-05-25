package com.jrr.jrrkmp_native_ui.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download_jobs")
data class DownloadJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "file_key")
    val fileKey: String,
    val state: String,
    @ColumnInfo(name = "bytes_downloaded")
    val bytesDownloaded: Long,
    @ColumnInfo(name = "bytes_total")
    val bytesTotal: Long,
    @ColumnInfo(name = "enqueued_at")
    val enqueuedAt: Long,
    @ColumnInfo(name = "started_at")
    val startedAt: Long? = null,
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
    @ColumnInfo(name = "file_type")
    val fileType: String,
    val filePath: String,
    val folderPath: String
)
