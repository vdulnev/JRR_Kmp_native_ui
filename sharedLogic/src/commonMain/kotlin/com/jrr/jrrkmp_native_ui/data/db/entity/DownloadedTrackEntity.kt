package com.jrr.jrrkmp_native_ui.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_tracks")
data class DownloadedTrackEntity(
    @PrimaryKey
    @ColumnInfo(name = "file_key")
    val fileKey: String,
    @ColumnInfo(name = "file_path")
    val filePath: String,
    val title: String,
    val artist: String,
    val album: String,
    @ColumnInfo(name = "duration_ms")
    val durationMs: Long,
    @ColumnInfo(name = "track_number")
    val trackNumber: Int? = null,
    val genre: String? = null,
    @ColumnInfo(name = "last_played_at")
    val lastPlayedAt: Long? = null
)
