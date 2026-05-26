package com.jrr.jrrkmp_native_ui

import android.content.Context
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.api.McwsCore
import com.jrr.jrrkmp_native_ui.data.db.DatabaseBuilder
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.createDatabase
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.playback.LocalPlayerHandler
import kotlinx.coroutines.runBlocking

object JrrDependencies {
    private var database: JrrDatabase? = null
    private var mcwsCore: McwsCore? = null
    private var localPlayerHandler: LocalPlayerHandler? = null
    private var facade: AudioPlayerFacade? = null
    private var libraryRepository: LibraryRepository? = null

    fun getDatabase(context: Context): JrrDatabase {
        return database ?: synchronized(this) {
            database ?: createDatabase(DatabaseBuilder(context.applicationContext).createBuilder()).also { database = it }
        }
    }

    private fun getMcwsCore(context: Context): McwsCore {
        return mcwsCore ?: synchronized(this) {
            mcwsCore ?: McwsCore.create(getDatabase(context)).also { mcwsCore = it }
        }
    }

    fun getMcwsClient(context: Context): McwsClient = getMcwsCore(context).mcwsClient

    fun getServerRepository(context: Context): ServerRepository = getMcwsCore(context).serverRepository

    fun getLocalPlayerHandler(context: Context): LocalPlayerHandler {
        return localPlayerHandler ?: synchronized(this) {
            localPlayerHandler ?: run {
                val db = getDatabase(context)
                val serverRepo = getServerRepository(context)
                LocalPlayerHandler(
                    context = context.applicationContext,
                    serverRepository = serverRepo,
                    checkLocalFileProvider = { fileKey ->
                        runBlocking {
                            db.downloadedTrackDao().getTrack(fileKey)?.filePath
                        }
                    }
                ).also { localPlayerHandler = it }
            }
        }
    }

    fun getAudioPlayerFacade(context: Context): AudioPlayerFacade {
        return facade ?: synchronized(this) {
            facade ?: run {
                val db = getDatabase(context)
                val engine = getLocalPlayerHandler(context)
                val serverRepo = getServerRepository(context)
                val mcws = getMcwsClient(context)
                val prefs = context.applicationContext.getSharedPreferences("jrr_settings", Context.MODE_PRIVATE)
                AudioPlayerFacade(
                    database = db,
                    localPlayerEngine = engine,
                    mcwsClient = mcws,
                    serverRepository = serverRepo,
                    saveLastActiveZoneId = { zoneId ->
                        prefs.edit().putString("last_active_zone_id", zoneId).apply()
                    },
                    loadLastActiveZoneId = {
                        prefs.getString("last_active_zone_id", null)
                    }
                ).also { facade = it }
            }
        }
    }

    fun getLibraryRepository(context: Context): LibraryRepository {
        return libraryRepository ?: synchronized(this) {
            libraryRepository ?: run {
                val db = getDatabase(context)
                val mcws = getMcwsClient(context)
                LibraryRepository(
                    database = db,
                    mcwsClient = mcws,
                    isOfflineProvider = {
                        getAudioPlayerFacade(context).activeZone.value == Zone.Offline
                    }
                ).also { libraryRepository = it }
            }
        }
    }
}
