import SharedLogic
import SwiftUI

/// Top-level tabbed shell, mirroring the other platforms' main tabs:
/// Playing Now · Library · Zones · Settings. tvOS renders the tabs as a
/// focusable top bar (swipe up to reach it).
struct TvMainView: View {
    let onDisconnect: () -> Void

    @State private var nowPlaying: NowPlayingObservable
    @State private var queue: QueueObservable
    @State private var library: TvLibraryObservable
    @State private var zones: ZonesObservable

    init(container: TvContainer, onDisconnect: @escaping () -> Void) {
        self.onDisconnect = onDisconnect
        _nowPlaying = State(wrappedValue: NowPlayingObservable(viewModel: container.makeNowPlayingViewModel()))
        _queue = State(wrappedValue: QueueObservable(viewModel: container.makeQueueViewModel()))
        _library = State(wrappedValue: TvLibraryObservable(viewModel: container.makeTvLibraryViewModel()))
        _zones = State(wrappedValue: ZonesObservable(viewModel: container.makeZonesViewModel()))
    }

    var body: some View {
        TabView {
            NavigationStack {
                TvNowPlayingDetailView(model: nowPlaying, queue: queue)
            }
            .tabItem { Label("Playing Now", systemImage: "play.circle") }

            TvLibraryRootView(observable: library)
                .tabItem { Label("Library", systemImage: "music.note.list") }

            NavigationStack { TvZonesView(zones: zones) }
                .tabItem { Label("Zones", systemImage: "hifispeaker.2.fill") }

            TvSettingsView(onDisconnect: onDisconnect)
                .tabItem { Label("Settings", systemImage: "gearshape") }
        }
    }
}
