import SharedLogic
import SwiftUI

/// Top-level tabbed shell shown once connected. tvOS renders the tabs as a
/// focusable top bar (swipe up to reach it), so every screen is directly
/// reachable — unlike a `List`-trapped toolbar / bottom bar.
struct TvMainView: View {
    @Environment(TvContainer.self) private var container
    @State private var nowPlaying: NowPlayingObservable?

    var body: some View {
        TabView {
            TvLibraryView()
                .tabItem { Label("Artists", systemImage: "music.mic") }

            NavigationStack { TvSearchView() }
                .tabItem { Label("Search", systemImage: "magnifyingglass") }

            Group {
                if let nowPlaying {
                    NavigationStack { TvNowPlayingDetailView(model: nowPlaying) }
                } else {
                    ProgressView()
                }
            }
            .tabItem { Label("Now Playing", systemImage: "play.circle") }

            NavigationStack { TvZonesView() }
                .tabItem { Label("Output", systemImage: "hifispeaker.2.fill") }
        }
        .onAppear {
            if nowPlaying == nil {
                nowPlaying = NowPlayingObservable(viewModel: container.makeNowPlayingViewModel())
            }
        }
    }
}
