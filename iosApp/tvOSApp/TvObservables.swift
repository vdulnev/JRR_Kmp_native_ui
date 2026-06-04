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
    var isPlaying = false
    var activeZoneName = "No Zone Selected"
    var hasTrack = false

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
        isPlaying = state.isPlaying
        activeZoneName = state.activeZoneName
        hasTrack = state.trackTitle != "Idle"
    }

    func playPause() { isPlaying ? viewModel.pause() : viewModel.play() }
    func next() { viewModel.next() }
    func previous() { viewModel.previous() }
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
