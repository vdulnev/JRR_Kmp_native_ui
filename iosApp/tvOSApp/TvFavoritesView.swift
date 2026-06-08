import SharedLogic
import SwiftUI

/// Favorited albums (reconstructed from the stored "name|albumArtist"
/// identifier, matching AlbumDetailViewModel) → album tracks.
struct TvFavoritesView: View {
    @Environment(TvLibraryObservable.self) private var observable
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
                        .contextMenu {
                            Button {
                                Task {
                                    if let tracks = try? await observable.albumTracks(album: album) {
                                        observable.play(tracks: tracks, startIndex: 0)
                                    }
                                }
                            } label: { Label("Play", systemImage: "play") }
                            Button {
                                Task {
                                    if let tracks = try? await observable.albumTracks(album: album) {
                                        observable.playNext(tracks: tracks)
                                    }
                                }
                            } label: { Label("Play Next", systemImage: "text.insert") }
                            Button {
                                Task {
                                    if let tracks = try? await observable.albumTracks(album: album) {
                                        observable.addTracksToQueue(tracks: tracks)
                                    }
                                }
                            } label: { Label("Add to Queue", systemImage: "text.append") }
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
            albums = try await observable.favoriteAlbums()
        } catch {
            albums = []
        }
        loading = false
    }
}
