import SharedLogic
import SwiftUI

/// Searches the library over MCWS and plays a result on the active zone.
struct TvSearchView: View {
    @Environment(TvContainer.self) private var container

    @State private var query = ""
    @State private var results: [Track] = []
    @State private var searching = false

    var body: some View {
        VStack {
            TextField("Search artists, albums, tracks", text: $query)
                .padding(.horizontal, 60)
                .onSubmit { runSearch() }

            if searching {
                ProgressView().frame(maxHeight: .infinity)
            } else {
                List(Array(results.enumerated()), id: \.element.fileKey) { index, track in
                    Button {
                        play(from: index)
                    } label: {
                        HStack(spacing: 16) {
                            TvArtwork(urlString: container.mcwsClient.buildImageUrl(fileKey: track.fileKey), size: 60)
                            VStack(alignment: .leading) {
                                Text(track.name).lineLimit(1)
                                Text("\(track.artist) • \(track.album)")
                                    .font(.subheadline).foregroundStyle(.secondary).lineLimit(1)
                            }
                            Spacer()
                        }
                    }
                }
            }
        }
        .navigationTitle("Search")
    }

    private func runSearch() {
        let q = query.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !q.isEmpty else { return }
        searching = true
        Task {
            defer { searching = false }
            // MCWS keyword search across the audio library.
            let mcwsQuery = "[Media Type]=[Audio] ([Name]=[\(q)] OR [Artist]=[\(q)] OR [Album]=[\(q)])"
            do {
                results = try await container.mcwsClient.searchTracks(query: mcwsQuery)
            } catch {
                results = []
            }
        }
    }

    private func play(from index: Int) {
        container.facade.setQueue(tracks: results, startIndex: Int32(index))
        container.facade.play()
    }
}
