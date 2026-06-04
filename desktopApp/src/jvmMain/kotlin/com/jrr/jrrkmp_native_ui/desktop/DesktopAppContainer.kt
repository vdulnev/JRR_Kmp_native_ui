package com.jrr.jrrkmp_native_ui.desktop

import co.touchlab.kermit.Logger
import com.jrr.jrrkmp_native_ui.data.api.McwsClient
import com.jrr.jrrkmp_native_ui.data.api.McwsCore
import com.jrr.jrrkmp_native_ui.data.db.DatabaseBuilder
import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.db.createDatabase
import com.jrr.jrrkmp_native_ui.data.repository.LibraryRepository
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.domain.model.LocalAudioQuality
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade

/**
 * Desktop application-scoped DI container — the JVM analogue of the Android
 * `AppContainer`, minus Context/WorkManager. Every long-lived service is a
 * `lazy` property. Local playback is the [DesktopPlayerEngine] stub (Phase 4
 * swaps in VLCJ); everything else (DB, MCWS, repos, facade) is the real
 * commonMain implementation.
 */
class DesktopAppContainer(private val settings: DesktopSettings) {
    private val log = Logger.withTag("di:AppContainer")

    init { log.i { "constructing (Desktop)" } }

    val database: JrrDatabase by lazy {
        log.d { "lazy: database" }
        createDatabase(DatabaseBuilder().createBuilder())
    }

    private val mcwsCore: McwsCore by lazy {
        log.d { "lazy: mcwsCore" }
        McwsCore.create(database)
    }

    val mcwsClient: McwsClient get() = mcwsCore.mcwsClient
    val serverRepository: ServerRepository get() = mcwsCore.serverRepository

    val localPlayerEngine: DesktopPlayerEngine by lazy {
        log.d { "lazy: localPlayerEngine (VLCJ)" }
        DesktopPlayerEngine(
            serverRepository = serverRepository,
            localAudioQualityProvider = {
                LocalAudioQuality.fromName(settings.getLocalAudioQuality())
            },
        )
    }

    val facade: AudioPlayerFacade by lazy {
        log.d { "lazy: facade" }
        AudioPlayerFacade(
            database = database,
            localPlayerEngine = localPlayerEngine,
            mcwsClient = mcwsClient,
            serverRepository = serverRepository,
            saveLastActiveZoneId = { settings.setLastActiveZoneId(it) },
            loadLastActiveZoneId = { settings.getLastActiveZoneId() },
            saveLocalAudioQuality = { settings.setLocalAudioQuality(it) },
            loadLocalAudioQuality = { settings.getLocalAudioQuality() },
        )
    }

    val libraryRepository: LibraryRepository by lazy {
        log.d { "lazy: libraryRepository" }
        LibraryRepository(
            database = database,
            mcwsClient = mcwsClient,
            isOfflineProvider = {
                facade.activeZone.value == Zone.Offline || facade.currentServerHost.isNullOrEmpty()
            },
        )
    }
}
