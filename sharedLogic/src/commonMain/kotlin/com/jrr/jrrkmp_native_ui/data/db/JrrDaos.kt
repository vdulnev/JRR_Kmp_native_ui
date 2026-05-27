package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jrr.jrrkmp_native_ui.data.db.entity.*
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedServerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(server: SavedServerEntity)

    @Update
    suspend fun update(server: SavedServerEntity)

    @Delete
    suspend fun delete(server: SavedServerEntity)

    @Query("SELECT * FROM saved_servers WHERE id = :id")
    suspend fun getServerById(id: String): SavedServerEntity?

    @Query("SELECT * FROM saved_servers ORDER BY last_used_at DESC")
    suspend fun getAllServers(): List<SavedServerEntity>

    @Query("SELECT * FROM saved_servers ORDER BY last_used_at DESC LIMIT 1")
    suspend fun getLastUsedServer(): SavedServerEntity?
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites ORDER BY added_at DESC")
    suspend fun getAllFavorites(): List<FavoriteEntity>

    @Query("SELECT * FROM favorites WHERE type = :type AND identifier = :identifier LIMIT 1")
    suspend fun getFavorite(type: String, identifier: String): FavoriteEntity?
}

@Dao
interface LocalQueueTrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: LocalQueueTrackEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<LocalQueueTrackEntity>)

    @Query("SELECT * FROM local_queue_tracks WHERE zone_id = :zoneId ORDER BY position ASC")
    suspend fun getTracksForZone(zoneId: String): List<LocalQueueTrackEntity>

    @Query("DELETE FROM local_queue_tracks WHERE zone_id = :zoneId")
    suspend fun clearQueueForZone(zoneId: String)

    @Delete
    suspend fun delete(track: LocalQueueTrackEntity)
}

@Dao
interface LocalQueueStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(state: LocalQueueStateEntity)

    @Query("SELECT * FROM local_queue_state WHERE zone_id = :zoneId LIMIT 1")
    suspend fun getStateForZone(zoneId: String): LocalQueueStateEntity?

    @Query("DELETE FROM local_queue_state WHERE zone_id = :zoneId")
    suspend fun deleteStateForZone(zoneId: String)
}

@Dao
interface DownloadedTrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: DownloadedTrackEntity)

    @Delete
    suspend fun delete(track: DownloadedTrackEntity)

    @Query("SELECT * FROM downloaded_tracks WHERE file_key = :fileKey LIMIT 1")
    suspend fun getTrack(fileKey: String): DownloadedTrackEntity?

    @Query("SELECT * FROM downloaded_tracks")
    suspend fun getAllTracks(): List<DownloadedTrackEntity>

    @NativeCoroutines
    @Query("SELECT * FROM downloaded_tracks")
    fun getAllTracksFlow(): Flow<List<DownloadedTrackEntity>>
}

@Dao
interface DownloadJobDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(job: DownloadJobEntity): Long

    @Update
    suspend fun update(job: DownloadJobEntity)

    @Delete
    suspend fun delete(job: DownloadJobEntity)

    @Query("SELECT * FROM download_jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Int): DownloadJobEntity?

    @Query("SELECT * FROM download_jobs WHERE state = :state ORDER BY enqueued_at ASC")
    suspend fun getJobsByState(state: String): List<DownloadJobEntity>

    @Query("SELECT * FROM download_jobs ORDER BY enqueued_at ASC")
    suspend fun getAllJobs(): List<DownloadJobEntity>

    @NativeCoroutines
    @Query("SELECT * FROM download_jobs ORDER BY enqueued_at ASC")
    fun getAllJobsFlow(): Flow<List<DownloadJobEntity>>
}

