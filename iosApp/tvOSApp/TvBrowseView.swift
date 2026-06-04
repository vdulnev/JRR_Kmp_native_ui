import SharedLogic
import SwiftUI

/// Browse the MCWS library tree. Each node loads its child folders; a leaf node
/// (no children) loads its tracks instead. Mirrors the other platforms' Browse
/// tab. Root node id is "-1".
struct TvBrowseView: View {
    var body: some View {
        NavigationStack {
            TvBrowseNodeView(nodeId: "-1", title: "Browse")
        }
    }
}

struct TvBrowseNodeView: View {
    @Environment(TvContainer.self) private var container
    let nodeId: String
    let title: String

    @State private var children: [BrowseItem] = []
    @State private var tracks: [Track] = []
    @State private var artistGroups: [ArtistTrackGroup] = []
    @State private var grouped = true
    @State private var loading = true

    var body: some View {
        Group {
            if loading {
                ProgressView()
            } else if !children.isEmpty {
                List(children, id: \.key) { item in
                    NavigationLink(item.name) {
                        TvBrowseNodeView(nodeId: item.key, title: item.name)
                    }
                }
            } else if !tracks.isEmpty {
                trackList
            } else {
                Text("Empty").foregroundStyle(.secondary)
            }
        }
        .navigationTitle(title)
        .toolbar {
            if !tracks.isEmpty {
                ToolbarItem(placement: .topBarTrailing) {
                    Button { grouped.toggle() } label: {
                        Label(grouped ? "Grouped" : "Flat",
                              systemImage: grouped ? "rectangle.grid.1x2" : "list.bullet")
                    }
                }
            }
        }
        .task { await load() }
    }

    @ViewBuilder private var trackList: some View {
        List {
            Section {
                Button {
                    play(tracks, from: 0)
                } label: { Label("Play All", systemImage: "play.fill") }
            }
            if grouped {
                // Album Artist → Album sections; multi-disc albums folded by
                // the shared groupTracksByArtistAndAlbum (BrowseTrackGrouping.kt).
                ForEach(Array(artistGroups.enumerated()), id: \.offset) { _, artistGroup in
                    ForEach(Array(artistGroup.albums.enumerated()), id: \.offset) { _, album in
                        Section {
                            Button {
                                play(album.tracks, from: 0)
                            } label: {
                                HStack(spacing: 16) {
                                    TvArtwork(urlString: container.mcwsClient.buildImageUrl(fileKey: album.artworkFileKey), size: 60)
                                    Text(album.name.isEmpty ? "Unknown Album" : album.name).font(.headline)
                                    Spacer()
                                    Image(systemName: "play.fill").foregroundStyle(.secondary)
                                }
                            }
                            ForEach(Array(album.tracks.enumerated()), id: \.element.fileKey) { index, track in
                                Button { play(album.tracks, from: index) } label: { trackRow(track) }
                            }
                        } header: {
                            Text("\(artistGroup.artist) — \(album.name.isEmpty ? "Unknown Album" : album.name)")
                        }
                    }
                }
            } else {
                ForEach(Array(tracks.enumerated()), id: \.element.fileKey) { index, track in
                    Button { play(tracks, from: index) } label: { trackRow(track) }
                }
            }
        }
    }

    private func trackRow(_ track: Track) -> some View {
        HStack(spacing: 16) {
            TvArtwork(urlString: container.mcwsClient.buildImageUrl(fileKey: track.fileKey), size: 60)
            VStack(alignment: .leading) {
                Text(track.name).lineLimit(1)
                Text(track.artist).font(.subheadline).foregroundStyle(.secondary).lineLimit(1)
            }
            Spacer()
        }
    }

    private func play(_ queue: [Track], from index: Int) {
        container.facade.setQueue(tracks: queue, startIndex: Int32(index))
        container.facade.play()
    }

    private func load() async {
        do {
            let kids = try await container.libraryRepository.getBrowseChildren(parentId: nodeId)
            if kids.isEmpty {
                let files = try await container.libraryRepository.getBrowseFiles(nodeId: nodeId)
                tracks = files
                artistGroups = BrowseTrackGroupingKt.groupTracksByArtistAndAlbum(tracks: files)
            } else {
                children = kids
            }
        } catch {
            children = []
            tracks = []
        }
        loading = false
    }
}
