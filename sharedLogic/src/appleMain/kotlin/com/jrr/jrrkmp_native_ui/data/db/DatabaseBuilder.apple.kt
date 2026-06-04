package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.Room
import androidx.room.RoomDatabase
import co.touchlab.kermit.Logger
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.OsFamily
import kotlin.native.Platform
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

private val log = Logger.withTag("db:Room")

actual class DatabaseBuilder {
    @OptIn(ExperimentalNativeApi::class)
    actual fun createBuilder(): RoomDatabase.Builder<JrrDatabase> {
        // tvOS does not permit POSIX file locking (flock → EPERM) inside the
        // app's Documents container, which crashes Room's FileLock on a real
        // Apple TV (the simulator runs in a permissive macOS sandbox, so it
        // only reproduces on device). Apple's storage model for tvOS also
        // designates Library/Caches — not Documents — as the writable location
        // for app data. iOS/macOS keep using Documents for durable storage.
        val isTvOs = Platform.osFamily == OsFamily.TVOS
        val searchPath = if (isTvOs) NSCachesDirectory else NSDocumentDirectory
        val baseDirectory = NSSearchPathForDirectoriesInDomains(
            searchPath,
            NSUserDomainMask,
            true,
        ).firstOrNull() as? String ?: NSHomeDirectory()
        val dbFilePath = "$baseDirectory/jrr_database.db"
        log.d { "createBuilder(apple, tvOS=$isTvOs) path=$dbFilePath" }
        return Room.databaseBuilder<JrrDatabase>(
            name = dbFilePath,
        )
    }
}
