package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

expect class DatabaseBuilder {
    fun createBuilder(): RoomDatabase.Builder<JrrDatabase>
}

fun createDatabase(builder: RoomDatabase.Builder<JrrDatabase>): JrrDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .fallbackToDestructiveMigration(true)
        .build()
}
