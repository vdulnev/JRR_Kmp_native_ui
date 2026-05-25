import CarPlay
import UIKit
import SharedLogic

final class CarPlaySceneDelegate: UIResponder, CPTemplateApplicationSceneDelegate {
    private var interfaceController: CPInterfaceController?
    private var tabBarTemplate: CPTabBarTemplate?
    
    private let downloadsTemplate = CPListTemplate(title: "Downloads", sections: [])
    private let recentlyPlayedTemplate = CPListTemplate(title: "Recently Played", sections: [])
    private let artistsTemplate = CPListTemplate(title: "Artists", sections: [])
    private let albumsTemplate = CPListTemplate(title: "Albums", sections: [])
    
    private var database: JrrDatabase {
        return JrrDependencies.shared.database
    }
    
    func templateApplicationScene(
        _ templateApplicationScene: CPTemplateApplicationScene,
        didConnect interfaceController: CPInterfaceController,
        to window: CPWindow
    ) {
        self.interfaceController = interfaceController
        setupTabBar()
    }
    
    func templateApplicationScene(
        _ templateApplicationScene: CPTemplateApplicationScene,
        didDisconnect interfaceController: CPInterfaceController,
        from window: CPWindow
    ) {
        self.interfaceController = nil
    }
    
    private func setupTabBar() {
        downloadsTemplate.tabImage = UIImage(systemName: "arrow.down.circle.fill")
        recentlyPlayedTemplate.tabImage = UIImage(systemName: "clock.fill")
        artistsTemplate.tabImage = UIImage(systemName: "music.mic")
        albumsTemplate.tabImage = UIImage(systemName: "square.stack.fill")
        
        let tabBar = CPTabBarTemplate(templates: [
            downloadsTemplate,
            recentlyPlayedTemplate,
            artistsTemplate,
            albumsTemplate
        ])
        tabBar.delegate = self
        self.tabBarTemplate = tabBar
        
        refreshDownloads()
        refreshArtists()
        refreshAlbums()
        
        interfaceController?.setRootTemplate(tabBar, animated: true, completion: nil)
    }
    
    private func refreshDownloads() {
        Task {
            do {
                let tracks = try await database.downloadedTrackDao().getAllTracks()
                let sortedTracks = tracks.sorted { $0.name < $1.name }
                
                let items = sortedTracks.enumerated().map { index, track in
                    let item = CPListItem(text: track.name, detailText: "\(track.artist) — \(track.album)")
                    item.handler = { [weak self] _, completion in
                        guard self != nil else { completion(); return }
                        let tracks = sortedTracks.map { ($0).toTrack() }
                        JrrDependencies.shared.facade.setQueue(tracks: tracks, startIndex: Int32(index))
                        completion()
                    }
                    return item
                }
                
                await MainActor.run {
                    downloadsTemplate.updateSections([CPListSection(items: items)])
                }
            } catch {
                print("[CarPlay] Error refreshing downloads: \(error)")
            }
        }
    }
    
    private func refreshArtists() {
        Task {
            do {
                let tracks = try await database.downloadedTrackDao().getAllTracks()
                let uniqueArtists = Array(Set(tracks.map { $0.artist })).sorted()
                
                let items = uniqueArtists.map { artistName in
                    let item = CPListItem(text: artistName, detailText: nil)
                    item.handler = { [weak self] _, completion in
                        guard let self = self else { completion(); return }
                        self.pushAlbumsList(artist: artistName)
                        completion()
                    }
                    return item
                }
                
                await MainActor.run {
                    artistsTemplate.updateSections([CPListSection(items: items)])
                }
            } catch {
                print("[CarPlay] Error refreshing artists: \(error)")
            }
        }
    }
    
    private func pushAlbumsList(artist: String) {
        Task {
            do {
                let tracks = try await database.downloadedTrackDao().getAllTracks()
                let artistTracks = tracks.filter { $0.artist == artist }
                let uniqueAlbums = Array(Set(artistTracks.map { $0.album })).sorted()
                
                let items = uniqueAlbums.map { albumName in
                    let item = CPListItem(text: albumName, detailText: nil)
                    item.handler = { [weak self] _, completion in
                        guard let self = self else { completion(); return }
                        self.pushTracksList(artist: artist, album: albumName)
                        completion()
                    }
                    return item
                }
                
                await MainActor.run {
                    let template = CPListTemplate(title: artist, sections: [CPListSection(items: items)])
                    self.interfaceController?.pushTemplate(template, animated: true, completion: nil)
                }
            } catch {
                print("[CarPlay] Error pushing albums list: \(error)")
            }
        }
    }
    
    private func refreshAlbums() {
        Task {
            do {
                let tracks = try await database.downloadedTrackDao().getAllTracks()
                
                var albumArtistMap: [String: String] = [:]
                for track in tracks {
                    if albumArtistMap[track.album] == nil {
                        albumArtistMap[track.album] = track.artist
                    } else if albumArtistMap[track.album] != track.artist {
                        albumArtistMap[track.album] = "Various Artists"
                    }
                }
                
                let uniqueAlbums = Array(Set(tracks.map { $0.album })).sorted()
                
                let items = uniqueAlbums.map { albumName in
                    let artistName = albumArtistMap[albumName] ?? ""
                    let item = CPListItem(text: albumName, detailText: artistName)
                    item.handler = { [weak self] _, completion in
                        guard let self = self else { completion(); return }
                        self.pushTracksList(album: albumName)
                        completion()
                    }
                    return item
                }
                
                await MainActor.run {
                    albumsTemplate.updateSections([CPListSection(items: items)])
                }
            } catch {
                print("[CarPlay] Error refreshing albums: \(error)")
            }
        }
    }
    
    private func pushTracksList(artist: String? = nil, album: String) {
        Task {
            do {
                let allTracks = try await database.downloadedTrackDao().getAllTracks()
                let tracks: [DownloadedTrackEntity]
                if let artist = artist {
                    tracks = allTracks.filter { $0.artist == artist && $0.album == album }
                } else {
                    tracks = allTracks.filter { $0.album == album }
                }
                
                let sortedTracks = tracks.sorted {
                    if $0.trackNumber == $1.trackNumber {
                        return $0.name < $1.name
                    }
                    return $0.trackNumber < $1.trackNumber
                }
                
                let items = sortedTracks.enumerated().map { index, track in
                    let item = CPListItem(text: track.name, detailText: track.artist)
                    item.handler = { [weak self] _, completion in
                        guard self != nil else { completion(); return }
                        let tracks = sortedTracks.map { ($0).toTrack() }
                        JrrDependencies.shared.facade.setQueue(tracks: tracks, startIndex: Int32(index))
                        completion()
                    }
                    return item
                }
                
                await MainActor.run {
                    let template = CPListTemplate(title: album, sections: [CPListSection(items: items)])
                    self.interfaceController?.pushTemplate(template, animated: true, completion: nil)
                }
            } catch {
                print("[CarPlay] Error pushing tracks list: \(error)")
            }
        }
    }
}

extension CarPlaySceneDelegate: CPTabBarTemplateDelegate {
    func tabBarTemplate(_ tabBarTemplate: CPTabBarTemplate, didSelect selectedTemplate: CPTemplate) {
        if selectedTemplate == downloadsTemplate {
            refreshDownloads()
        } else if selectedTemplate == artistsTemplate {
            refreshArtists()
        } else if selectedTemplate == albumsTemplate {
            refreshAlbums()
        }
    }
}
