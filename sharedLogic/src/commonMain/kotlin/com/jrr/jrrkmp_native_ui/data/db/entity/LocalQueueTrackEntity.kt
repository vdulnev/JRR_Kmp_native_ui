package com.jrr.jrrkmp_native_ui.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_queue_tracks")
data class LocalQueueTrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "zone_id")
    val zoneId: String,
    @ColumnInfo(name = "file_key")
    val fileKey: String,
    @ColumnInfo(name = "track_json")
    val trackJson: String,
    val position: Int
)
