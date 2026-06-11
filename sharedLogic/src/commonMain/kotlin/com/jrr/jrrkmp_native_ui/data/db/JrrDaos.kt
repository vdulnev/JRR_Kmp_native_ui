package com.jrr.jrrkmp_native_ui.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jrr.jrrkmp_native_ui.data.db.entity.*
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

    /** Point a connection profile at a real server identity. */
    @Query("UPDATE saved_servers SET server_id = :serverId WHERE id = :id")
    suspend fun setServerId(id: String, serverId: String)

    /** How many profiles still reference a server identity (0 = orphaned). */
    @Query("SELECT COUNT(*) FROM saved_servers WHERE server_id = :serverId")
    suspend fun countProfilesForServer(serverId: String): Int
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteEntity)

    @Delete
    suspend fun delete(favorite: FavoriteEntity)

    @Query("SELECT * FROM favorites WHERE server_id = :serverId ORDER BY added_at DESC")
    suspend fun getAllFavorites(serverId: String): List<FavoriteEntity>

    @Query("SELECT * FROM favorites WHERE server_id = :serverId ORDER BY added_at DESC")
    fun getAllFavoritesFlow(serverId: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE server_id = :serverId AND type = :type AND identifier = :identifier LIMIT 1")
    suspend fun getFavorite(serverId: String, type: String, identifier: String): FavoriteEntity?

    /** Re-key one server's favorites onto another (used when grouping). */
    @Query("UPDATE OR IGNORE favorites SET server_id = :target WHERE server_id = :source")
    suspend fun moveFavorites(source: String, target: String)

    /** Drop any favorites left on a now-orphaned server identity. */
    @Query("DELETE FROM favorites WHERE server_id = :serverId")
    suspend fun deleteFavoritesForServer(serverId: String)
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

    @Query("SELECT * FROM downloaded_tracks")
    fun getAllTracksFlow(): Flow<List<DownloadedTrackEntity>>

    @Query("DELETE FROM downloaded_tracks")
    suspend fun deleteAll()
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

    @Query("SELECT * FROM download_jobs ORDER BY enqueued_at ASC")
    fun getAllJobsFlow(): Flow<List<DownloadJobEntity>>

    /** Drop every job that hasn't finished — used when the server connection
     *  is cleared, so no download outlives the disconnect. */
    @Query("DELETE FROM download_jobs WHERE state IN ('QUEUED', 'DOWNLOADING')")
    suspend fun deleteUnfinishedJobs(): Int
}

