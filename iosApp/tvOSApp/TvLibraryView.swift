import SharedLogic
import SwiftUI

/// Artists library sub-tab: album artists → albums → tracks. Reads the library
/// via `LibraryRepository` directly (suspend → async).
struct TvArtistsView: View {
    @Environment(TvLibraryObservable.self) private var observable
    @State private var artists: [String] = []
    @State private var loading = true
    @State private var error = ""

    var body: some View {
        NavigationStack {
            content.navigationTitle("Artists")
        }
        .task { await load() }
    }

    @ViewBuilder private var content: some View {
        if loading {
            ProgressView("Loading library…").frame(maxHeight: .infinity)
        } else if !error.isEmpty {
            Text(error).foregroundStyle(.red).frame(maxHeight: .infinity)
        } else {
            List(artists, id: \.self) { artist in
                NavigationLink {
                    TvArtistAlbumsView(artist: artist)
                } label: {
                    Text(artist.isEmpty ? "Unknown Artist" : artist)
                }
                .contextMenu {
                    Button {
                        Task {
                            if let albums = try? await observable.albums(artist: artist) {
                                var allTracks: [Track] = []
                                for album in albums {
                                    if let tracks = try? await observable.albumTracks(album: album) {
                                        allTracks.append(contentsOf: tracks)
                                    }
                                }
                                observable.play(tracks: allTracks, startIndex: 0)
                            }
                        }
                    } label: { Label("Play", systemImage: "play") }
                    Button {
                        Task {
                            if let albums = try? await observable.albums(artist: artist) {
                                var allTracks: [Track] = []
                                for album in albums {
                                    if let tracks = try? await observable.albumTracks(album: album) {
                                        allTracks.append(contentsOf: tracks)
                                    }
                                }
                                observable.playNext(tracks: allTracks)
                            }
                        }
                    } label: { Label("Play Next", systemImage: "text.insert") }
                    Button {
                        Task {
                            if let albums = try? await observable.albums(artist: artist) {
                                var allTracks: [Track] = []
                                for album in albums {
                                    if let tracks = try? await observable.albumTracks(album: album) {
                                        allTracks.append(contentsOf: tracks)
                                    }
                                }
                                observable.addTracksToQueue(tracks: allTracks)
                            }
                        }
                    } label: { Label("Add to Queue", systemImage: "text.append") }
                }
            }
        }
    }

    private func load() async {
        do {
            artists = try await observable.artists()
        } catch {
            self.error = "Failed to load: \(error.localizedDescription)"
        }
        loading = false
    }
}

/// Albums for a selected album artist.
struct TvArtistAlbumsView: View {
    @Environment(TvLibraryObservable.self) private var observable
    let artist: String

    @State private var albums: [Album] = []
    @State private var loading = true
    @State private var favoriteAlbumKeys: Set<String> = []

    var body: some View {
        Group {
            if loading {
                ProgressView()
            } else {
                List(albums, id: \.albumGroupId) { album in
                    NavigationLink {
                        TvAlbumTracksView(album: album)
                    } label: {
                        HStack(spacing: 16) {
                            TvArtwork(urlString: observable.artworkUrl(fileKey: album.artworkFileKey) ?? "", size: 80)
                            VStack(alignment: .leading) {
                                HStack(spacing: 8) {
                                    Text(album.name.isEmpty ? "Unknown Album" : album.name)
                                        .font(.headline)
                                    if favoriteAlbumKeys.contains(album.albumGroupId) {
                                        Image(systemName: "star.fill")
                                            .foregroundColor(.accentColor)
                                            .font(.system(size: 14))
                                    }
                                }
                                if !album.date.isEmpty {
                                    Text(album.date).font(.subheadline).foregroundStyle(.secondary)
                                }
                            }
                        }
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
                        Button {
                            Task {
                                if let nowFav = try? await observable.toggleAlbumFavorite(album: album) {
                                    if nowFav {
                                        favoriteAlbumKeys.insert(album.albumGroupId)
                                    } else {
                                        favoriteAlbumKeys.remove(album.albumGroupId)
                                    }
                                }
                            }
                        } label: {
                            let isFav = favoriteAlbumKeys.contains(album.albumGroupId)
                            Label(isFav ? "Remove from Favorites" : "Add to Favorites", systemImage: isFav ? "star.fill" : "star")
                        }
                    }
                }
            }
        }
        .navigationTitle(artist.isEmpty ? "Unknown Artist" : artist)
        .task { await load() }
    }

    private func load() async {
        do {
            albums = try await observable.albums(artist: artist)
            if let favs = try? await observable.favoriteAlbums() {
                favoriteAlbumKeys = Set(favs.map(\.albumGroupId))
            }
        } catch {
            albums = []
            favoriteAlbumKeys = []
        }
        loading = false
    }
}
