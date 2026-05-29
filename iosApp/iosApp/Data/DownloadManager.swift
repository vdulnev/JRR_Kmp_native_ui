import Foundation
import SharedLogic

private let log = SwiftLog("playback:DownloadManager")

class DownloadManager: NSObject, URLSessionDownloadDelegate {

    private var session: URLSession!
    private var activeDownloads: [URLSessionDownloadTask: (String, Int32)] = [:] // Task to (fileKey, jobId) mapping
    private let queue = DispatchQueue(label: "com.jrr.download.manager", qos: .background)

    private let database: JrrDatabase
    private let facade: AudioPlayerFacade

    init(database: JrrDatabase, facade: AudioPlayerFacade) {
        self.database = database
        self.facade = facade
        super.init()
        let config = URLSessionConfiguration.background(withIdentifier: "com.jrr.jrr-ios.download")
        let wifiOnly = UserDefaults.standard.bool(forKey: "wifi_only_downloads")
        config.allowsCellularAccess = !wifiOnly
        config.sharedContainerIdentifier = nil
        self.session = URLSession(configuration: config, delegate: self, delegateQueue: nil)
    }
    
    func setup(libraryRepository: LibraryRepository) {
        libraryRepository.onDownloadQueued = { [weak self] track, jobId in
            self?.triggerDownload(track: track, jobId: jobId.int32Value)
        }
        
        // Resume pending downloads
        resumePendingDownloads()
    }
    
    func recreateSession(wifiOnly: Bool) {
        queue.async { [weak self] in
            guard let self = self else { return }
            self.session.invalidateAndCancel()
            let config = URLSessionConfiguration.background(withIdentifier: "com.jrr.jrr-ios.download")
            config.allowsCellularAccess = !wifiOnly
            self.session = URLSession(configuration: config, delegate: self, delegateQueue: nil)
            self.resumePendingDownloads()
        }
    }
    
    func resumePendingDownloads() {
        Task {
            do {
                let jobs = try await database.downloadJobDao().getAllJobs()
                for job in jobs {
                    if job.state == "QUEUED" || job.state == "DOWNLOADING" {
                        self.triggerDownload(fileKey: job.fileKey, jobId: job.id)
                    }
                }
            } catch {
                log.e("Error resuming downloads: \(error)")
            }
        }
    }
    
    private func triggerDownload(track: Track, jobId: Int32) {
        triggerDownload(fileKey: track.fileKey, jobId: jobId)
    }
    
    private func triggerDownload(fileKey: String, jobId: Int32) {
        // Resolve connection settings from the injected facade
        guard let host = facade.currentServerHost else {
            log.w("Download failed: server not configured")
            return
        }

        let useSsl = facade.currentServerUseSsl
        let port = useSsl ? facade.currentServerSslPort : facade.currentServerPort
        let scheme = useSsl ? "https" : "http"
        let token = facade.currentServerToken ?? ""
        
        // Download in the user-selected quality. The server transcodes on the
        // fly and returns the converted format (flac/opus); the destination
        // extension is taken from the response's suggestedFilename below.
        let quality = facade.currentLocalAudioQuality
        let urlString = "\(scheme)://\(host):\(port)/MCWS/v1/File/GetFile?File=\(fileKey)&FileType=Key&\(quality.mcwsParams)&Token=\(token)"
        guard let url = URL(string: urlString) else { return }
        
        let task = session.downloadTask(with: url)
        
        // Update job state in database
        Task {
            do {
                if let job = try await database.downloadJobDao().getJobById(id: jobId) {
                    let updatedJob = DownloadJobEntity(
                        id: job.id,
                        fileKey: job.fileKey,
                        state: "DOWNLOADING",
                        bytesDownloaded: job.bytesDownloaded,
                        bytesTotal: job.bytesTotal,
                        enqueuedAt: job.enqueuedAt,
                        startedAt: Int64(Date().timeIntervalSince1970 * 1000),
                        name: job.name,
                        artist: job.artist,
                        album: job.album,
                        albumArtist: job.albumArtist,
                        date: job.date,
                        genre: job.genre,
                        durationMs: job.durationMs,
                        trackNumber: job.trackNumber,
                        discNumber: job.discNumber,
                        totalDiscs: job.totalDiscs,
                        totalTracks: job.totalTracks,
                        bitrate: job.bitrate,
                        bitDepth: job.bitDepth,
                        sampleRate: job.sampleRate,
                        channels: job.channels,
                        fileType: job.fileType,
                        filePath: job.filePath,
                        folderPath: job.folderPath
                    )
                    try await database.downloadJobDao().update(job: updatedJob)
                }
            } catch {
                log.e("Failed to update job to DOWNLOADING: \(error)")
            }
        }
        
        activeDownloads[task] = (fileKey, jobId)
        task.resume()
    }
    
    // MARK: - URLSessionDownloadDelegate
    
