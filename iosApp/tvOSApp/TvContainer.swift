import Foundation
import Observation
import SharedLogic

/// Application-scoped container for the tvOS app. Builds the Room DB, the MCWS
/// networking stack, the playback facade (remote-zone control), and the library
/// repository. Local audio (AVPlayer) is a follow-up; playback here targets a
/// JRiver server zone over MCWS.
@Observable
final class TvContainer {
    let database: JrrDatabase
    let serverRepository: ServerRepository
    let mcwsClient: McwsClient
    let libraryRepository: LibraryRepository
    let localPlayerEngine: IosLocalPlayerEngine
    let facade: AudioPlayerFacade
    /// Native AVPlayer driver for on-device ("Local") playback. Registers
    /// itself as the engine's controller; retained for the app's lifetime.
    let corePlayer: CorePlayer

    init() {
        let builder = DatabaseBuilder().createBuilder()
        let db = DatabaseBuilderKt.createDatabase(builder: builder)
        database = db

        let core = McwsCore.companion.create(database: db)
        serverRepository = core.serverRepository
        mcwsClient = core.mcwsClient

        let engine = IosLocalPlayerEngine()
        localPlayerEngine = engine

        facade = AudioPlayerFacade(
            database: db,
            localPlayerEngine: engine,
            mcwsClient: core.mcwsClient,
            serverRepository: core.serverRepository,
            saveLastActiveZoneId: { UserDefaults.standard.set($0, forKey: "last_active_zone_id") },
            loadLastActiveZoneId: { UserDefaults.standard.string(forKey: "last_active_zone_id") },
            saveLocalAudioQuality: { UserDefaults.standard.set($0, forKey: "local_audio_quality") },
            loadLocalAudioQuality: { UserDefaults.standard.string(forKey: "local_audio_quality") },
        )

        corePlayer = CorePlayer(engine: engine, database: db, facade: facade)

        libraryRepository = LibraryRepository(
            database: db,
            mcwsClient: core.mcwsClient,
            isOfflineProvider: AlwaysOnlineProvider(),
        )
    }

    func makeZonesViewModel() -> ZonesViewModel {
        ZonesViewModel(facade: facade, libraryRepository: libraryRepository)
    }

    func makeNowPlayingViewModel() -> NowPlayingViewModel {
        NowPlayingViewModel(facade: facade, mcwsClient: mcwsClient)
    }

    func makeQueueViewModel() -> QueueViewModel {
        QueueViewModel(facade: facade, libraryRepository: libraryRepository)
    }

    func makeSettingsViewModel() -> SettingsViewModel {
        SettingsViewModel(
            facade: facade,
            database: database,
            clearPhysicalDownloads: {},
            isDebugBuild: true,
        )
    }

    /// Clears the active server and forgets saved servers so launch restore
    /// won't reconnect — used by the Settings "Disconnect" action.
    func disconnect() async {
        if let servers = try? await serverRepository.getAllServers() {
            for s in servers {
                try? await serverRepository.deleteServer(server: s)
            }
        }
        serverRepository.setActiveServer(host: "", port: 52199, useSsl: false, sslPort: 52200, token: nil)
    }
}

/// tvOS is online-only, so the library always serves from MCWS.
private final class AlwaysOnlineProvider: OfflineModeProvider {
    func isOffline() -> Bool {
        false
    }
}
