import SharedLogic
import SwiftUI

/// Artists library sub-tab: album artists → albums → tracks. Reads the library
/// via `LibraryRepository` directly (suspend → async).
struct TvArtistsView: View {
    @Environment(TvContainer.self) private var container
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
                NavigationLink(artist.isEmpty ? "Unknown Artist" : artist) {
                    TvArtistAlbumsView(artist: artist)
                }
            }
        }
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
                    NavigationLink {
                        TvAlbumTracksView(album: album)
                    } label: {
                        HStack(spacing: 16) {
                            TvArtwork(urlString: container.mcwsClient.buildImageUrl(fileKey: album.artworkFileKey), size: 80)
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

