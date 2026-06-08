package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import co.touchlab.kermit.Logger
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.OsFamily
import kotlin.native.Platform
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

private val log = Logger.withTag("db:Room")

actual class DatabaseBuilder {
    @OptIn(ExperimentalNativeApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)
    actual fun createBuilder(): RoomDatabase.Builder<JrrDatabase> {
        // tvOS does not permit POSIX file locking (flock → EPERM) inside the
        // app's Documents container, which crashes Room's FileLock on a real
        // Apple TV (the simulator runs in a permissive macOS sandbox, so it
        // only reproduces on device). Apple's storage model for tvOS also
        // designates Library/Caches — not Documents — as the writable location
        // for app data. iOS/macOS keep using Documents for durable storage.
        val isTvOs = Platform.osFamily == OsFamily.TVOS
        val searchPath = if (isTvOs) NSCachesDirectory else NSLibraryDirectory
        val baseDirectory = NSSearchPathForDirectoriesInDomains(
            searchPath,
            NSUserDomainMask,
            true,
        ).firstOrNull() as? String ?: NSHomeDirectory()
        val dbFilePath = "$baseDirectory/jrr_database.db"
        log.d { "createBuilder(apple, tvOS=$isTvOs) path=$dbFilePath" }

        // Migrate from old Documents path if it exists (only relevant for non-tvOS)
        if (!isTvOs) {
            val fileManager = NSFileManager.defaultManager
            val documentsDir = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true,
            ).firstOrNull() as? String ?: NSHomeDirectory()
            val oldDbFilePath = "$documentsDir/jrr_database.db"

            if (fileManager.fileExistsAtPath(oldDbFilePath)) {
                log.i { "Migrating database from Documents container to Library container: $dbFilePath" }
                try {
                    if (!fileManager.moveItemAtPath(oldDbFilePath, dbFilePath, null)) {
                        log.e { "Failed to move database file from $oldDbFilePath to $dbFilePath" }
                    } else {
                        // Move sqlite helper files if they exist
                        val shmOld = "$oldDbFilePath-shm"
                        val shmNew = "$dbFilePath-shm"
                        if (fileManager.fileExistsAtPath(shmOld)) {
                            fileManager.moveItemAtPath(shmOld, shmNew, null)
                        }
                        val walOld = "$oldDbFilePath-wal"
                        val walNew = "$dbFilePath-wal"
                        if (fileManager.fileExistsAtPath(walOld)) {
                            fileManager.moveItemAtPath(walOld, walNew, null)
                        }
                        log.i { "Database migration from Documents to Library completed successfully." }
                    }
                } catch (e: Exception) {
                    log.e(e) { "Error during database migration" }
                }
            }
        }

        return Room.databaseBuilder<JrrDatabase>(
            name = dbFilePath,
        )
    }
}
