import Observation
import SharedLogic

/// Bridges `NowPlayingViewModel.state` (Kotlin StateFlow) into observable Swift
/// properties for the transport bar.
@Observable
@MainActor
final class NowPlayingObservable {
    let viewModel: NowPlayingViewModel

    var trackTitle = "Idle"
    var artistName = ""
    var albumTitle = ""
    var isPlaying = false
    var activeZoneName = "No Zone Selected"
    var hasTrack = false
    var imageUrl = ""
    var positionMs: Int64 = 0
    var durationMs: Int64 = 0
    var shuffleOn = false
    var repeatLabel = "Off"

    init(viewModel: NowPlayingViewModel) {
        self.viewModel = viewModel
        sync(viewModel.state.value)
    }

    func observe() async {
        for await state in viewModel.state {
            sync(state)
        }
    }

    private func sync(_ state: NowPlayingViewState) {
        trackTitle = state.trackTitle
        artistName = state.artistName
        albumTitle = state.albumTitle
        isPlaying = state.isPlaying
        activeZoneName = state.activeZoneName
        hasTrack = state.trackTitle != "Idle"
        imageUrl = state.imageUrl
        positionMs = state.positionMs
        durationMs = state.durationMs
        // `mcwsMode` ("Off"/"On"/…) avoids depending on SKIE enum case names.
        shuffleOn = state.shuffleMode.mcwsMode != "Off"
        repeatLabel = state.repeatMode.mcwsMode
    }

    var progress: Double {
        durationMs > 0 ? min(1.0, Double(positionMs) / Double(durationMs)) : 0
    }

    func playPause() {
        isPlaying ? viewModel.pause() : viewModel.play()
    }

    func next() {
        viewModel.next()
    }

    func previous() {
        viewModel.previous()
    }

    func toggleShuffle() {
        viewModel.toggleShuffle()
    }

    func toggleRepeat() {
        viewModel.toggleRepeat()
    }

    func seek(toFraction f: Double) {
        guard durationMs > 0 else { return }
        viewModel.seekTo(positionMs: Int64(f * Double(durationMs)))
    }
}

/// Bridges `QueueViewModel.state` into observable Swift properties for the
/// playing-now queue list.
@Observable
@MainActor
final class QueueObservable {
    let viewModel: QueueViewModel

    var queueTracks: [Track] = []
    var activeIndex: Int = -1
    var favoritedTrackKeys: Set<String> = []

    init(viewModel: QueueViewModel) {
        self.viewModel = viewModel
        sync(viewModel.state.value)
    }

    func observe() async {
        for await state in viewModel.state {
            sync(state)
        }
    }

    private func sync(_ state: QueueViewState) {
        queueTracks = state.queueTracks
        activeIndex = Int(state.activeIndex)
        favoritedTrackKeys = state.favoritedTrackKeys
    }

    func playByIndex(_ index: Int) {
        viewModel.playByIndex(index: Int32(index))
    }

    func toggleFavoriteTrack(_ track: Track) {
        viewModel.toggleFavoriteTrack(track: track)
    }

    func removeQueueTrack(_ index: Int) {
        viewModel.removeQueueTrack(index: Int32(index))
    }
}

/// Bridges `ZonesViewModel.state` into observable Swift properties for the
/// zone picker.
@Observable
@MainActor
final class ZonesObservable {
    let viewModel: ZonesViewModel

    var serverZones: [Zone] = []
    var deviceZones: [Zone] = []
    var activeZoneId = ""
    var isLoading = false

    init(viewModel: ZonesViewModel) {
        self.viewModel = viewModel
        sync(viewModel.state.value)
    }

    func observe() async {
        for await state in viewModel.state {
            sync(state)
        }
    }

    private func sync(_ state: ZonesViewState) {
        serverZones = state.serverZones
        // On-device playback only; drop the Offline pseudo-zone for this app.
        deviceZones = state.deviceZones.filter(\.isLocal)
        activeZoneId = state.activeZoneId
        isLoading = state.isLoading
    }

    func refresh() {
        viewModel.refreshZones()
    }

    func select(_ zone: Zone) {
        viewModel.selectZone(zone: zone)
    }
}

/// Bridges `TvLibraryViewModel` for SwiftUI consumption, encapsulating all
/// data-fetching and playback-routing so the UI doesn't talk to the
/// LibraryRepository or AudioPlayerFacade directly.
@Observable
@MainActor
final class TvLibraryObservable {
    let viewModel: TvLibraryViewModel

    init(viewModel: TvLibraryViewModel) {
        self.viewModel = viewModel
    }

    func play(tracks: [Track], startIndex: Int) {
        viewModel.play(tracks: tracks, startIndex: Int32(startIndex))
    }

    func playNext(tracks: [Track]) {
        viewModel.playNext(tracks: tracks)
    }

    func addTracksToQueue(tracks: [Track]) {
        viewModel.addTracksToQueue(tracks: tracks)
    }

    func albumTracks(album: Album) async throws -> [Track] {
        try await viewModel.albumTracks(album: album)
    }

    func browseFiles(nodeId: String) async throws -> [Track] {
        try await viewModel.browseFiles(nodeId: nodeId)
    }

    func browseChildren(parentId: String) async throws -> [BrowseItem] {
        try await viewModel.browseChildren(parentId: parentId)
    }

    func browseNode(nodeId: String) async throws -> BrowseContent {
        try await viewModel.browseNode(nodeId: nodeId)
    }

    func artists() async throws -> [String] {
        try await viewModel.artists()
    }

    func albums(artist: String) async throws -> [Album] {
        try await viewModel.albums(artist: artist)
    }

    func randomAlbums(limit: Int = 24) async throws -> [Album] {
        try await viewModel.randomAlbums(limit: Int32(limit))
    }

    func favoriteAlbums() async throws -> [Album] {
        try await viewModel.favoriteAlbums()
    }

    func favoritePlaylists() async throws -> [BrowseItem] {
        try await viewModel.favoritePlaylists()
    }

    func isPlaylistFavorite(key: String) async throws -> Bool {
        try await viewModel.isPlaylistFavorite(key: key) as! Bool
    }

    func togglePlaylistFavorite(key: String, name: String) async throws -> Bool {
        try await viewModel.togglePlaylistFavorite(key: key, name: name) as! Bool
    }

    func favoriteTracks() async throws -> [Track] {
        try await viewModel.favoriteTracks()
    }

    func isTrackFavorite(fileKey: String) async throws -> Bool {
        try await viewModel.isTrackFavorite(fileKey: fileKey) as! Bool
    }

    func toggleTrackFavorite(track: Track) async throws -> Bool {
        try await viewModel.toggleTrackFavorite(track: track) as! Bool
    }

    func group(tracks: [Track]) -> [ArtistTrackGroup] {
        viewModel.group(tracks: tracks)
    }

    func artworkUrl(fileKey: String) -> String? {
        viewModel.artworkUrl(fileKey: fileKey)
    }
}
