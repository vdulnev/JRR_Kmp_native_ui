import SharedLogic
import SwiftUI

/// Top-level library browse: album artists, drilling into their albums.
/// Calls `LibraryRepository` directly (suspend → async) — no ViewModel/facade
/// needed for read-only browsing.
struct TvLibraryView: View {
    @Environment(TvContainer.self) private var container
    @State private var artists: [String] = []
    @State private var loading = true
    @State private var error = ""

    var body: some View {
        NavigationStack {
            Group {
                if loading {
                    ProgressView("Loading library…")
                } else if !error.isEmpty {
                    Text(error).foregroundStyle(.red)
                } else {
                    List(artists, id: \.self) { artist in
                        NavigationLink(artist.isEmpty ? "Unknown Artist" : artist) {
                            TvArtistAlbumsView(artist: artist)
                        }
                    }
                }
            }
            .navigationTitle("Artists")
        }
        .task { await load() }
    }

    private func load() async {
        do {
            artists = try await container.libraryRepository.getArtists()
        } catch {
            self.error = "Failed to load: \(error.localizedDescription)"
        }
        loading = false
    }
}

/// Albums for a selected album artist.
struct TvArtistAlbumsView: View {
    @Environment(TvContainer.self) private var container
    let artist: String

    @State private var albums: [Album] = []
    @State private var loading = true

    var body: some View {
        Group {
            if loading {
                ProgressView()
            } else {
                List(albums, id: \.albumGroupId) { album in
                    VStack(alignment: .leading) {
                        Text(album.name.isEmpty ? "Unknown Album" : album.name)
                            .font(.headline)
                        if !album.date.isEmpty {
                            Text(album.date).font(.subheadline).foregroundStyle(.secondary)
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
            albums = try await container.libraryRepository.getAlbumsByArtist(artistName: artist)
        } catch {
            albums = []
        }
        loading = false
    }
}
