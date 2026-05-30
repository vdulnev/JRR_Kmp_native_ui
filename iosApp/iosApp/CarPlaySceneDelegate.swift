import CarPlay
import SharedLogic
import UIKit

private let log = SwiftLog("ui:iOS:CarPlay")

final class CarPlaySceneDelegate: UIResponder, CPTemplateApplicationSceneDelegate {
    private var interfaceController: CPInterfaceController?
    private var tabBarTemplate: CPTabBarTemplate?

    private let downloadsTemplate = CPListTemplate(title: "Downloads", sections: [])
    private let recentlyPlayedTemplate = CPListTemplate(title: "Recently Played", sections: [])
    private let artistsTemplate = CPListTemplate(title: "Artists", sections: [])
    private let albumsTemplate = CPListTemplate(title: "Albums", sections: [])

    private var container: AppContainer {
        // App-scoped container is owned by AppDelegate; CarPlay scenes are a
        // sibling scene to the SwiftUI window, so we reach it via the app
        // delegate rather than the SwiftUI environment.
        guard let container = (UIApplication.shared.delegate as? AppDelegate)?.container else {
            fatalError("AppContainer not yet initialised when CarPlay scene connected")
        }
        return container
    }

    private var database: JrrDatabase {
        container.database
    }

    func templateApplicationScene(
        _: CPTemplateApplicationScene,
        didConnect interfaceController: CPInterfaceController,
        to _: CPWindow,
    ) {
        self.interfaceController = interfaceController
        setupTabBar()
    }

    func templateApplicationScene(
        _: CPTemplateApplicationScene,
        didDisconnect _: CPInterfaceController,
        from _: CPWindow,
    ) {
        interfaceController = nil
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
            albumsTemplate,
        ])
        tabBar.delegate = self
        tabBarTemplate = tabBar

        refreshDownloads()
        refreshArtists()
        refreshAlbums()

        interfaceController?.setRootTemplate(tabBar, animated: true, completion: nil)
    }

    private func refreshDownloads() {
        let container = container
        Task {
            do {
                let tracks = try await database.downloadedTrackDao().getAllTracks()
                let sortedTracks = tracks.sorted { $0.name < $1.name }

                let items = sortedTracks.enumerated().map { index, track in
                    let item = CPListItem(text: track.name, detailText: "\(track.artist) — \(track.album)")
                    item.handler = { _, completion in
                        let tracks = sortedTracks.map { $0.toTrack() }
                        container.facade.setQueue(tracks: tracks, startIndex: Int32(index))
                        completion()
                    }
                    return item
                }

                await MainActor.run {
                    downloadsTemplate.updateSections([CPListSection(items: items)])
                }
            } catch {
                log.e("refreshing downloads: \(error)")
            }
        }
    }

    private func refreshArtists() {
        Task {
            do {
                let tracks = try await database.downloadedTrackDao().getAllTracks()
                let uniqueArtists = Array(Set(tracks.map(\.artist))).sorted()

                let items = uniqueArtists.map { artistName in
                    let item = CPListItem(text: artistName, detailText: nil)
                    item.handler = { [weak self] _, completion in
                        guard let self else { completion(); return }
                        pushAlbumsList(artist: artistName)
                        completion()
                    }
                    return item
                }

                await MainActor.run {
                    artistsTemplate.updateSections([CPListSection(items: items)])
                }
            } catch {
                log.e("refreshing artists: \(error)")
            }
        }
    }

    private func pushAlbumsList(artist: String) {
        Task {
            do {
                let tracks = try await database.downloadedTrackDao().getAllTracks()
                let artistTracks = tracks.filter { $0.artist == artist }
                let uniqueAlbums = Array(Set(artistTracks.map(\.album))).sorted()

                let items = uniqueAlbums.map { albumName in
                    let item = CPListItem(text: albumName, detailText: nil)
                    item.handler = { [weak self] _, completion in
                        guard let self else { completion(); return }
                        pushTracksList(artist: artist, album: albumName)
                        completion()
                    }
                    return item
                }

                await MainActor.run {
                    let template = CPListTemplate(title: artist, sections: [CPListSection(items: items)])
                    self.interfaceController?.pushTemplate(template, animated: true, completion: nil)
                }
            } catch {
                log.e("pushing albums list: \(error)")
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

                let uniqueAlbums = Array(Set(tracks.map(\.album))).sorted()

                let items = uniqueAlbums.map { albumName in
                    let artistName = albumArtistMap[albumName] ?? ""
                    let item = CPListItem(text: albumName, detailText: artistName)
                    item.handler = { [weak self] _, completion in
                        guard let self else { completion(); return }
                        pushTracksList(album: albumName)
                        completion()
                    }
                    return item
                }

                await MainActor.run {
                    albumsTemplate.updateSections([CPListSection(items: items)])
                }
            } catch {
                log.e("refreshing albums: \(error)")
            }
        }
    }

    private func pushTracksList(artist: String? = nil, album: String) {
        let container = container
        Task {
            do {
                let allTracks = try await database.downloadedTrackDao().getAllTracks()
                let tracks: [DownloadedTrackEntity] = if let artist {
                    allTracks.filter { $0.artist == artist && $0.album == album }
                } else {
                    allTracks.filter { $0.album == album }
                }

                let sortedTracks = tracks.sorted {
                    if $0.trackNumber == $1.trackNumber {
                        return $0.name < $1.name
                    }
                    return $0.trackNumber < $1.trackNumber
                }

                let items = sortedTracks.enumerated().map { index, track in
                    let item = CPListItem(text: track.name, detailText: track.artist)
                    item.handler = { _, completion in
                        let tracks = sortedTracks.map { $0.toTrack() }
                        container.facade.setQueue(tracks: tracks, startIndex: Int32(index))
                        completion()
                    }
                    return item
                }

                await MainActor.run {
                    let template = CPListTemplate(title: album, sections: [CPListSection(items: items)])
                    self.interfaceController?.pushTemplate(template, animated: true, completion: nil)
                }
            } catch {
                log.e("pushing tracks list: \(error)")
            }
        }
    }
}

extension CarPlaySceneDelegate: CPTabBarTemplateDelegate {
    func tabBarTemplate(_: CPTabBarTemplate, didSelect selectedTemplate: CPTemplate) {
        if selectedTemplate == downloadsTemplate {
            refreshDownloads()
        } else if selectedTemplate == artistsTemplate {
            refreshArtists()
        } else if selectedTemplate == albumsTemplate {
            refreshAlbums()
        }
    }
}