    func urlSession(
        _ session: URLSession,
        downloadTask: URLSessionDownloadTask,
        didFinishDownloadingTo location: URL
    ) {
        guard let (fileKey, jobId) = activeDownloads[downloadTask] else { return }
        activeDownloads.removeValue(forKey: downloadTask)
        
        let fileManager = FileManager.default
        
        var resolvedLocation = location
        if resolvedLocation.path.hasPrefix("/.nofollow") {
            let cleanPath = String(resolvedLocation.path.dropFirst("/.nofollow".count))
            resolvedLocation = URL(fileURLWithPath: cleanPath)
        } else if resolvedLocation.path.hasPrefix("/.resolve") {
            let cleanPath = String(resolvedLocation.path.dropFirst("/.resolve".count))
            resolvedLocation = URL(fileURLWithPath: cleanPath)
        }
        
        log.v("Original download location: \(location.path), exists: \(fileManager.fileExists(atPath: location.path))")
        log.v("Resolved download location: \(resolvedLocation.path), exists: \(fileManager.fileExists(atPath: resolvedLocation.path))")
        
        let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
        let downloadsDir = documentsURL.appendingPathComponent("downloads")
        
        do {
            if !fileManager.fileExists(atPath: downloadsDir.path) {
                try fileManager.createDirectory(at: downloadsDir, withIntermediateDirectories: true, attributes: nil)
            }
            
            let suggestedFilename = downloadTask.response?.suggestedFilename ?? "\(fileKey).mp3"
            let ext = (suggestedFilename as NSString).pathExtension
            let destinationURL = downloadsDir.appendingPathComponent("\(fileKey).\(ext.isEmpty ? "mp3" : ext)")
            
            if fileManager.fileExists(atPath: destinationURL.path) {
                try fileManager.removeItem(at: destinationURL)
            }
            
            // Move the file synchronously before this delegate method returns and iOS deletes the temp file!
            try fileManager.moveItem(at: resolvedLocation, to: destinationURL)
            
            // Download the album artwork
            downloadArtwork(fileKey: fileKey)
            
            Task {
                do {
                    guard let job = try await database.downloadJobDao().getJobById(id: jobId) else { return }
                    
                    // Save to DownloadedTrackEntity
                    let track = DownloadedTrackEntity(
                        fileKey: fileKey,
                        name: job.name,
                        artist: job.artist,
                        album: job.album, // Saved correctly to job.album instead of job.artist
                        albumArtist: job.albumArtist,
                        date: job.date,
                        genre: job.genre,
                        durationMs: job.durationMs,
                        trackNumber: job.trackNumber,
                        discNumber: job.discNumber,
                        totalDiscs: job.totalDiscs,
                        totalTracks: job.totalTracks,
                        bitrate: job.bitrate,
                        bitDepth: job.bitDepth,
                        sampleRate: job.sampleRate,
                        channels: job.channels,
                        fileType: job.fileType,
                        filePath: job.filePath,
                        folderPath: job.folderPath
                    )
                    try await database.downloadedTrackDao().insert(track: track)
                    try await database.downloadJobDao().delete(job: job)
                    log.i("Completed and saved track: \(job.name) to \(destinationURL.path)")
                } catch {
                    log.e("Error updating database after download: \(error)")
                }
            }
        } catch {
            log.e("Error saving downloaded file: \(error)")
            Task {
                do {
                    if let job = try await database.downloadJobDao().getJobById(id: jobId) {
                        let failedJob = DownloadJobEntity(
                            id: job.id,
                            fileKey: job.fileKey,
                            state: "FAILED",
                            bytesDownloaded: job.bytesDownloaded,
                            bytesTotal: job.bytesTotal,
                            enqueuedAt: job.enqueuedAt,
                            startedAt: job.startedAt,
                            name: job.name,
                            artist: job.artist,
                            album: job.album,
                            albumArtist: job.albumArtist,
                            date: job.date,
                            genre: job.genre,
                            durationMs: job.durationMs,
                            trackNumber: job.trackNumber,
                            discNumber: job.discNumber,
                            totalDiscs: job.totalDiscs,
                            totalTracks: job.totalTracks,
                            bitrate: job.bitrate,
                            bitDepth: job.bitDepth,
                            sampleRate: job.sampleRate,
                            channels: job.channels,
                            fileType: job.fileType,
                            filePath: job.filePath,
                            folderPath: job.folderPath
                        )
                        try await database.downloadJobDao().update(job: failedJob)
                    }
                } catch {
                    log.e("Failed to mark job as FAILED: \(error)")
                }
            }
        }
    }
    
