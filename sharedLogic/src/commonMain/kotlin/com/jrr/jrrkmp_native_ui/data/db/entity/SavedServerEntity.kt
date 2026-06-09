package com.jrr.jrrkmp_native_ui.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_servers")
data class SavedServerEntity(
    @PrimaryKey
    val id: String,
    val host: String,
    val port: Int = 52199,
    val username: String,
    @ColumnInfo(name = "password_key")
    val passwordKey: String,
    @ColumnInfo(name = "friendly_name")
    val friendlyName: String? = null,
    /** Epoch-ms of last successful connect. `0L` = never used. */
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long = 0L,
    @ColumnInfo(name = "auth_token")
    val authToken: String? = null,
    @ColumnInfo(name = "use_ssl")
    val useSsl: Boolean = false,
    @ColumnInfo(name = "ssl_port")
    val sslPort: Int = 52200,
    /**
     * Stable identity of the *real* server this connection profile points at.
     * Profiles that share a `serverId` are different ip/port settings for the
     * same physical server; a profile with its own unique `serverId` is, by
     * definition, a distinct server. This is the key favorites are scoped by.
     */
    @ColumnInfo(name = "server_id")
    val serverId: String = ""
)
