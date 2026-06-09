package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.jrr.jrrkmp_native_ui.data.db.entity.*

@Database(
    entities = [
        SavedServerEntity::class,
        FavoriteEntity::class,
        LocalQueueTrackEntity::class,
        LocalQueueStateEntity::class,
        DownloadedTrackEntity::class,
        DownloadJobEntity::class
    ],
    version = 7,
    exportSchema = false
)
@ConstructedBy(JrrDatabaseConstructor::class)
abstract class JrrDatabase : RoomDatabase() {

    abstract fun savedServerDao(): SavedServerDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun localQueueTrackDao(): LocalQueueTrackDao
    abstract fun localQueueStateDao(): LocalQueueStateDao
    abstract fun downloadedTrackDao(): DownloadedTrackDao
    abstract fun downloadJobDao(): DownloadJobDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT", "KotlinNoActualForExpect")
expect object JrrDatabaseConstructor : RoomDatabaseConstructor<JrrDatabase> {
    override fun initialize(): JrrDatabase
}
