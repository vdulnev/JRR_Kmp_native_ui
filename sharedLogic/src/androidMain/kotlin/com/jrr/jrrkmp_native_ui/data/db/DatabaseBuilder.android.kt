package com.jrr.jrrkmp_native_ui.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import co.touchlab.kermit.Logger

private val log = Logger.withTag("db:Room")

actual class DatabaseBuilder(private val context: Context) {
    actual fun createBuilder(): RoomDatabase.Builder<JrrDatabase> {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath("jrr_database.db")
        log.d { "createBuilder(Android) path=${dbFile.absolutePath}" }
        return Room.databaseBuilder<JrrDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
    }
}
