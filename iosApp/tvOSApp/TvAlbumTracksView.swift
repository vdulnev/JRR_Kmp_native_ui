import SharedLogic
import SwiftUI

/// Track list for an album, with play actions that send the queue to the
/// active JRiver zone via the facade.
struct TvAlbumTracksView: View {
    @Environment(TvLibraryObservable.self) private var observable
    let album: Album

    @State private var tracks: [Track] = []
    @State private var loading = true
    @State private var favoritedTrackKeys: Set<String> = []
    @State private var isAlbumFav = false

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
                        Button {
                            Task {
                                if let nowFav = try? await observable.toggleAlbumFavorite(album: album) {
                                    isAlbumFav = nowFav
                                }
                            }
                        } label: {
                            Label(isAlbumFav ? "Remove Album from Favorites" : "Add Album to Favorites", systemImage: isAlbumFav ? "star.fill" : "star")
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
                                     if favoritedTrackKeys.contains(track.fileKey) {
                                         Spacer().frame(width: 8)
                                         Image(systemName: "star.fill")
                                             .foregroundColor(.accentColor)
                                             .font(.system(size: 14))
                                     }
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
                                 Button {
                                     Task {
                                         if let nowFav = try? await observable.toggleTrackFavorite(track: track) {
                                             if nowFav {
                                                 favoritedTrackKeys.insert(track.fileKey)
                                             } else {
                                                 favoritedTrackKeys.remove(track.fileKey)
                                             }
                                         }
                                     }
                                 } label: {
                                     let isFav = favoritedTrackKeys.contains(track.fileKey)
                                     Label(isFav ? "Remove from Favorites" : "Add to Favorites", systemImage: isFav ? "star.fill" : "star")
                                 }
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
             if let favs = try? await observable.favoriteTracks() {
                 favoritedTrackKeys = Set(favs.map(\.fileKey))
             }
             isAlbumFav = (try? await observable.isAlbumFavorite(name: album.name, artist: album.albumArtist)) ?? false
         } catch {
             tracks = []
         }
        loading = false
    }

    private func play(from index: Int) {
        observable.play(tracks: tracks, startIndex: index)
    }
}
