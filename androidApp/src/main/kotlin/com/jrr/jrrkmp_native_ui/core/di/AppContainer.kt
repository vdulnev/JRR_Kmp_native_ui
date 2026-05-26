package com.jrr.jrrkmp_native_ui.core.di

import android.content.Context
import com.jrr.jrrkmp_native_ui.JrrApplication
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

/**
 * Application-scoped dependency container. Constructed once in
 * [JrrApplication.onCreate]; every long-lived service is a `lazy` property so
 * the construction order is implicit in property access.
 *
 * Activities/services reach the container via [Context.appContainer].
 */
class AppContainer(context: Context) {
    private val appContext: Context = context.applicationContext

    val database: JrrDatabase by lazy {
        createDatabase(DatabaseBuilder(appContext).createBuilder())
    }

    private val mcwsCore: McwsCore by lazy { McwsCore.create(database) }

    val mcwsClient: McwsClient get() = mcwsCore.mcwsClient
    val serverRepository: ServerRepository get() = mcwsCore.serverRepository

    val localPlayerHandler: LocalPlayerHandler by lazy {
        LocalPlayerHandler(
            context = appContext,
            serverRepository = serverRepository,
            checkLocalFileProvider = { fileKey ->
                runBlocking {
                    database.downloadedTrackDao().getTrack(fileKey)?.filePath
                }
            }
        )
    }

    val facade: AudioPlayerFacade by lazy {
        val prefs = appContext.getSharedPreferences("jrr_settings", Context.MODE_PRIVATE)
        AudioPlayerFacade(
            database = database,
            localPlayerEngine = localPlayerHandler,
            mcwsClient = mcwsClient,
            serverRepository = serverRepository,
            saveLastActiveZoneId = { zoneId ->
                prefs.edit().putString("last_active_zone_id", zoneId).apply()
            },
            loadLastActiveZoneId = {
                prefs.getString("last_active_zone_id", null)
            },
        )
    }

    val libraryRepository: LibraryRepository by lazy {
        LibraryRepository(
            database = database,
            mcwsClient = mcwsClient,
            isOfflineProvider = { facade.activeZone.value == Zone.Offline },
        )
    }
}

/** Convenience accessor: `context.appContainer.facade`. */
val Context.appContainer: AppContainer
    get() = (applicationContext as JrrApplication).container
