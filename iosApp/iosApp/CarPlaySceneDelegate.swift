import CarPlay
import SharedLogic
import UIKit

private let log = SwiftLog("ui:iOS:CarPlay")

/// CarPlay browse tree, kept in parity with the Android Auto media tree
/// (`PlaybackService.kt`):
/// - **Downloads** grouped Album Artist (auto) → Album → Tracks
/// - **Random Albums** with an explicit refresh row (also re-rolls on tab select)
/// - **Artists** (online) → Albums → Tracks
/// - **Browse** mirrors JRiver's MCWS Browse hierarchy; opening a leaf plays it
/// All album/track rows show remote artwork via the MCWS image endpoints.
final class CarPlaySceneDelegate: UIResponder, CPTemplateApplicationSceneDelegate {
    private var interfaceController: CPInterfaceController?
    private var containerRef: AppContainer?

    private let downloadsTemplate = CPListTemplate(title: "Downloads", sections: [])
    private let randomAlbumsTemplate = CPListTemplate(title: "Random Albums", sections: [])
    private let artistsTemplate = CPListTemplate(title: "Artists", sections: [])
    private let browseTemplate = CPListTemplate(title: "Browse", sections: [])

    private var container: AppContainer {
        guard let containerRef else {
            fatalError("AppContainer not yet initialised when CarPlay scene connected")
        }
        return containerRef
    }

    override init() {
        super.init()
        log.d("CarPlaySceneDelegate init")
    }

    // MARK: - Scene lifecycle

    /// iOS 14+ delivers the connect with a CPWindow…
    func templateApplicationScene(
        _: CPTemplateApplicationScene,
        didConnect interfaceController: CPInterfaceController,
        to _: CPWindow,
    ) {
        connect(interfaceController)
    }

    /// …and there is also a windowless variant some OS versions call.
    func templateApplicationScene(
        _: CPTemplateApplicationScene,
        didConnect interfaceController: CPInterfaceController,
    ) {
        connect(interfaceController)
    }

    private func connect(_ interfaceController: CPInterfaceController) {
        log.d("CarPlay scene connected")
        containerRef = (UIApplication.shared.delegate as? AppDelegate)?.container
        self.interfaceController = interfaceController
        setupTabBar()
    }

    func templateApplicationScene(
        _: CPTemplateApplicationScene,
        didDisconnect _: CPInterfaceController,
        from _: CPWindow,
    ) {
        log.d("CarPlay scene disconnected")
        interfaceController = nil
    }

    func templateApplicationScene(
        _: CPTemplateApplicationScene,
        didDisconnectInterfaceController _: CPInterfaceController,
    ) {
        log.d("CarPlay scene disconnected")
        interfaceController = nil
    }

    private func setupTabBar() {
        downloadsTemplate.tabImage = UIImage(systemName: "arrow.down.circle.fill")
        randomAlbumsTemplate.tabImage = UIImage(systemName: "shuffle")
        artistsTemplate.tabImage = UIImage(systemName: "music.mic")
        browseTemplate.tabImage = UIImage(systemName: "folder.fill")

        let tabBar = CPTabBarTemplate(templates: [
            downloadsTemplate,
            randomAlbumsTemplate,
            artistsTemplate,
            browseTemplate,
        ])
        tabBar.delegate = self

        refreshDownloads()
        refreshRandomAlbums()
        refreshArtists()
        refreshBrowse()

        interfaceController?.setRootTemplate(tabBar, animated: true, completion: nil)
    }

    // MARK: - Helpers

    /// JRiver "Album Artist (auto)": album artist, falling back to track artist.
    private func autoAlbumArtist(_ track: Track) -> String {
        let value = track.albumArtist.isEmpty ? track.artist : track.albumArtist
        return value.isEmpty ? "Unknown Artist" : value
    }

    private func albumOf(_ track: Track) -> String {
        track.album.isEmpty ? "Unknown Album" : track.album
    }

    /// Build playable track rows; tapping plays the whole list from that index.
    private func makeTrackItems(_ tracks: [Track]) -> [CPListItem] {
        tracks.enumerated().map { index, track in
            let item = CPListItem(text: track.name, detailText: track.artist)
            loadArtwork(into: item, url: container.mcwsClient.buildImageUrl(fileKey: track.fileKey))
            item.handler = { [weak self] _, completion in
                self?.container.facade.setQueue(tracks: tracks, startIndex: Int32(index))
                self?.presentNowPlaying()
                completion()
            }
            return item
        }
    }

    /// Fetch remote artwork and attach it to a list item. CarPlay needs a
    /// concrete `UIImage` (it won't load a URL like Media3 does on Android).
    private func loadArtwork(into item: CPListItem, url urlString: String) {
        guard !urlString.isEmpty, let url = URL(string: urlString) else { return }
        Task {
            // Use the SSL-bypassing session so self-signed JRiver servers work,
            // matching the SwiftUI app's image loader.
            guard
                let (data, _) = try? await URLSession.sslBypassingSession.data(from: url),
                let image = UIImage(data: data)
            else { return }
            await MainActor.run { item.setImage(image) }
        }
    }

