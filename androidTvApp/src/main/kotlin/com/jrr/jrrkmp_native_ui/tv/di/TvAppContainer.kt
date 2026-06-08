package com.jrr.jrrkmp_native_ui.tv.di

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.core.content.edit
import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.api.McwsCore
import com.jrr.jrrkmp_native_ui.data.db.DatabaseBuilder
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.createDatabase
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.domain.model.LocalAudioQuality
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.playback.LocalPlayerHandler
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.LibraryViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.NowPlayingViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.QueueViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.SettingsViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.TvConnectViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.TvLibraryViewModel
import com.jrr.jrrkmp_native_ui.presentation.viewmodel.ZonesViewModel

private val log = Logger.withTag("di:TvAppContainer")

/**
 * Application-scoped DI container for the Android TV app. Online-only: there is
 * no offline mode, no downloads, and no WorkManager. Mirrors the tvOS
 * `TvContainer` — same shared building blocks (`McwsCore`, `LibraryRepository`,
 * `LocalPlayerHandler`, `AudioPlayerFacade`), wired for an always-online TV.
 */
class TvAppContainer(context: Context) {
    private val appContext: Context = context.applicationContext

    init {
        log.i { "constructing (Android TV)" }
    }

    val database: JrrDatabase by lazy {
        log.d { "lazy: database" }
        createDatabase(DatabaseBuilder(appContext).createBuilder())
    }

    private val mcwsCore: McwsCore by lazy { McwsCore.create(database) }
    val mcwsClient: McwsClient get() = mcwsCore.mcwsClient
    val serverRepository: ServerRepository get() = mcwsCore.serverRepository

    private val prefs by lazy {
        appContext.getSharedPreferences("jrr_settings", Context.MODE_PRIVATE)
    }

    private val localPlayerHandler: LocalPlayerHandler by lazy {
        log.d { "lazy: localPlayerHandler" }
        LocalPlayerHandler(
            context = appContext,
            serverRepository = serverRepository,
            // Online-only: there are no on-device downloads, so the local engine
            // always streams from the server.
            checkLocalFileProvider = { null },
            localAudioQualityProvider = {
                LocalAudioQuality.fromName(prefs.getString("local_audio_quality", null))
            },
        )
    }

    val facade: AudioPlayerFacade by lazy {
        log.d { "lazy: facade" }
        AudioPlayerFacade(
            database = database,
            localPlayerEngine = localPlayerHandler,
            mcwsClient = mcwsClient,
            serverRepository = serverRepository,
            saveLastActiveZoneId = { zoneId -> prefs.edit { putString("last_active_zone_id", zoneId) } },
            loadLastActiveZoneId = { prefs.getString("last_active_zone_id", null) },
            saveLocalAudioQuality = { quality -> prefs.edit { putString("local_audio_quality", quality) } },
            loadLocalAudioQuality = { prefs.getString("local_audio_quality", null) },
        )
    }

    val libraryRepository: LibraryRepository by lazy {
        log.d { "lazy: libraryRepository" }
        LibraryRepository(
            database = database,
            mcwsClient = mcwsClient,
            // tvOS-style: always online.
            isOfflineProvider = { false },
        )
    }

    val isDebugBuild: Boolean =
        (appContext.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

    // --- ViewModel factories (constructed per-screen, reused across phases) ---

    fun makeLibraryViewModel(): LibraryViewModel = LibraryViewModel(libraryRepository, facade, database)

    /** Backs the TV Library sections; keeps repo/DB/Mcws/facade out of the UI. */
    fun makeTvLibraryViewModel(): TvLibraryViewModel =
        TvLibraryViewModel(libraryRepository, facade, database, mcwsClient)

    /** Connect/restore flow; keeps ServerRepository + facade out of the UI. */
    fun makeTvConnectViewModel(): TvConnectViewModel =
        TvConnectViewModel(serverRepository, facade)

    fun makeNowPlayingViewModel(): NowPlayingViewModel = NowPlayingViewModel(facade, mcwsClient)

    fun makeQueueViewModel(): QueueViewModel = QueueViewModel(facade, libraryRepository)

    fun makeZonesViewModel(): ZonesViewModel = ZonesViewModel(facade, libraryRepository)

    fun makeSettingsViewModel(): SettingsViewModel = SettingsViewModel(
        facade = facade,
        database = database,
        clearPhysicalDownloads = {},
        isDebugBuild = isDebugBuild,
    )

}
