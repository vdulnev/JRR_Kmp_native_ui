import Foundation
import MediaPlayer
import UIKit

private let log = SwiftLog("playback:NowPlayingCoordinator")

class NowPlayingCoordinator {
    private var playHandler: (() -> Void)?
    private var pauseHandler: (() -> Void)?
    private var nextHandler: (() -> Void)?
    private var prevHandler: (() -> Void)?
    private var seekHandler: ((Int64) -> Void)?

    // Cache artwork by URL so per-second position updates don't re-fetch it or
    // wipe it from the now-playing info.
    private var lastArtworkUrl: String?
    private var cachedArtwork: MPMediaItemArtwork?

    init() {
        setupRemoteCommands()
    }

    func configure(
        playHandler: @escaping () -> Void,
        pauseHandler: @escaping () -> Void,
        nextHandler: @escaping () -> Void,
        prevHandler: @escaping () -> Void,
        seekHandler: @escaping (Int64) -> Void,
    ) {
        self.playHandler = playHandler
        self.pauseHandler = pauseHandler
        self.nextHandler = nextHandler
        self.prevHandler = prevHandler
        self.seekHandler = seekHandler
    }

    private func setupRemoteCommands() {
        let commandCenter = MPRemoteCommandCenter.shared()

        commandCenter.playCommand.isEnabled = true
        commandCenter.playCommand.addTarget { [weak self] _ in
            self?.playHandler?()
            return .success
        }

        commandCenter.pauseCommand.isEnabled = true
        commandCenter.pauseCommand.addTarget { [weak self] _ in
            self?.pauseHandler?()
            return .success
        }

        commandCenter.nextTrackCommand.isEnabled = true
        commandCenter.nextTrackCommand.addTarget { [weak self] _ in
            self?.nextHandler?()
            return .success
        }

        commandCenter.previousTrackCommand.isEnabled = true
        commandCenter.previousTrackCommand.addTarget { [weak self] _ in
            self?.prevHandler?()
            return .success
        }

        commandCenter.changePlaybackPositionCommand.isEnabled = true
        commandCenter.changePlaybackPositionCommand.addTarget { [weak self] event in
            if let positionEvent = event as? MPChangePlaybackPositionCommandEvent {
                let positionMs = Int64(positionEvent.positionTime * 1000)
                self?.seekHandler?(positionMs)
                return .success
            }
            return .commandFailed
        }
    }

    func updateNowPlaying(
        title: String?,
        artist: String?,
        album: String?,
        positionMs: Int64,
        durationMs: Int64,
        isPlaying: Bool,
        artworkUrl: String? = nil,
    ) {
        var nowPlayingInfo = [String: Any]()

        if let title {
            nowPlayingInfo[MPMediaItemPropertyTitle] = title
        }
        if let artist {
            nowPlayingInfo[MPMediaItemPropertyArtist] = artist
        }
        if let album {
            nowPlayingInfo[MPMediaItemPropertyAlbumTitle] = album
        }

        nowPlayingInfo[MPMediaItemPropertyPlaybackDuration] = Double(durationMs) / 1000.0
        nowPlayingInfo[MPNowPlayingInfoPropertyElapsedPlaybackTime] = Double(positionMs) / 1000.0
        nowPlayingInfo[MPNowPlayingInfoPropertyPlaybackRate] = isPlaying ? 1.0 : 0.0

        // Keep already-loaded artwork in place across position-only updates.
        if artworkUrl == lastArtworkUrl, let cachedArtwork {
            nowPlayingInfo[MPMediaItemPropertyArtwork] = cachedArtwork
        }

        MPNowPlayingInfoCenter.default().nowPlayingInfo = nowPlayingInfo
        // CarPlay / lock screen drive the play-pause button off `playbackState`,
        // not just the rate — set it explicitly so the control reflects reality.
        MPNowPlayingInfoCenter.default().playbackState = isPlaying ? .playing : .paused

        // Fetch artwork only when the track (URL) actually changes.
        if let artworkUrl, artworkUrl != lastArtworkUrl, let url = URL(string: artworkUrl) {
            log.d("now-playing: fetching artwork (isPlaying=\(isPlaying))")
            lastArtworkUrl = artworkUrl
            cachedArtwork = nil
            fetchArtwork(url: url) { [weak self] image in
                guard let self, let image else {
                    log.w("now-playing: artwork fetch returned no image")
                    return
                }
                log.d("now-playing: artwork set (\(Int(image.size.width))x\(Int(image.size.height)))")
                let artwork = MPMediaItemArtwork(boundsSize: image.size) { _ in image }
                cachedArtwork = artwork
                var updatedInfo = MPNowPlayingInfoCenter.default().nowPlayingInfo ?? [String: Any]()
                updatedInfo[MPMediaItemPropertyArtwork] = artwork
                MPNowPlayingInfoCenter.default().nowPlayingInfo = updatedInfo
            }
        }
    }

    private func fetchArtwork(url: URL, completion: @escaping (UIImage?) -> Void) {
        Task {
            do {
                let (data, _) = try await URLSession.sslBypassingSession.data(from: url)
                let image = UIImage(data: data)
                await MainActor.run {
                    completion(image)
                }
            } catch {
                log.w("Failed to fetch artwork: \(error)")
                await MainActor.run {
                    completion(nil)
                }
            }
        }
    }
}
