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
private let log = SwiftLog("di:AppContainer")

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

    /// Decompose navigation tree. Built once here — not in `ContentView.init` —
    /// so it survives SwiftUI view re-creation. Drives `ContentView` from Phase 4.
    let root: RootComponent
    /// Essenty lifecycle backing [root]'s `ComponentContext`. Resumed at the end
    /// of `init`; held so it stays alive for the app's lifetime.
    @ObservationIgnored private let rootLifecycle: LifecycleRegistry

    init() {
        log.i("constructing (iOS)")
        let builder = DatabaseBuilder().createBuilder()
        let database = DatabaseBuilderKt.createDatabase(builder: builder)
        self.database = database

        let engine = IosLocalPlayerEngine()
        self.localPlayerEngine = engine

        // Build the MCWS networking stack (httpClient + ServerRepository +
        // McwsClient share the same underlying ktor client).
        log.d("building McwsCore")
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
            },
            saveLocalAudioQuality: { quality in
                UserDefaults.standard.set(quality, forKey: "local_audio_quality")
            },
            loadLocalAudioQuality: {
                UserDefaults.standard.string(forKey: "local_audio_quality")
            }
        )
        self.facade = facade

        self.corePlayer = CorePlayer(
            engine: engine,
            database: database,
            facade: facade
        )

        let libraryRepository = LibraryRepository(
            database: database,
            mcwsClient: mcwsCore.mcwsClient,
            isOfflineProvider: FacadeOfflineModeProvider(facade: facade)
        )
        self.libraryRepository = libraryRepository

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

        // ---- Decompose navigation tree (Phase 3 groundwork) ----
        //
        // Feature ViewModels are built lazily by the component tree (retained in
        // Essenty's InstanceKeeper), so AppDeps only supplies factory closures.
        // These capture local lets — not `self` — so they're valid before init
        // finishes.
        #if DEBUG
        let isDebugBuild = true
        #else
        let isDebugBuild = false
        #endif
        let clearPhysicalDownloads: () -> Void = {
            let fileManager = FileManager.default
            let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
            let downloadsDir = documentsURL.appendingPathComponent("downloads")
            if fileManager.fileExists(atPath: downloadsDir.path) {
                do {
                    let filePaths = try fileManager.contentsOfDirectory(atPath: downloadsDir.path)
                    for filePath in filePaths {
                        let fullPath = downloadsDir.appendingPathComponent(filePath).path
                        try fileManager.removeItem(atPath: fullPath)
                    }
                } catch {
                    log.e("clearPhysicalDownloads failed: \(error)")
                }
            }
        }

        let mcwsClient = mcwsCore.mcwsClient
        let deps = AppDeps(
            libraryViewModel: {
                LibraryViewModel(libraryRepository: libraryRepository, facade: facade)
            },
            albumDetailViewModel: { album in
                AlbumDetailViewModel(
                    album: album,
                    libraryRepository: libraryRepository,
                    facade: facade,
                    database: database
                )
            },
            nowPlayingViewModel: {
                NowPlayingViewModel(facade: facade, mcwsClient: mcwsClient)
            },
            queueViewModel: {
                QueueViewModel(facade: facade, libraryRepository: libraryRepository, database: database)
            },
            zonesViewModel: {
                ZonesViewModel(facade: facade, libraryRepository: libraryRepository)
            },
            settingsViewModel: {
                SettingsViewModel(
                    facade: facade,
                    database: database,
                    clearPhysicalDownloads: clearPhysicalDownloads,
                    isDebugBuild: isDebugBuild
                )
            }
        )

        // iOS has no `defaultComponentContext`; build the context manually from
        // Essenty primitives (standard Decompose-on-iOS). StateKeeper for
        // cold-launch restore is an optional follow-up — in-session retention
        // (root held here) is enough for now.
        let lifecycle = LifecycleRegistryKt.LifecycleRegistry()
        let componentContext = DefaultComponentContext(
            lifecycle: lifecycle,
            stateKeeper: nil,
            instanceKeeper: nil,
            backHandler: nil
        )
        let settings = SwiftMainShellSettings()
        let root = RootComponent(
            componentContext: componentContext,
            deps: deps,
            initialConfig: RootComponent.companion.initialConfig(settings: settings)
        )
        self.rootLifecycle = lifecycle
        self.root = root

        LifecycleRegistryExtKt.resume(lifecycle)

        log.i("constructed")
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
