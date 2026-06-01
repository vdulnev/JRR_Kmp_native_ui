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
        self.nowPlayingCoordinator = nowPlayingCoordinator
        self.mcwsClient = mcwsClient

        observationTasks.insert(Task { @MainActor [weak self] in
            for await zone in facade.activeZone {
                self?.activeZone = zone
            }
        })

        observationTasks.insert(Task { @MainActor [weak self] in
            for await status in facade.playerStatus {
                guard let self else { return }
                playerStatus = status

                // Update lock screen controls when playing on local/offline zone
                if let status {
                    let isActiveZoneLocalOrOffline = activeZone.isLocal || activeZone.isOffline
                    if isActiveZoneLocalOrOffline {
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
                let favs = try await database.favoriteDao().getAllFavorites()
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
