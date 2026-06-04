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
                List {
                    Section {
                        Button {
                            container.facade.setQueue(tracks: tracks, startIndex: 0)
                            container.facade.play()
                        } label: { Label("Play All", systemImage: "play.fill") }
                    }
                    ForEach(Array(tracks.enumerated()), id: \.element.fileKey) { index, track in
                        Button {
                            container.facade.setQueue(tracks: tracks, startIndex: Int32(index))
                            container.facade.play()
                        } label: {
                            HStack(spacing: 16) {
                                TvArtwork(urlString: container.mcwsClient.buildImageUrl(fileKey: track.fileKey), size: 60)
                                VStack(alignment: .leading) {
                                    Text(track.name).lineLimit(1)
                                    Text(track.artist).font(.subheadline)
                                        .foregroundStyle(.secondary).lineLimit(1)
                                }
                                Spacer()
                            }
                        }
                    }
                }
            } else {
                Text("Empty").foregroundStyle(.secondary)
            }
        }
        .navigationTitle(title)
        .task { await load() }
    }

    private func load() async {
        do {
            let kids = try await container.libraryRepository.getBrowseChildren(parentId: nodeId)
            if kids.isEmpty {
                tracks = try await container.libraryRepository.getBrowseFiles(nodeId: nodeId)
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
