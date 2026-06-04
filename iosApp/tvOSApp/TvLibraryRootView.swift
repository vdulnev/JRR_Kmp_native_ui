import SwiftUI

/// The Library section, mirroring the other platforms' library tabs:
/// Artists, Random albums, Browse, Favorites. (Downloads is omitted — tvOS is
/// online-only.)
///
/// tvOS does not support a `TabView` nested inside another `TabView` (the inner
/// tab bar never appears), so the sub-sections are switched with a focusable
/// selector row at the top — the same shape as the other platforms' secondary
/// tab row.
struct TvLibraryRootView: View {
    @State private var section = 0
    private let titles = ["Artists", "Random Albums", "Browse", "Favorites"]

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 24) {
                ForEach(Array(titles.enumerated()), id: \.offset) { index, title in
                    Button {
                        section = index
                    } label: {
                        Text(title)
                            .fontWeight(section == index ? .bold : .regular)
                    }
                    .buttonStyle(.bordered)
                    .tint(section == index ? .yellow : .gray)
                }
            }
            .padding(.vertical, 16)

            Group {
                switch section {
                case 0: TvArtistsView()
                case 1: TvRandomAlbumsView()
                case 2: TvBrowseView()
                default: TvFavoritesView()
                }
            }
        }
    }
}
