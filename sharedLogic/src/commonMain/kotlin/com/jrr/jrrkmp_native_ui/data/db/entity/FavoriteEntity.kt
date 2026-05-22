package com.jrr.jrrkmp_native_ui.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String,
    val identifier: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "added_at")
    val addedAt: Long
)
