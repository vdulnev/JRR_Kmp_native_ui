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

    func playPause() { isPlaying ? viewModel.pause() : viewModel.play() }
    func next() { viewModel.next() }
    func previous() { viewModel.previous() }
    func toggleShuffle() { viewModel.toggleShuffle() }
    func toggleRepeat() { viewModel.toggleRepeat() }
    func seek(toFraction f: Double) {
        guard durationMs > 0 else { return }
        viewModel.seekTo(positionMs: Int64(f * Double(durationMs)))
    }
}

/// Bridges `ZonesViewModel.state` into observable Swift properties for the
/// zone picker.
@Observable
@MainActor
final class ZonesObservable {
    let viewModel: ZonesViewModel

    var serverZones: [Zone] = []
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
        activeZoneId = state.activeZoneId
        isLoading = state.isLoading
    }

    func refresh() { viewModel.refreshZones() }
    func select(_ zone: Zone) { viewModel.selectZone(zone: zone) }
}
