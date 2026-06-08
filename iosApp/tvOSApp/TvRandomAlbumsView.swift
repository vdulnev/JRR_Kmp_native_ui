import SharedLogic
import SwiftUI

/// Random albums grid → album tracks.
struct TvRandomAlbumsView: View {
    @Environment(TvLibraryObservable.self) private var observable
    @State private var albums: [Album] = []
    @State private var loading = true

    private let columns = [GridItem(.adaptive(minimum: 260), spacing: 40)]

    var body: some View {
        NavigationStack {
            Group {
                if loading {
                    ProgressView()
                } else {
                    ScrollView {
                        LazyVGrid(columns: columns, spacing: 40) {
                            ForEach(albums, id: \.albumGroupId) { album in
                                NavigationLink {
                                    TvAlbumTracksView(album: album)
                                } label: {
                                    VStack(alignment: .leading) {
                                        TvArtwork(urlString: observable.artworkUrl(fileKey: album.artworkFileKey) ?? "", size: 260)
                                        Text(album.name.isEmpty ? "Unknown Album" : album.name)
                                            .font(.headline).lineLimit(1)
                                        Text(album.albumArtist).font(.subheadline)
                                            .foregroundStyle(.secondary).lineLimit(1)
                                    }
                                }
                                .buttonStyle(.card)
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
                        .padding(40)
                    }
                }
            }
            .navigationTitle("Random Albums")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { Task { await load() } } label: {
                        Label("Shuffle", systemImage: "arrow.clockwise")
                    }
                }
            }
        }
        .task { if albums.isEmpty { await load() } }
    }

    private func load() async {
        loading = true
        do {
            albums = try await observable.randomAlbums(limit: 24)
        } catch {
            albums = []
        }
        loading = false
    }
}
