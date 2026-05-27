import Foundation
import SharedLogic
import Combine
import KMPNativeCoroutinesCombine

class PlaybackStateObserver: ObservableObject {

    @Published var activeZone: Zone = Zone.offline
    @Published var playerStatus: PlayerStatus? = nil
    @Published var localQueue: [Track] = []

    @Published var downloadedTracks: [DownloadedTrackEntity] = []
    @Published var downloadJobs: [DownloadJobEntity] = []
    @Published var favorites: [FavoriteEntity] = []

    private let database: JrrDatabase
    private let nowPlayingCoordinator: NowPlayingCoordinator

    private var cancellables = Set<AnyCancellable>()

    init(facade: AudioPlayerFacade, database: JrrDatabase, nowPlayingCoordinator: NowPlayingCoordinator) {
        self.database = database
        self.nowPlayingCoordinator = nowPlayingCoordinator

        createPublisher(for: facade.activeZoneFlow)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { _ in },
                receiveValue: { [weak self] zone in
                    self?.activeZone = zone
                }
            )
            .store(in: &cancellables)

        createPublisher(for: facade.playerStatusFlow)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { _ in },
                receiveValue: { [weak self] status in
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
            )
            .store(in: &cancellables)

        createPublisher(for: facade.localQueueFlow)
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { _ in },
                receiveValue: { [weak self] queue in
                    self?.localQueue = queue
                }
            )
            .store(in: &cancellables)

        createPublisher(for: database.downloadedTrackDao().getAllTracksFlow())
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { _ in },
                receiveValue: { [weak self] list in
                    self?.downloadedTracks = list
                }
            )
            .store(in: &cancellables)

        createPublisher(for: database.downloadJobDao().getAllJobsFlow())
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { _ in },
                receiveValue: { [weak self] list in
                    self?.downloadJobs = list
                }
            )
            .store(in: &cancellables)

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
}
