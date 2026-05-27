import Foundation
import AVFoundation
import Combine
import SharedLogic

class CorePlayer: NSObject, ObservableObject, NativePlayerController {
    private let engine: IosLocalPlayerEngine
    private let database: JrrDatabase
    private let facade: AudioPlayerFacade

    private var queuePlayer: AVQueuePlayer?
    private var playerItems: [AVPlayerItem] = []

    private var localQueue: [Track] = []
    private var localCurrentIndex: Int32 = -1

    private var timeObserver: Any?
    private var cancellables = Set<AnyCancellable>()

    init(engine: IosLocalPlayerEngine, database: JrrDatabase, facade: AudioPlayerFacade) {
        self.engine = engine
        self.database = database
        self.facade = facade
        super.init()
        self.engine.setController(controller: self)
        setupAudioSession()
    }
    
    private func setupAudioSession() {
        do {
            let session = AVAudioSession.sharedInstance()
            try session.setCategory(.playback, mode: .default, options: [])
            try session.setActive(true)
            
            NotificationCenter.default.publisher(for: AVAudioSession.interruptionNotification)
                .sink { [weak self] notification in
                    self?.handleInterruption(notification: notification)
                }
                .store(in: &cancellables)
        } catch {
            print("Failed to setup AVAudioSession: \(error)")
        }
    }
    
    private func handleInterruption(notification: Notification) {
        guard let userInfo = notification.userInfo,
              let typeValue = userInfo[AVAudioSessionInterruptionTypeKey] as? UInt,
              let type = AVAudioSession.InterruptionType(rawValue: typeValue) else {
            return
        }
        
        if type == .began {
            pause()
        } else if type == .ended {
            if let optionsValue = userInfo[AVAudioSessionInterruptionOptionKey] as? UInt {
                let options = AVAudioSession.InterruptionOptions(rawValue: optionsValue)
                if options.contains(.shouldResume) {
                    play()
                }
            }
        }
    }
    
    private func setupPlayer() {
        let player = AVQueuePlayer()
        self.queuePlayer = player
        
        player.publisher(for: \.timeControlStatus)
            .sink { [weak self] status in
                self?.updatePlaybackState(status: status)
            }
            .store(in: &cancellables)
        
        player.publisher(for: \.volume)
            .sink { [weak self] vol in
                self?.engine.updateVolume(vol: vol)
            }
            .store(in: &cancellables)
        
        player.publisher(for: \.currentItem)
            .sink { [weak self] item in
                self?.handleCurrentItemChanged(item: item)
            }
            .store(in: &cancellables)
            
        timeObserver = player.addPeriodicTimeObserver(forInterval: CMTime(seconds: 0.5, preferredTimescale: 600), queue: .main) { [weak self] _ in
            self?.objectWillChange.send()
        }
        
        NotificationCenter.default.publisher(for: .AVPlayerItemDidPlayToEndTime)
            .sink { [weak self] notification in
                self?.handleItemDidPlayToEnd(notification: notification)
            }
            .store(in: &cancellables)
    }
    
    private func updatePlaybackState(status: AVPlayer.TimeControlStatus) {
        let state: PlaybackState
        switch status {
        case .playing:
            state = .playing
        case .paused:
            state = .paused
        case .waitingToPlayAtSpecifiedRate:
            state = .playing
        @unknown default:
            state = .stopped
        }
        engine.updatePlaybackState(state: state)
    }
    
    private func handleCurrentItemChanged(item: AVPlayerItem?) {
        guard let item = item else {
            if playerItems.isEmpty {
                engine.updateCurrentIndex(index: -1)
                localCurrentIndex = -1
            }
            return
        }
        
        if let idx = playerItems.firstIndex(of: item) {
            let index32 = Int32(idx)
            localCurrentIndex = index32
            engine.updateCurrentIndex(index: index32)
        }
    }
    
    private func handleItemDidPlayToEnd(notification: Notification) {
        if (engine.repeatMode.value as? RepeatMode) == .track {
            if let item = notification.object as? AVPlayerItem {
                item.seek(to: .zero, completionHandler: nil)
                queuePlayer?.play()
            }
        }
    }
    
    // MARK: - NativePlayerController
    
    func play() {
        if queuePlayer == nil {
            setupPlayer()
        }
        queuePlayer?.play()
    }
    
    func pause() {
        queuePlayer?.pause()
    }
    
