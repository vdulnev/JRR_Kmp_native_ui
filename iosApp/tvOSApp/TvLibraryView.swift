import SharedLogic
import SwiftUI

/// Top-level library browse: album artists → albums → tracks, with a zone
/// picker and a now-playing transport bar. Reads the library via
/// `LibraryRepository` directly (suspend → async).
struct TvLibraryView: View {
    @Environment(TvContainer.self) private var container
    @State private var artists: [String] = []
    @State private var loading = true
    @State private var error = ""
    @State private var nowPlaying: NowPlayingObservable?

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                content
                if let nowPlaying {
                    TvNowPlayingBar(model: nowPlaying)
                        .task { await nowPlaying.observe() }
                }
            }
            .navigationTitle("Artists")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink {
                        TvSearchView()
                    } label: {
                        Label("Search", systemImage: "magnifyingglass")
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink {
                        TvZonesView()
                    } label: {
                        Label("Zone", systemImage: "hifispeaker.2.fill")
                    }
                }
            }
        }
        .task { await load() }
        .onAppear {
            if nowPlaying == nil {
                nowPlaying = NowPlayingObservable(viewModel: container.makeNowPlayingViewModel())
            }
        }
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

/// Bottom transport bar reflecting the active zone's now-playing state.
struct TvNowPlayingBar: View {
    @Bindable var model: NowPlayingObservable

    var body: some View {
        HStack(spacing: 24) {
            TvArtwork(urlString: model.imageUrl, size: 60)
            VStack(alignment: .leading) {
                Text(model.trackTitle).font(.headline).lineLimit(1)
                Text(model.hasTrack ? model.artistName : model.activeZoneName)
                    .font(.subheadline).foregroundStyle(.secondary).lineLimit(1)
            }
            Spacer()
            Button { model.previous() } label: { Image(systemName: "backward.fill") }
            Button { model.playPause() } label: {
                Image(systemName: model.isPlaying ? "pause.fill" : "play.fill")
            }
            Button { model.next() } label: { Image(systemName: "forward.fill") }
        }
        .padding(.horizontal, 60)
        .padding(.vertical, 20)
        .background(.ultraThinMaterial)
    }
}
