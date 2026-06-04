import SwiftUI

/// The Library section, mirroring the other platforms' library tabs:
/// Artists, Random albums, Browse, Favorites. (Downloads is omitted — tvOS is
/// online-only.)
struct TvLibraryRootView: View {
    var body: some View {
        TabView {
            TvArtistsView()
                .tabItem { Label("Artists", systemImage: "music.mic") }

            TvRandomAlbumsView()
                .tabItem { Label("Random", systemImage: "shuffle") }

            TvBrowseView()
                .tabItem { Label("Browse", systemImage: "folder") }

            TvFavoritesView()
                .tabItem { Label("Favorites", systemImage: "star") }
        }
    }
}
