import SharedLogic
import SwiftUI

/// Random albums grid → album tracks.
struct TvRandomAlbumsView: View {
    @Environment(TvContainer.self) private var container
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
                                        TvArtwork(urlString: container.mcwsClient.buildImageUrl(fileKey: album.artworkFileKey), size: 260)
                                        Text(album.name.isEmpty ? "Unknown Album" : album.name)
                                            .font(.headline).lineLimit(1)
                                        Text(album.albumArtist).font(.subheadline)
                                            .foregroundStyle(.secondary).lineLimit(1)
                                    }
                                }
                                .buttonStyle(.card)
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
            albums = try await container.libraryRepository.getRandomAlbums(limit: 24)
        } catch {
            albums = []
        }
        loading = false
    }
}
