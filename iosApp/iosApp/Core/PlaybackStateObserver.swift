import Foundation
import SharedLogic

private let log = SwiftLog("ui:iOS:PlaybackStateObserver")

class PlaybackStateObserver: ObservableObject {
    @Published var activeZone: Zone = .offline
    @Published var playerStatus: PlayerStatus? = nil
    @Published var localQueue: [Track] = []

    @Published var downloadedTracks: [DownloadedTrackEntity] = []
    @Published var downloadJobs: [DownloadJobEntity] = []
    @Published var favorites: [FavoriteEntity] = []

    private let database: JrrDatabase
    private let facade: AudioPlayerFacade
    private let nowPlayingCoordinator: NowPlayingCoordinator
    private let mcwsClient: McwsClient

    private var observationTasks: Set<Task<Void, Never>> = []

    init(
        facade: AudioPlayerFacade,
        database: JrrDatabase,
        nowPlayingCoordinator: NowPlayingCoordinator,
        mcwsClient: McwsClient,
    ) {
        log.d("init")
        self.database = database
        self.facade = facade
        self.nowPlayingCoordinator = nowPlayingCoordinator
        self.mcwsClient = mcwsClient

        // Favorites are per real server: refresh whenever the active identity
        // changes so they swap on connect.
        observationTasks.insert(Task { @MainActor [weak self] in
            for await _ in facade.activeServerId {
                self?.refreshFavorites()
            }
        })

        observationTasks.insert(Task { @MainActor [weak self] in
            for await zone in facade.activeZone {
                self?.activeZone = zone
            }
        })

        observationTasks.insert(Task { @MainActor [weak self] in
            for await status in facade.playerStatus {
                guard let self else { return }
                playerStatus = status

                // Update system now-playing/remote controls.
                //
                // iOS: only for local/offline playback — the device plays the
                // audio, so the lock screen should control it. For remote zones
                // there is no audio session, so the system would ignore the
                // registration anyway.
                //
                // macOS: for every zone with a loaded track. The app is a remote
                // control; without this the system has no Now Playing app while
                // a *server* zone plays, so the keyboard's play/pause media key
                // launches Music.app instead of controlling JRR.
                if let status {
                    #if os(macOS)
                        let publishNowPlaying = !status.trackName.isEmpty
                    #else
                        let publishNowPlaying = activeZone.isLocal || activeZone.isOffline
                    #endif
                    if publishNowPlaying {
                        let artworkUrl = status.trackFileKey.isEmpty
                            ? nil
                            : mcwsClient.buildImageUrl(fileKey: status.trackFileKey)
                        self.nowPlayingCoordinator.updateNowPlaying(
                            title: status.trackName,
                            artist: status.trackArtist,
                            album: status.trackAlbum,
                            positionMs: status.positionMs,
                            durationMs: status.durationMs,
                            isPlaying: status.state == .playing,
                            artworkUrl: artworkUrl,
                        )
                    }
                }
            }
        })

        observationTasks.insert(Task { @MainActor [weak self] in
            for await queue in facade.localQueue {
                self?.localQueue = queue
            }
        })

        observationTasks.insert(Task { @MainActor [weak self] in
            for await list in database.downloadedTrackDao().getAllTracksFlow() {
                self?.downloadedTracks = list
            }
        })

        observationTasks.insert(Task { @MainActor [weak self] in
            for await list in database.downloadJobDao().getAllJobsFlow() {
                self?.downloadJobs = list
            }
        })

        refreshFavorites()
    }

    deinit {
        // Snapshot to a local so the SwiftLog @autoclosure doesn't capture
        // self during deinit (Swift's strict-capture rule).
        let taskCount = observationTasks.count
        log.d("deinit (cancelling \(taskCount) observation tasks)")
        observationTasks.forEach { $0.cancel() }
    }

    func refreshFavorites() {
        Task {
            do {
                let favs = try await database.favoriteDao().getAllFavorites(serverId: facade.activeServerId.value)
                await MainActor.run {
                    self.favorites = favs
                    log.d("favorites refreshed (count=\(favs.count))")
                }
            } catch {
                log.e("refreshFavorites failed: \(error)")
            }
        }
    }
}
