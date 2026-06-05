import SharedLogic
import SwiftUI

/// Top-level tabbed shell, mirroring the other platforms' main tabs:
/// Playing Now · Library · Zones · Settings. tvOS renders the tabs as a
/// focusable top bar (swipe up to reach it).
struct TvMainView: View {
    @Environment(TvContainer.self) private var container
    let onDisconnect: () -> Void
    @State private var nowPlaying: NowPlayingObservable?
    @State private var queue: QueueObservable?

    var body: some View {
        TabView {
            Group {
                if let nowPlaying, let queue {
                    NavigationStack { TvNowPlayingDetailView(model: nowPlaying, queue: queue) }
                } else {
                    ProgressView()
                }
            }
            .tabItem { Label("Playing Now", systemImage: "play.circle") }

            TvLibraryRootView()
                .tabItem { Label("Library", systemImage: "music.note.list") }

            NavigationStack { TvZonesView() }
                .tabItem { Label("Zones", systemImage: "hifispeaker.2.fill") }

            TvSettingsView(onDisconnect: onDisconnect)
                .tabItem { Label("Settings", systemImage: "gearshape") }
        }
        .onAppear {
            if nowPlaying == nil {
                nowPlaying = NowPlayingObservable(viewModel: container.makeNowPlayingViewModel())
            }
            if queue == nil {
                queue = QueueObservable(viewModel: container.makeQueueViewModel())
            }
        }
    }
}