    func stop() {
        queuePlayer?.pause()
        queuePlayer?.removeAllItems()
        engine.updatePlaybackState(state: .stopped)
        engine.updateCurrentIndex(index: -1)
        localCurrentIndex = -1
    }
    
    func seekTo(positionMs: Int64) {
        let time = CMTime(value: positionMs, timescale: 1000)
        queuePlayer?.seek(to: time)
    }
    
    func setVolume(level: Float) {
        queuePlayer?.volume = level
        engine.updateVolume(vol: level)
    }
    
    private func createPlayerItem(for track: Track) -> AVPlayerItem {
        let url: URL
        if let localPath = getLocalFilePath(fileKey: track.fileKey) {
            url = URL(fileURLWithPath: localPath)
        } else {
            let host = facade.currentServerHost ?? ""
            let useSsl = facade.currentServerUseSsl
            let port = useSsl ? facade.currentServerSslPort : facade.currentServerPort
            let scheme = useSsl ? "https" : "http"
            let token = facade.currentServerToken ?? ""
            let encodedUrl = "\(scheme)://\(host):\(port)/MCWS/v1/File/GetFile?File=\(track.fileKey)&Playback=1&Token=\(token)"
            url = URL(string: encodedUrl) ?? URL(fileURLWithPath: "")
        }
        return AVPlayerItem(url: url)
    }

    func setQueue(tracks: [Track], startIndex: Int32) {
        if queuePlayer == nil {
            setupPlayer()
        }
        
        guard let player = queuePlayer else { return }
        
        player.pause()
        player.removeAllItems()
        
        self.localQueue = tracks
        
        var items: [AVPlayerItem] = []
        for track in tracks {
            items.append(createPlayerItem(for: track))
        }
        
        self.playerItems = items
        
        let start = Int(startIndex)
        guard start >= 0 && start < playerItems.count else { return }
        
        for i in start..<playerItems.count {
            let item = playerItems[i]
            if player.canInsert(item, after: nil) {
                player.insert(item, after: nil)
            }
        }
        
        self.localCurrentIndex = startIndex
        engine.updateCurrentIndex(index: startIndex)
    }
    
    func playByIndex(index: Int32) {
        guard index >= 0 && index < localQueue.count else { return }
        setQueue(tracks: localQueue, startIndex: index)
        play()
    }
    
    func removeTrack(index: Int32) {
        let idx = Int(index)
        guard idx >= 0 && idx < localQueue.count else { return }
        
        let wasPlaying = queuePlayer?.rate ?? 0 > 0
        
        if idx == localCurrentIndex {
            if localQueue.count == 1 {
                stop()
            } else {
                let nextIdx = idx < localQueue.count - 1 ? idx + 1 : idx - 1
                let nextTrack = localQueue[nextIdx]
                
                localQueue.remove(at: idx)
                playerItems.remove(at: idx)
                
                if let newIdx = localQueue.firstIndex(of: nextTrack) {
                    setQueue(tracks: localQueue, startIndex: Int32(newIdx))
                    if !wasPlaying {
                        pause()
                    }
                }
            }
        } else {
            let removedItem = playerItems[idx]
            queuePlayer?.remove(removedItem)
            
            localQueue.remove(at: idx)
            playerItems.remove(at: idx)
            
            if idx < localCurrentIndex {
                localCurrentIndex -= 1
                engine.updateCurrentIndex(index: localCurrentIndex)
            }
            objectWillChange.send()
        }
    }
    
