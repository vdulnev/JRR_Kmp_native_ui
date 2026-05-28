import Foundation
import SharedLogic

private let log = SwiftLog("ui:iOS:PlaybackStateObserver")

class PlaybackStateObserver: ObservableObject {

    @Published var activeZone: Zone = Zone.offline
    @Published var playerStatus: PlayerStatus? = nil
    @Published var localQueue: [Track] = []

    @Published var downloadedTracks: [DownloadedTrackEntity] = []
    @Published var downloadJobs: [DownloadJobEntity] = []
    @Published var favorites: [FavoriteEntity] = []

    private let database: JrrDatabase
    private let nowPlayingCoordinator: NowPlayingCoordinator

    private var observationTasks: Set<Task<Void, Never>> = []

    init(facade: AudioPlayerFacade, database: JrrDatabase, nowPlayingCoordinator: NowPlayingCoordinator) {
        log.d("init")
        self.database = database
        self.nowPlayingCoordinator = nowPlayingCoordinator

        observationTasks.insert(Task { @MainActor [weak self] in
            for await zone in facade.activeZone {
                self?.activeZone = zone
            }
        })

        observationTasks.insert(Task { @MainActor [weak self] in
            for await status in facade.playerStatus {
                guard let self = self else { return }
                self.playerStatus = status

                // Update lock screen controls when playing on local/offline zone
                if let status = status {
                    let isActiveZoneLocalOrOffline = self.activeZone.isLocal || self.activeZone.isOffline
                    if isActiveZoneLocalOrOffline {
                        self.nowPlayingCoordinator.updateNowPlaying(
                            title: status.trackName,
                            artist: status.trackArtist,
                            album: status.trackAlbum,
                            positionMs: status.positionMs,
                            durationMs: status.durationMs,
                            isPlaying: status.state == .playing
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
        log.d("deinit (cancelling \(observationTasks.count) observation tasks)")
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
