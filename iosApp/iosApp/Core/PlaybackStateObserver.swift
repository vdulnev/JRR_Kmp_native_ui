import Foundation
import SharedLogic
import Combine

class PlaybackStateObserver: ObservableObject {

    @Published var activeZone: Zone = Zone.companion.Offline
    @Published var playerStatus: PlayerStatus? = nil
    @Published var localQueue: [Track] = []

    @Published var downloadedTracks: [DownloadedTrackEntity] = []
    @Published var downloadJobs: [DownloadJobEntity] = []
    @Published var favorites: [FavoriteEntity] = []

    private let database: JrrDatabase
    private let nowPlayingCoordinator: NowPlayingCoordinator

    private var activeZoneDisposable: Disposable?
    private var playerStatusDisposable: Disposable?
    private var localQueueDisposable: Disposable?
    private var downloadedTracksDisposable: Disposable?
    private var downloadJobsDisposable: Disposable?

    init(facade: AudioPlayerFacade, database: JrrDatabase, nowPlayingCoordinator: NowPlayingCoordinator) {
        self.database = database
        self.nowPlayingCoordinator = nowPlayingCoordinator

        activeZoneDisposable = FlowObserver<Zone>(flow: facade.activeZone).start { [weak self] zone in
            if let zone = zone {
                self?.activeZone = zone
            }
        }

        playerStatusDisposable = FlowObserver<PlayerStatus>(flow: facade.playerStatus).start { [weak self] status in
            let status = status
            self?.playerStatus = status

            // Update lock screen controls when playing on local/offline zone
            if let status = status, let self = self {
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

        localQueueDisposable = FlowObserver<NSArray>(flow: facade.localQueue).start { [weak self] queue in
            self?.localQueue = queue as? [Track] ?? []
        }

        downloadedTracksDisposable = FlowObserver<NSArray>(flow: database.downloadedTrackDao().getAllTracksFlow()).start { [weak self] list in
            if let list = list as? [DownloadedTrackEntity] {
                self?.downloadedTracks = list
            }
        }

        downloadJobsDisposable = FlowObserver<NSArray>(flow: database.downloadJobDao().getAllJobsFlow()).start { [weak self] list in
            if let list = list as? [DownloadJobEntity] {
                self?.downloadJobs = list
            }
        }

        refreshFavorites()
    }

    func refreshFavorites() {
        Task {
            do {
                let favs = try await database.favoriteDao().getAllFavorites()
                await MainActor.run {
                    self.favorites = favs
                }
            } catch {
                print("Failed to load favorites: \(error)")
            }
        }
    }

    deinit {
        activeZoneDisposable?.dispose()
        playerStatusDisposable?.dispose()
        localQueueDisposable?.dispose()
        downloadedTracksDisposable?.dispose()
        downloadJobsDisposable?.dispose()
    }
}
