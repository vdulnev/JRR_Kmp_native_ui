import Foundation
import SharedLogic
import Combine

class PlaybackStateObserver: ObservableObject {
    static let shared = PlaybackStateObserver()
    
    @Published var activeZone: Zone = Zone.companion.Offline
    @Published var playerStatus: PlayerStatus? = nil
    @Published var localQueue: [Track] = []
    
    @Published var downloadedTracks: [DownloadedTrackEntity] = []
    @Published var downloadJobs: [DownloadJobEntity] = []
    @Published var favorites: [FavoriteEntity] = []
    
    private var activeZoneDisposable: Disposable?
    private var playerStatusDisposable: Disposable?
    private var localQueueDisposable: Disposable?
    private var downloadedTracksDisposable: Disposable?
    private var downloadJobsDisposable: Disposable?
    
    private init() {
        let facade = JrrDependencies.shared.facade
        let db = JrrDependencies.shared.database
        
        activeZoneDisposable = FlowObserver<Zone>(flow: facade.activeZone).start { [weak self] zone in
            if let zone = zone {
                self?.activeZone = zone
            }
        }
        
        playerStatusDisposable = FlowObserver<PlayerStatus>(flow: facade.playerStatus).start { [weak self] status in
            let status = status
            self?.playerStatus = status
            
            // Update lock screen controls when playing on local/offline zone
            if let status = status {
                let isActiveZoneLocalOrOffline = self?.activeZone.isLocal == true || self?.activeZone.isOffline == true
                if isActiveZoneLocalOrOffline {
                    NowPlayingCoordinator.shared.updateNowPlaying(
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
        
        downloadedTracksDisposable = FlowObserver<NSArray>(flow: db.downloadedTrackDao().getAllTracksFlow()).start { [weak self] list in
            if let list = list as? [DownloadedTrackEntity] {
                self?.downloadedTracks = list
            }
        }
        
        downloadJobsDisposable = FlowObserver<NSArray>(flow: db.downloadJobDao().getAllJobsFlow()).start { [weak self] list in
            if let list = list as? [DownloadJobEntity] {
                self?.downloadJobs = list
            }
        }
        
        // Configure system remote command center shortcuts
        NowPlayingCoordinator.shared.configure(
            playHandler: { facade.play() },
            pauseHandler: { facade.pause() },
            nextHandler: { facade.next() },
            prevHandler: { facade.previous() },
            seekHandler: { pos in facade.seekTo(positionMs: pos) }
        )
        
        refreshFavorites()
    }
    
    func refreshFavorites() {
        Task {
            do {
                let db = JrrDependencies.shared.database
                let favs = try await db.favoriteDao().getAllFavorites()
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

