import SharedLogic
import SwiftUI

/// Favorited albums (reconstructed from the stored "name|albumArtist"
/// identifier, matching AlbumDetailViewModel) → album tracks.
struct TvFavoritesView: View {
    @Environment(TvContainer.self) private var container
    @State private var albums: [Album] = []
    @State private var loading = true

    var body: some View {
        NavigationStack {
            Group {
                if loading {
                    ProgressView()
                } else if albums.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "star").font(.system(size: 60)).foregroundStyle(.secondary)
                        Text("No favorites yet").foregroundStyle(.secondary)
                    }
                } else {
                    List(albums, id: \.albumGroupId) { album in
                        NavigationLink {
                            TvAlbumTracksView(album: album)
                        } label: {
                            Text(album.name.isEmpty ? "Unknown Album" : album.name)
                            + Text("  ·  \(album.albumArtist)").foregroundColor(.secondary)
                        }
                    }
                }
            }
            .navigationTitle("Favorites")
        }
        .task { await load() }
    }

    private func load() async {
        do {
            let favs = try await container.database.favoriteDao().getAllFavorites()
            albums = favs.filter { $0.type == "album" }.map { fav in
                let parts = fav.identifier.split(separator: "|", maxSplits: 1).map(String.init)
                return Album(
                    name: parts.first ?? fav.displayName,
                    albumArtist: parts.count > 1 ? parts[1] : "Unknown Artist",
                    folderPath: "",
                    parentFolderPath: "",
                    date: "",
                    artworkFileKey: "",
                    totalDiscs: 1,
                    discNumber: 1
                )
            }
        } catch {
            albums = []
        }
        loading = false
    }
}
