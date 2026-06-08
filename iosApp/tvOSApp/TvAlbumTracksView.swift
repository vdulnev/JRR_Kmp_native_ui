import SharedLogic
import SwiftUI

/// Track list for an album, with play actions that send the queue to the
/// active JRiver zone via the facade.
struct TvAlbumTracksView: View {
    @Environment(TvLibraryObservable.self) private var observable
    let album: Album

    @State private var tracks: [Track] = []
    @State private var loading = true

    var body: some View {
        Group {
            if loading {
                ProgressView()
            } else {
                List {
                    Section {
                        Button {
                            play(from: 0)
                        } label: {
                            Label("Play Album", systemImage: "play.fill")
                        }
                    }
                    Section(album.name.isEmpty ? "Tracks" : album.name) {
                        ForEach(Array(tracks.enumerated()), id: \.element.fileKey) { index, track in
                            Button {
                                play(from: index)
                            } label: {
                                HStack {
                                    Text("\(track.trackNumber)")
                                        .foregroundStyle(.secondary)
                                        .frame(width: 60, alignment: .trailing)
                                    Text(track.name)
                                    Spacer()
                                }
                            }
                            .contextMenu {
                                Button {
                                    observable.play(tracks: [track], startIndex: 0)
                                } label: { Label("Play", systemImage: "play") }
                                Button {
                                    observable.playNext(tracks: [track])
                                } label: { Label("Play Next", systemImage: "text.insert") }
                                Button {
                                    observable.addTracksToQueue(tracks: [track])
                                } label: { Label("Add to Queue", systemImage: "text.append") }
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle(album.name.isEmpty ? "Album" : album.name)
        .task { await load() }
    }

    private func load() async {
        do {
            tracks = try await observable.albumTracks(album: album)
        } catch {
            tracks = []
        }
        loading = false
    }

    private func play(from index: Int) {
        observable.play(tracks: tracks, startIndex: index)
    }
}
