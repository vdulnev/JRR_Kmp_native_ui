package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.core.logging.LoggingSQLiteDriver

private val log = Logger.withTag("db:Room")

expect class DatabaseBuilder {
    fun createBuilder(): RoomDatabase.Builder<JrrDatabase>
}

/**
 * v8 → v9: add `artist_info_cache`.
 *
 * Written out rather than left to [RoomDatabase.Builder.fallbackToDestructiveMigration]
 * because that fallback drops the whole file — saved servers, favorites, the
 * local queues and the downloaded-track index would all go with it, just to add
 * one cache table. The statement is copied verbatim from Room's generated
 * `createAllTables`, so the schema hash matches and Room's post-migration
 * validation passes.
 */
private val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `artist_info_cache` " +
                "(`artist_key` TEXT NOT NULL, `provider` TEXT NOT NULL, " +
                "`artist_name` TEXT NOT NULL, `info_json` TEXT NOT NULL, " +
                "`fetched_at` INTEGER NOT NULL, PRIMARY KEY(`artist_key`, `provider`))",
        )
    }
}

fun createDatabase(builder: RoomDatabase.Builder<JrrDatabase>): JrrDatabase {
    log.i { "createDatabase()" }
    return builder
        .setDriver(LoggingSQLiteDriver(BundledSQLiteDriver()))
        .addMigrations(MIGRATION_8_9)
        // Still the last resort for any version pair without a written path.
        .fallbackToDestructiveMigration(true)
        .build()
        .also { log.i { "database ready" } }
}
