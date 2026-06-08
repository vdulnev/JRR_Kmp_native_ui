import SharedLogic
import SwiftUI

/// Favorited albums and playlists → album tracks / browse node.
struct TvFavoritesView: View {
    @Environment(TvLibraryObservable.self) private var observable
    @State private var albums: [Album] = []
    @State private var playlists: [BrowseItem] = []
    @State private var loading = true

    var body: some View {
        NavigationStack {
            Group {
                if loading {
                    ProgressView()
                } else if albums.isEmpty, playlists.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "star").font(.system(size: 60)).foregroundStyle(.secondary)
                        Text("No favorites yet").foregroundStyle(.secondary)
                    }
                } else {
                    List {
                        if !albums.isEmpty {
                            Section(header: Text("Albums")) {
                                ForEach(albums, id: \.albumGroupId) { album in
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

                        if !playlists.isEmpty {
                            Section(header: Text("Playlists")) {
                                ForEach(playlists, id: \.key) { playlist in
                                    NavigationLink {
                                        TvBrowseNodeView(nodeId: playlist.key, title: playlist.name)
                                    } label: {
                                        Text(playlist.name)
                                    }
                                    .contextMenu {
                                        Button {
                                            Task {
                                                if let tracks = try? await observable.browseFiles(nodeId: playlist.key) {
                                                    observable.play(tracks: tracks, startIndex: 0)
                                                }
                                            }
                                        } label: { Label("Play", systemImage: "play") }
                                        Button {
                                            Task {
                                                if let tracks = try? await observable.browseFiles(nodeId: playlist.key) {
                                                    observable.playNext(tracks: tracks)
                                                }
                                            }
                                        } label: { Label("Play Next", systemImage: "text.insert") }
                                        Button {
                                            Task {
                                                if let tracks = try? await observable.browseFiles(nodeId: playlist.key) {
                                                    observable.addTracksToQueue(tracks: tracks)
                                                }
                                            }
                                        } label: { Label("Add to Queue", systemImage: "text.append") }
                                        Button {
                                            Task {
                                                _ = try? await observable.togglePlaylistFavorite(key: playlist.key, name: playlist.name)
                                                await load()
                                            }
                                        } label: { Label("Remove from Favorites", systemImage: "star.slash") }
                                    }
                                }
                            }
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
            playlists = try await observable.favoritePlaylists()
        } catch {
            albums = []
            playlists = []
        }
        loading = false
    }
}
