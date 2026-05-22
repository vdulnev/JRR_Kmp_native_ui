package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual class DatabaseBuilder {
    actual fun createBuilder(): RoomDatabase.Builder<JrrDatabase> {
        val documentDirectory = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: NSHomeDirectory()
        val dbFilePath = "$documentDirectory/jrr_database.db"
        return Room.databaseBuilder<JrrDatabase>(
            name = dbFilePath
        )
    }
}
