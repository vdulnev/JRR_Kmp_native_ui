import SwiftUI

/// Library sections enum representing the sidebar items.
enum LibrarySection: String, CaseIterable, Identifiable {
    case artists = "Artists"
    case randomAlbums = "Random Albums"
    case browse = "Browse"
    case favorites = "Favorites"

    var id: String {
        rawValue
    }
}

/// The Library section, mirroring the other platforms' library tabs:
/// Artists, Random albums, Browse, Favorites. (Downloads is omitted — tvOS is
/// online-only.)
///
/// tvOS now uses a `NavigationSplitView` to display a sidebar list on the left
/// and the selected section's content (artists list, random albums grid, etc.) on the right.
struct TvLibraryRootView: View {
    @State private var selectedSection: LibrarySection? = .artists

    var body: some View {
        NavigationSplitView {
            List(LibrarySection.allCases, selection: $selectedSection) { section in
                NavigationLink(value: section) {
                    Text(section.rawValue)
                }
            }
            .navigationTitle("Library")
        } detail: {
            if let selectedSection {
                switch selectedSection {
                case .artists:
                    TvArtistsView()
                case .randomAlbums:
                    TvRandomAlbumsView()
                case .browse:
                    TvBrowseView()
                case .favorites:
                    TvFavoritesView()
                }
            } else {
                Text("Select a section")
            }
        }
    }
}
