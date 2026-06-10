package com.jrr.jrrkmp_native_ui.core.di

import android.content.Context
import androidx.core.content.edit
import co.touchlab.kermit.Logger
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

private val log = Logger.withTag("di:AppContainer")

/**
 * Application-scoped dependency container. Constructed once in
 * [JrrApplication.onCreate]; every long-lived service is a `lazy` property so
 * the construction order is implicit in property access.
 *
 * Activities/services reach the container via [Context.appContainer].
 */
class AppContainer(context: Context) {
    private val appContext: Context = context.applicationContext

    init {
        log.i { "constructing (Android)" }
    }

    val database: JrrDatabase by lazy {
        log.d { "lazy: database" }
        createDatabase(DatabaseBuilder(appContext).createBuilder())
    }

    private val mcwsCore: McwsCore by lazy {
        log.d { "lazy: mcwsCore" }
        McwsCore.create(database)
    }

    val mcwsClient: McwsClient get() = mcwsCore.mcwsClient
    val serverRepository: ServerRepository get() = mcwsCore.serverRepository

    /** Process-wide preferences store, shared by the facade + player handler. */
    private val prefs by lazy {
        appContext.getSharedPreferences("jrr_settings", Context.MODE_PRIVATE)
    }

    val localPlayerHandler: LocalPlayerHandler by lazy {
        log.d { "lazy: localPlayerHandler" }
        LocalPlayerHandler(
            context = appContext,
            checkLocalFileProvider = { fileKey ->
                runBlocking {
                    database.downloadedTrackDao().getTrack(fileKey)?.filePath
                }
            },
            // The facade is the single source of truth for stream + artwork URLs
            // (active server, quality, Channels=2). `facade` is `by lazy` and defined
            // below, so the lambdas resolve it at call time — no construction cycle.
            streamUrlProvider = { track -> facade.streamUrl(track.fileKey, playback = true) },
            artworkUrlProvider = { fileKey -> facade.artworkUrl(fileKey) },
        )
    }

    val facade: AudioPlayerFacade by lazy {
        log.d { "lazy: facade" }
        AudioPlayerFacade(
            database = database,
            localPlayerEngine = localPlayerHandler,
            mcwsClient = mcwsClient,
            serverRepository = serverRepository,
            saveLastActiveZoneId = { zoneId ->
                prefs.edit { putString("last_active_zone_id", zoneId) }
            },
            loadLastActiveZoneId = {
                prefs.getString("last_active_zone_id", null)
            },
            saveLocalAudioQuality = { quality ->
                prefs.edit { putString("local_audio_quality", quality) }
            },
            loadLocalAudioQuality = {
                prefs.getString("local_audio_quality", null)
            },
        )
    }

    val libraryRepository: LibraryRepository by lazy {
        log.d { "lazy: libraryRepository" }
        LibraryRepository(
            database = database,
            mcwsClient = mcwsClient,
            isOfflineProvider = { facade.activeZone.value == Zone.Offline || facade.currentServerHost.isNullOrEmpty() },
        ).apply {
            onDownloadQueued = { track, jobId ->
                val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.jrr.jrrkmp_native_ui.data.api.DownloadWorker>()
                    .setInputData(androidx.work.workDataOf(
                        "file_key" to track.fileKey,
                        "job_id" to jobId
                    ))
                    .build()
                androidx.work.WorkManager.getInstance(appContext).enqueue(workRequest)
            }
        }
    }
}

/** Convenience accessor: `context.appContainer.facade`. */
val Context.appContainer: AppContainer
    get() = (applicationContext as JrrApplication).container