    private func presentNowPlaying() {
        guard let ic = interfaceController else { return }
        if ic.topTemplate === CPNowPlayingTemplate.shared { return }
        // Pushing Now Playing from a deep Browse leaf could exceed the template
        // limit, so collapse the stack to root first when we're near the cap.
        if ic.templates.count >= Self.maxTemplateDepth {
            ic.popToRootTemplate(animated: false) { [weak ic] _, _ in
                ic?.pushTemplate(CPNowPlayingTemplate.shared, animated: true, completion: nil)
            }
        } else {
            ic.pushTemplate(CPNowPlayingTemplate.shared, animated: true, completion: nil)
        }
    }

    /// CarPlay crashes once the navigation stack reaches 5 templates, so the
    /// deepest template we allow sits at stack position 4. Beyond that we swap
    /// the top template instead of growing the stack (see `pushBrowse`), so the
    /// user can keep drilling — only the deepest single back-step is collapsed.
    private static let maxTemplateDepth = 4

    private func push(_ template: CPTemplate) {
        guard let ic = interfaceController else { return }
        log.v("push: current stack depth=\(ic.templates.count)")
        if ic.templates.count >= Self.maxTemplateDepth {
            ic.popTemplate(animated: false) { [weak ic] _, _ in
                ic?.pushTemplate(template, animated: true, completion: nil)
            }
        } else {
            ic.pushTemplate(template, animated: true, completion: nil)
        }
    }

    // MARK: - Downloads  (Album Artist (auto) → Album → Tracks)

