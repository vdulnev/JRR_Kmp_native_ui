package com.jrr.jrrkmp_native_ui.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_queue_state")
data class LocalQueueStateEntity(
    @PrimaryKey
    @ColumnInfo(name = "zone_id")
    val zoneId: String,
    @ColumnInfo(name = "current_index")
    val currentIndex: Int = -1
)
