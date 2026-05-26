package com.jrr.jrrkmp_native_ui

import android.content.Context
import com.jrr.jrrkmp_native_ui.data.db.DatabaseBuilder
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.createDatabase
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.playback.LocalPlayerHandler
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import kotlinx.coroutines.runBlocking

object JrrDependencies {
    private var database: JrrDatabase? = null
    private var localPlayerHandler: LocalPlayerHandler? = null
    private var facade: AudioPlayerFacade? = null
    private var serverRepository: ServerRepository? = null
    private var libraryRepository: LibraryRepository? = null

    fun getDatabase(context: Context): JrrDatabase {
        return database ?: synchronized(this) {
            database ?: createDatabase(DatabaseBuilder(context.applicationContext).createBuilder()).also { database = it }
        }
    }

    fun getLocalPlayerHandler(context: Context): LocalPlayerHandler {
        return localPlayerHandler ?: synchronized(this) {
            localPlayerHandler ?: run {
                val db = getDatabase(context)
                LocalPlayerHandler(
                    context = context.applicationContext,
                    serverUrlProvider = {
                        runBlocking {
                            val activeServer = db.savedServerDao().getLastUsedServer()
                            if (activeServer != null) {
                                val host = activeServer.host
                                val scheme = if (activeServer.useSsl) "https" else "http"
                                val port = if (activeServer.useSsl) activeServer.sslPort else activeServer.port
                                "$scheme://$host:$port/MCWS/v1"
                            } else null
                        }
                    },
                    tokenProvider = {
                        runBlocking {
                            db.savedServerDao().getLastUsedServer()?.authToken
                        }
                    },
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
                val prefs = context.applicationContext.getSharedPreferences("jrr_settings", Context.MODE_PRIVATE)
                AudioPlayerFacade(
                    database = db,
                    localPlayerEngine = engine,
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

    fun getServerRepository(context: Context): ServerRepository {
        return serverRepository ?: synchronized(this) {
            serverRepository ?: ServerRepository(getDatabase(context)).also {
                serverRepository = it
                McwsClient.initialize(it.activeServer)
            }
        }
    }

    fun getLibraryRepository(context: Context): LibraryRepository {
        return libraryRepository ?: synchronized(this) {
            libraryRepository ?: run {
                val db = getDatabase(context)
                LibraryRepository(
                    database = db,
                    isOfflineProvider = {
                        getAudioPlayerFacade(context).activeZone.value == Zone.Offline
                    }
                ).also { libraryRepository = it }
            }
        }
    }
}