    private func refreshDownloads() {
        Task {
            do {
                let tracks = try await container.libraryRepository.getDownloadedTracks()
                let artists = Set(tracks.map { autoAlbumArtist($0) })
                    .sorted { $0.lowercased() < $1.lowercased() }
                log.d("refreshDownloads: \(tracks.count) tracks, \(artists.count) artists")
                let items = artists.map { artist -> CPListItem in
                    let item = CPListItem(text: artist, detailText: nil)
                    item.handler = { [weak self] _, completion in
                        self?.pushDownloadsAlbums(artist: artist)
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

    private func pushDownloadsAlbums(artist: String) {
        Task {
            do {
                let tracks = try await container.libraryRepository.getDownloadedTracks()
                    .filter { autoAlbumArtist($0) == artist }
                let albums = Dictionary(grouping: tracks) { albumOf($0) }
                    .sorted { $0.key.lowercased() < $1.key.lowercased() }
                let items = albums.map { album, albumTracks -> CPListItem in
                    let item = CPListItem(text: album, detailText: artist)
                    if let rep = albumTracks.first {
                        loadArtwork(into: item, url: container.mcwsClient.buildImageUrl(fileKey: rep.fileKey))
                    }
                    item.handler = { [weak self] _, completion in
                        self?.pushTracks(title: album, tracks: self?.sortedAlbum(albumTracks) ?? [])
                        completion()
                    }
                    return item
                }
                await MainActor.run {
                    push(CPListTemplate(title: artist, sections: [CPListSection(items: items)]))
                }
            } catch {
                log.e("downloads albums: \(error)")
            }
        }
    }

    private func sortedAlbum(_ tracks: [Track]) -> [Track] {
        tracks.sorted {
            if $0.discNumber != $1.discNumber { return $0.discNumber < $1.discNumber }
            return $0.trackNumber < $1.trackNumber
        }
    }

    // MARK: - Random Albums

    private func refreshRandomAlbums() {
        Task {
            do {
                let albums = try await container.libraryRepository.getRandomAlbums(limit: 50)
                log.d("refreshRandomAlbums: \(albums.count) albums")
                var items: [CPListItem] = []
                let refresh = CPListItem(text: "Refresh", detailText: "Load new random albums")
                refresh.setImage(UIImage(systemName: "arrow.clockwise"))
                refresh.handler = { [weak self] _, completion in
                    self?.refreshRandomAlbums()
                    completion()
                }
                items.append(refresh)
                items.append(contentsOf: albums.map { albumRow($0) })
                await MainActor.run {
                    randomAlbumsTemplate.updateSections([CPListSection(items: items)])
                }
            } catch {
                log.e("refreshing random albums: \(error)")
            }
        }
    }

    private func albumRow(_ album: Album) -> CPListItem {
        let item = CPListItem(text: album.name, detailText: album.albumArtist)
        loadArtwork(into: item, url: container.mcwsClient.buildImageUrl(fileKey: album.artworkFileKey))
        item.handler = { [weak self] _, completion in
            self?.pushAlbumTracks(album)
            completion()
        }
        return item
    }

    private func pushAlbumTracks(_ album: Album) {
        Task {
            do {
                let tracks = try await container.libraryRepository.getAlbumTracks(album: album)
                await MainActor.run { pushTracks(title: album.name, tracks: tracks) }
            } catch {
                log.e("album tracks: \(error)")
            }
        }
    }

    // MARK: - Artists (online) → Albums → Tracks

    private func refreshArtists() {
        Task {
            do {
                let artists = try await container.libraryRepository.getArtists()
                log.d("refreshArtists: \(artists.count) artists")
                let items = artists.map { artist -> CPListItem in
                    let item = CPListItem(text: artist, detailText: nil)
                    item.handler = { [weak self] _, completion in
                        self?.pushArtistAlbums(artist: artist)
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

    private func pushArtistAlbums(artist: String) {
        Task {
            do {
                let albums = try await container.libraryRepository.getAlbumsByArtist(artistName: artist)
                await MainActor.run {
                    let items = albums.map { albumRow($0) }
                    push(CPListTemplate(title: artist, sections: [CPListSection(items: items)]))
                }
            } catch {
                log.e("artist albums: \(error)")
            }
        }
    }

    // MARK: - Browse (MCWS hierarchy, play-on-leaf-open)

    private func refreshBrowse() {
        Task {
            do {
                let children = try await container.libraryRepository.getBrowseChildren(parentId: "-1")
                log.d("refreshBrowse: \(children.count) children")
                await MainActor.run {
                    // The Browse tab itself sits at stack depth 1 (inside the tab
                    // bar); tapping a row pushes a template at depth 2.
                    browseTemplate.updateSections(
                        [CPListSection(items: children.map { browseRow($0, nextDepth: 2) })],
                    )
                }
            } catch {
                log.e("refreshing browse: \(error)")
            }
        }
    }

    /// `nextDepth` is the CarPlay stack depth the template *opened by this row*
    /// would occupy. We thread it down the navigation chain so we can stay under
    /// CarPlay's 5-template limit deterministically (independent of the tab-bar
    /// stack-count quirks).
    private func browseRow(_ child: BrowseItem, nextDepth: Int) -> CPListItem {
        let item = CPListItem(text: child.name, detailText: nil)
        loadArtwork(into: item, url: container.mcwsClient.buildBrowseImageUrl(nodeId: child.key))
        item.handler = { [weak self] _, completion in
            self?.openBrowseNode(id: child.key, title: child.name, depth: nextDepth)
            completion()
        }
        return item
    }

    private func openBrowseNode(id: String, title: String, depth: Int) {
        Task {
            do {
                let children = try await container.libraryRepository.getBrowseChildren(parentId: id)
                if !children.isEmpty {
                    await MainActor.run {
                        let items = children.map { browseRow($0, nextDepth: depth + 1) }
                        let template = CPListTemplate(title: title, sections: [CPListSection(items: items)])
                        pushBrowse(template, depth: depth)
                    }
                    return
                }
                // Leaf: a folder of files → start playing them, like Android Auto.
                let tracks = try await container.libraryRepository.getBrowseFiles(nodeId: id)
                guard !tracks.isEmpty else { return }
                await MainActor.run {
                    container.facade.setQueue(tracks: tracks, startIndex: 0)
                    presentNowPlaying()
                }
            } catch {
                log.e("opening browse node \(id): \(error)")
            }
        }
    }

    /// Push a browse template, but once we'd exceed CarPlay's 5-template limit,
    /// swap the top template instead of growing the stack — so the user can keep
    /// drilling arbitrarily deep without the "hierarchy depth limit" crash.
    private func pushBrowse(_ template: CPTemplate, depth: Int) {
        guard let ic = interfaceController else { return }
        log.v("pushBrowse depth=\(depth) stack=\(ic.templates.count)")
        if depth > Self.maxTemplateDepth {
            ic.popTemplate(animated: false) { [weak ic] _, _ in
                ic?.pushTemplate(template, animated: true, completion: nil)
            }
        } else {
            ic.pushTemplate(template, animated: true, completion: nil)
        }
    }

    // MARK: - Shared track-list pusher

    private func pushTracks(title: String, tracks: [Track]) {
        let items = makeTrackItems(tracks)
        push(CPListTemplate(title: title, sections: [CPListSection(items: items)]))
    }
}

extension CarPlaySceneDelegate: CPTabBarTemplateDelegate {
    func tabBarTemplate(_: CPTabBarTemplate, didSelect selectedTemplate: CPTemplate) {
        // Re-fetch on tab switch so dynamic tabs (esp. Random Albums) stay fresh.
        if selectedTemplate == downloadsTemplate {
            refreshDownloads()
        } else if selectedTemplate == randomAlbumsTemplate {
            refreshRandomAlbums()
        } else if selectedTemplate == artistsTemplate {
            refreshArtists()
        } else if selectedTemplate == browseTemplate {
            refreshBrowse()
        }
    }
}
