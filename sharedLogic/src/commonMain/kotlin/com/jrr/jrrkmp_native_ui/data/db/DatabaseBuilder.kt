package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.LoggingSQLiteDriver

private val log = Logger.withTag("db:Room")

expect class DatabaseBuilder {
    fun createBuilder(): RoomDatabase.Builder<JrrDatabase>
}

fun createDatabase(builder: RoomDatabase.Builder<JrrDatabase>): JrrDatabase {
    log.i { "createDatabase()" }
    return builder
        .setDriver(LoggingSQLiteDriver(BundledSQLiteDriver()))
        .fallbackToDestructiveMigration(true)
        .build()
        .also { log.i { "database ready" } }
}