    func moveTrack(from: Int32, to: Int32) {
        let fromIdx = Int(from)
        let toIdx = Int(to)
        guard fromIdx >= 0 && fromIdx < localQueue.count,
              toIdx >= 0 && toIdx < localQueue.count,
              fromIdx != toIdx else { return }
              
        let wasPlaying = queuePlayer?.rate ?? 0 > 0
        let activePos = getCurrentPosition()
        let currentIdx = Int(localCurrentIndex)
        let activeTrackKey: String? = (!localQueue.isEmpty && currentIdx >= 0 && currentIdx < localQueue.count) ? localQueue[currentIdx].fileKey : nil
        
        let track = localQueue.remove(at: fromIdx)
        localQueue.insert(track, at: toIdx)
        
        var items: [AVPlayerItem] = []
        for t in localQueue {
            items.append(createPlayerItem(for: t))
        }
        self.playerItems = items
        
        if let key = activeTrackKey, let newIdx = localQueue.firstIndex(where: { $0.fileKey == key }) {
            localCurrentIndex = Int32(newIdx)
            engine.updateCurrentIndex(index: localCurrentIndex)
            if let player = queuePlayer {
                player.pause()
                player.removeAllItems()
                for i in Int(localCurrentIndex)..<playerItems.count {
                    let item = playerItems[i]
                    if player.canInsert(item, after: nil) {
                        player.insert(item, after: nil)
                    }
                }
                seekTo(positionMs: activePos)
                if wasPlaying {
                    player.play()
                }
            }
        } else {
            if let player = queuePlayer {
                player.pause()
                player.removeAllItems()
                let newStart = min(Int(localCurrentIndex), localQueue.count - 1)
                if newStart >= 0 {
                    for i in newStart..<playerItems.count {
                        let item = playerItems[i]
                        if player.canInsert(item, after: nil) {
                            player.insert(item, after: nil)
                        }
                    }
                    if wasPlaying {
                        player.play()
                    }
                }
            }
        }
        
        objectWillChange.send()
    }
    
    func clearQueue() {
        stop()
        localQueue = []
        playerItems = []
        engine.updateQueue(tracks: [])
        objectWillChange.send()
    }
    
    func getCurrentPosition() -> Int64 {
        guard let player = queuePlayer else { return 0 }
        let sec = CMTimeGetSeconds(player.currentTime())
        return sec.isNaN ? 0 : Int64(sec * 1000)
    }
    
    func getDuration() -> Int64 {
        guard let item = queuePlayer?.currentItem else { return 0 }
        let sec = CMTimeGetSeconds(item.duration)
        return sec.isNaN ? 0 : Int64(sec * 1000)
    }
    
    func addTracks(tracks: [Track]) {
        if queuePlayer == nil {
            setupPlayer()
        }
        
        guard let player = queuePlayer else { return }
        
        let wasEmpty = localQueue.isEmpty
        
        self.localQueue.append(contentsOf: tracks)
        
        var newItems: [AVPlayerItem] = []
        for track in tracks {
            let item = createPlayerItem(for: track)
            newItems.append(item)
            self.playerItems.append(item)
        }
        
        for item in newItems {
            if player.canInsert(item, after: nil) {
                player.insert(item, after: nil)
            }
        }
        
        if wasEmpty {
            self.localCurrentIndex = 0
            engine.updateCurrentIndex(index: 0)
            play()
        }
        
        objectWillChange.send()
    }
    
    func insertTracksNext(tracks: [Track]) {
        if queuePlayer == nil {
            setupPlayer()
        }
        
        guard let player = queuePlayer else { return }
        
        let wasEmpty = localQueue.isEmpty
        
        let currentIdx = Int(localCurrentIndex)
        let insertIndex = currentIdx >= 0 ? currentIdx + 1 : 0
        
        var newItems: [AVPlayerItem] = []
        for track in tracks {
            newItems.append(createPlayerItem(for: track))
        }
        
        // Insert into localQueue and playerItems arrays
        if insertIndex >= 0 && insertIndex <= localQueue.count {
            localQueue.insert(contentsOf: tracks, at: insertIndex)
            playerItems.insert(contentsOf: newItems, at: insertIndex)
        } else {
            localQueue.append(contentsOf: tracks)
            playerItems.append(contentsOf: newItems)
        }
        
        // Insert into queuePlayer
        var previousItem = player.currentItem
        for item in newItems {
            if player.canInsert(item, after: previousItem) {
                player.insert(item, after: previousItem)
                previousItem = item
            }
        }
        
        if wasEmpty {
            self.localCurrentIndex = 0
            engine.updateCurrentIndex(index: 0)
            play()
        }
        
        objectWillChange.send()
    }
    
    private func getLocalFilePath(fileKey: String) -> String? {
        let fileManager = FileManager.default
        let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let downloadsDir = documentsURL.appendingPathComponent("downloads")
        for ext in ["mp3", "m4a", "wav", "flac", "aac"] {
            let fileURL = downloadsDir.appendingPathComponent("\(fileKey).\(ext)")
            if fileManager.fileExists(atPath: fileURL.path) {
                return fileURL.path
            }
        }
        return nil
    }
    
    deinit {
        if let observer = timeObserver {
            queuePlayer?.removeTimeObserver(observer)
        }
    }
}