    func urlSession(
        _ session: URLSession,
        downloadTask: URLSessionDownloadTask,
        didWriteData bytesWritten: Int64,
        totalBytesWritten: Int64,
        totalBytesExpectedToWrite: Int64
    ) {
        guard let (_, jobId) = activeDownloads[downloadTask] else { return }
        
        Task {
            do {
                if let job = try await database.downloadJobDao().getJobById(id: jobId) {
                    let updatedJob = DownloadJobEntity(
                        id: job.id,
                        fileKey: job.fileKey,
                        state: job.state,
                        bytesDownloaded: totalBytesWritten,
                        bytesTotal: totalBytesExpectedToWrite,
                        enqueuedAt: job.enqueuedAt,
                        startedAt: job.startedAt,
                        name: job.name,
                        artist: job.artist,
                        album: job.album,
                        albumArtist: job.albumArtist,
                        date: job.date,
                        genre: job.genre,
                        durationMs: job.durationMs,
                        trackNumber: job.trackNumber,
                        discNumber: job.discNumber,
                        totalDiscs: job.totalDiscs,
                        totalTracks: job.totalTracks,
                        bitrate: job.bitrate,
                        bitDepth: job.bitDepth,
                        sampleRate: job.sampleRate,
                        channels: job.channels,
                        fileType: job.fileType,
                        filePath: job.filePath,
                        folderPath: job.folderPath
                    )
                    try await database.downloadJobDao().update(job: updatedJob)
                }
            } catch {
                log.e("Error updating progress: \(error)")
            }
        }
    }
    
    func urlSession(
        _ session: URLSession,
        task: URLSessionTask,
        didCompleteWithError error: Error?
    ) {
        guard let downloadTask = task as? URLSessionDownloadTask,
              let (_, jobId) = activeDownloads[downloadTask] else { return }
        activeDownloads.removeValue(forKey: downloadTask)
        
        if let error = error {
            log.e("Download failed: \(error)")
            Task {
                do {
                    if let job = try await database.downloadJobDao().getJobById(id: jobId) {
                        let failedJob = DownloadJobEntity(
                            id: job.id,
                            fileKey: job.fileKey,
                            state: "FAILED",
                            bytesDownloaded: job.bytesDownloaded,
                            bytesTotal: job.bytesTotal,
                            enqueuedAt: job.enqueuedAt,
                            startedAt: job.startedAt,
                            name: job.name,
                            artist: job.artist,
                            album: job.album,
                            albumArtist: job.albumArtist,
                            date: job.date,
                            genre: job.genre,
                            durationMs: job.durationMs,
                            trackNumber: job.trackNumber,
                            discNumber: job.discNumber,
                            totalDiscs: job.totalDiscs,
                            totalTracks: job.totalTracks,
                            bitrate: job.bitrate,
                            bitDepth: job.bitDepth,
                            sampleRate: job.sampleRate,
                            channels: job.channels,
                            fileType: job.fileType,
                            filePath: job.filePath,
                            folderPath: job.folderPath
                        )
                        try await database.downloadJobDao().update(job: failedJob)
                    }
                } catch {
                    log.e("Failed to mark job as FAILED: \(error)")
                }
            }
        }
    }
    
    func urlSession(
        _ session: URLSession,
        didReceive challenge: URLAuthenticationChallenge,
        completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void
    ) {
        if challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust,
           let serverTrust = challenge.protectionSpace.serverTrust {
            completionHandler(.useCredential, URLCredential(trust: serverTrust))
        } else {
            completionHandler(.performDefaultHandling, nil)
        }
    }
    
    private func downloadArtwork(fileKey: String) {
        guard let host = facade.currentServerHost else {
            log.w("Artwork download failed: host is missing")
            return
        }
        let useSsl = facade.currentServerUseSsl
        let port = useSsl ? facade.currentServerSslPort : facade.currentServerPort
        let scheme = useSsl ? "https" : "http"
        let token = facade.currentServerToken ?? ""
        
        let urlString = "\(scheme)://\(host):\(port)/MCWS/v1/File/GetImage?File=\(fileKey)&Type=Thumbnail&Width=300&Height=300&Square=1&Token=\(token)"
        guard let url = URL(string: urlString) else { return }
        
        log.d("Starting artwork download fileKey=\(fileKey)")
        
        URLSession.sslBypassingSession.dataTask(with: url) { data, response, error in
            guard let data = data, error == nil else {
                log.w("Failed to download artwork: \(String(describing: error))")
                return
            }
            
            let fileManager = FileManager.default
            let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
            let downloadsDir = documentsURL.appendingPathComponent("downloads")
            let artURL = downloadsDir.appendingPathComponent("art_\(fileKey).jpg")
            
            do {
                if !fileManager.fileExists(atPath: downloadsDir.path) {
                    try fileManager.createDirectory(at: downloadsDir, withIntermediateDirectories: true, attributes: nil)
                }
                if fileManager.fileExists(atPath: artURL.path) {
                    try fileManager.removeItem(at: artURL)
                }
                try data.write(to: artURL)
                log.d("Saved artwork to \(artURL.path)")
            } catch {
                log.e("Failed to save artwork: \(error)")
            }
        }.resume()
    }
}
