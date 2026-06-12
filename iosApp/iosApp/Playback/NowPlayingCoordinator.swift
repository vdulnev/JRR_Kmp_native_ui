import Foundation
import MediaPlayer
#if canImport(UIKit)
    import UIKit
#elseif canImport(AppKit)
    import AppKit
#endif

private let log = SwiftLog("playback:NowPlayingCoordinator")

class NowPlayingCoordinator {
    private var playHandler: (() -> Void)?
    private var pauseHandler: (() -> Void)?
    private var toggleHandler: (() -> Void)?
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
        toggleHandler: @escaping () -> Void,
        nextHandler: @escaping () -> Void,
        prevHandler: @escaping () -> Void,
        seekHandler: @escaping (Int64) -> Void,
    ) {
        self.playHandler = playHandler
        self.pauseHandler = pauseHandler
        self.toggleHandler = toggleHandler
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

        // The macOS play/pause media key (F8) and single-tap headphone buttons
        // arrive as togglePlayPause, not separate play/pause commands — without
        // this handler the hardware key does nothing (or launches Music.app).
        commandCenter.togglePlayPauseCommand.isEnabled = true
        commandCenter.togglePlayPauseCommand.addTarget { [weak self] _ in
            log.d("remote command: togglePlayPause")
            self?.toggleHandler?()
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

    #if canImport(AppKit)
        /// macOS only routes the hardware play/pause key to an app after
        /// MediaRemote has accepted it as a Now Playing client — and an app
        /// that has only ever claimed `.paused` never gets accepted, so the
        /// key falls through to the system default and launches Music.app.
        /// See the first-update priming in [updateNowPlaying].
        private var didPrimeMediaKeys = false
        private var lastIsPlaying = false
    #endif

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
        #if canImport(AppKit)
            lastIsPlaying = isPlaying
            if !didPrimeMediaKeys {
                // First publish must pass through `.playing` for MediaRemote to
                // accept the app as the media-key target (a synchronous flip to
                // `.paused` would be coalesced away, hence the delayed settle).
                didPrimeMediaKeys = true
                log.d("priming macOS media-key routing")
                MPNowPlayingInfoCenter.default().playbackState = .playing
                if !isPlaying {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) { [weak self] in
                        guard let self else { return }
                        MPNowPlayingInfoCenter.default().playbackState = lastIsPlaying ? .playing : .paused
                    }
                }
            } else {
                MPNowPlayingInfoCenter.default().playbackState = isPlaying ? .playing : .paused
            }
        #else
            MPNowPlayingInfoCenter.default().playbackState = isPlaying ? .playing : .paused
        #endif

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

    private func fetchArtwork(url: URL, completion: @escaping (PlatformImage?) -> Void) {
        Task {
            do {
                let (data, _) = try await URLSession.sslBypassingSession.data(from: url)
                let image = PlatformImage(data: data)
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
