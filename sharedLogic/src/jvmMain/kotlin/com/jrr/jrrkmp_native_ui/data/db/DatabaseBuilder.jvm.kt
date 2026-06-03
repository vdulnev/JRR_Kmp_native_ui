package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import co.touchlab.kermit.Logger
import java.io.File

private val log = Logger.withTag("db:Room")

actual class DatabaseBuilder {
    actual fun createBuilder(): RoomDatabase.Builder<JrrDatabase> {
        // Persist under the user's local app-data dir (Windows: %LOCALAPPDATA%),
        // falling back to the home directory on other JVM hosts.
        val baseDir = System.getenv("LOCALAPPDATA")
            ?: System.getProperty("user.home")
        val dbFile = File(baseDir, "jrr_database.db")
        log.d { "createBuilder(Desktop) path=${dbFile.absolutePath}" }
        return Room.databaseBuilder<JrrDatabase>(
            name = dbFile.absolutePath
        )
    }
}
