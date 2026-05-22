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
    @ColumnInfo(name = "last_used_at")
    val lastUsedAt: Long? = null,
    @ColumnInfo(name = "auth_token")
    val authToken: String? = null,
    @ColumnInfo(name = "use_ssl")
    val useSsl: Boolean = false,
    @ColumnInfo(name = "ssl_port")
    val sslPort: Int = 52200
)
