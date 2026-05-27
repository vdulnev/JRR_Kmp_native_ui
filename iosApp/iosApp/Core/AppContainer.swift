import Foundation
import Observation
import SharedLogic

/// Application-scoped dependency container. Constructed once in [AppDelegate]
/// at process start. Every long-lived service is a `let` so the container is
/// effectively immutable after `init`.
///
/// SwiftUI views read the container via `@Environment(AppContainer.self)`.
/// Non-SwiftUI surfaces (CarPlay, scene delegates) reach it via
/// `(UIApplication.shared.delegate as? AppDelegate)?.container`.
@Observable
final class AppContainer {

    let database: JrrDatabase
    let serverRepository: ServerRepository
    let mcwsClient: McwsClient
    let libraryRepository: LibraryRepository
    let localPlayerEngine: IosLocalPlayerEngine
    let corePlayer: CorePlayer
    let facade: AudioPlayerFacade
    let nowPlayingCoordinator: NowPlayingCoordinator
    let playbackStateObserver: PlaybackStateObserver
    let downloadManager: DownloadManager

    init() {
        let builder = DatabaseBuilder().createBuilder()
        let database = DatabaseBuilderKt.createDatabase(builder: builder)
        self.database = database

        let engine = IosLocalPlayerEngine()
        self.localPlayerEngine = engine

        // Build the MCWS networking stack (httpClient + ServerRepository +
        // McwsClient share the same underlying ktor client).
        let mcwsCore = McwsCore.companion.create(database: database)
        self.serverRepository = mcwsCore.serverRepository
        self.mcwsClient = mcwsCore.mcwsClient

        let facade = AudioPlayerFacade(
            database: database,
            localPlayerEngine: engine,
            mcwsClient: mcwsCore.mcwsClient,
            serverRepository: mcwsCore.serverRepository,
            saveLastActiveZoneId: { zoneId in
                UserDefaults.standard.set(zoneId, forKey: "last_active_zone_id")
            },
            loadLastActiveZoneId: {
                UserDefaults.standard.string(forKey: "last_active_zone_id")
            }
        )
        self.facade = facade

        self.corePlayer = CorePlayer(
            engine: engine,
            database: database,
            facade: facade
        )

        self.libraryRepository = LibraryRepository(
            database: database,
            mcwsClient: mcwsCore.mcwsClient,
            isOfflineProvider: FacadeOfflineModeProvider(facade: facade)
        )

        let nowPlayingCoordinator = NowPlayingCoordinator()
        nowPlayingCoordinator.configure(
            playHandler: { facade.play() },
            pauseHandler: { facade.pause() },
            nextHandler: { facade.next() },
            prevHandler: { facade.previous() },
            seekHandler: { pos in facade.seekTo(positionMs: pos) }
        )
        self.nowPlayingCoordinator = nowPlayingCoordinator

        self.playbackStateObserver = PlaybackStateObserver(
            facade: facade,
            database: database,
            nowPlayingCoordinator: nowPlayingCoordinator
        )

        self.downloadManager = DownloadManager(
            database: database,
            facade: facade
        )
        self.downloadManager.setup(libraryRepository: self.libraryRepository)
    }
}

/// Bridges the Kotlin `OfflineModeProvider` SAM interface to the live
/// `AudioPlayerFacade.activeZone`. Used by `LibraryRepository` to decide
/// whether to serve from the downloaded-tracks cache or hit MCWS.
///
/// Implementing the interface directly (rather than passing a closure) avoids
/// the `() -> Boolean` -> `() -> KotlinBoolean` boxing that Kotlin/Native
/// otherwise forces on Swift closures that return primitives. SKIE 0.10.x
/// does not fix this for closures.
private final class FacadeOfflineModeProvider: OfflineModeProvider {
    private let facade: AudioPlayerFacade

    init(facade: AudioPlayerFacade) {
        self.facade = facade
    }

    func isOffline() -> Bool {
        return facade.activeZone.value == Zone.offline
    }
}
