package com.jrr.jrrkmp_native_ui.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorites",
    // One favorite per (server, type, identifier) — lets merge/duplicate dedupe
    // via INSERT/UPDATE OR IGNORE.
    indices = [Index(value = ["server_id", "type", "identifier"], unique = true)],
)
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    /**
     * Identity of the real server this favorite belongs to (see
     * [SavedServerEntity.serverId]). Favorites are per real server: connecting
     * to a different server shows that server's favorites.
     */
    @ColumnInfo(name = "server_id")
    val serverId: String = "",
    val type: String,
    val identifier: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "added_at")
    val addedAt: Long
)
