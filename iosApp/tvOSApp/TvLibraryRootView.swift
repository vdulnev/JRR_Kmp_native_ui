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
    let observable: TvLibraryObservable
    @State private var selectedSection: LibrarySection? = .artists

    var body: some View {
        HStack(spacing: 0) {
            // Left Panel (Sidebar)
            VStack {
                Text("Library")
                    .font(.title3)
                    .fontWeight(.bold)
                    .padding(.top, 40)
                    .padding(.bottom, 20)

                List(LibrarySection.allCases, selection: $selectedSection) { section in
                    Button(action: {
                        selectedSection = section
                    }) {
                        HStack {
                            Text(section.rawValue)
                            Spacer()
                        }
                    }
                    .buttonStyle(.plain)
                    // Highlight the selected row visually
                    .padding(.vertical, 12)
                    .padding(.horizontal, 16)
                    .background(selectedSection == section ? Color.gray.opacity(0.3) : Color.clear)
                    .cornerRadius(8)
                }
            }
            .frame(width: 350)
            .background(Color.black.opacity(0.1))

            // Right Panel (Content)
            Group {
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
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .environment(observable)
    }
}
