import SharedLogic
import SwiftUI

/// Full-screen now-playing detail: artwork, progress, and full transport
/// (shuffle / prev / play-pause / next / repeat) on the left, with the live
/// playing queue on the right. Bound to the shared `NowPlayingViewModel` /
/// `QueueViewModel` via their observables.
struct TvNowPlayingDetailView: View {
    @Bindable var model: NowPlayingObservable
    @Bindable var queue: QueueObservable

    var body: some View {
        HStack(alignment: .top, spacing: 60) {
            nowPlayingPanel
                .frame(maxWidth: .infinity, alignment: .leading)

            queuePanel
                .frame(width: 620)
        }
        .padding(80)
        .navigationTitle("Now Playing")
        .task { await model.observe() }
        .task { await queue.observe() }
    }

    // MARK: - Left: now playing + transport

    private var nowPlayingPanel: some View {
        HStack(alignment: .top, spacing: 60) {
            TvArtwork(urlString: model.imageUrl, size: 360)

            VStack(alignment: .leading, spacing: 24) {
                Text(model.trackTitle).font(.title.bold()).lineLimit(2)
                Text(model.artistName).font(.title3).foregroundStyle(.secondary)
                Text(model.albumTitle).font(.body).foregroundStyle(.secondary)

                ProgressView(value: model.progress)
                    .tint(.yellow)
                HStack {
                    Text(timeString(model.positionMs))
                    Spacer()
                    Text(timeString(model.durationMs))
                }
                .font(.caption.monospacedDigit())
                .foregroundStyle(.secondary)

                HStack(spacing: 40) {
                    Button { model.toggleShuffle() } label: {
                        Image(systemName: "shuffle")
                            .foregroundStyle(model.shuffleOn ? AnyShapeStyle(.tint) : AnyShapeStyle(.secondary))
                    }
                    Button { model.previous() } label: { Image(systemName: "backward.fill") }
                    Button { model.playPause() } label: {
                        Image(systemName: model.isPlaying ? "pause.circle.fill" : "play.circle.fill")
                            .font(.system(size: 60))
                    }
                    Button { model.next() } label: { Image(systemName: "forward.fill") }
                    Button { model.toggleRepeat() } label: {
                        Image(systemName: model.repeatLabel == "Track" ? "repeat.1" : "repeat")
                            .foregroundStyle(model.repeatLabel == "Off" ? AnyShapeStyle(.secondary) : AnyShapeStyle(.tint))
                    }
                }
                .padding(.top, 16)
            }
            .frame(maxWidth: 700, alignment: .leading)
        }
    }

    // MARK: - Right: playing queue

    private var queuePanel: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Queue").font(.title2.bold())

            if queue.queueTracks.isEmpty {
                Text("Queue is empty")
                    .foregroundStyle(.secondary)
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(Array(queue.queueTracks.enumerated()), id: \.element.fileKey) { index, track in
                            queueRow(index: index, track: track)
                        }
                    }
                }
            }
        }
    }

    private func queueRow(index: Int, track: Track) -> some View {
        let isActive = index == queue.activeIndex
        let isFav = queue.favoritedTrackKeys.contains(track.fileKey)
        return Button {
            queue.playByIndex(index)
        } label: {
            HStack(spacing: 14) {
                Image(systemName: isActive ? "speaker.wave.2.fill" : "music.note")
                    .foregroundStyle(isActive ? AnyShapeStyle(.tint) : AnyShapeStyle(.secondary))
                    .frame(width: 28)
                VStack(alignment: .leading, spacing: 2) {
                    HStack {
                        Text(track.name)
                            .lineLimit(1)
                            .foregroundStyle(isActive ? AnyShapeStyle(.tint) : AnyShapeStyle(.primary))
                        if isFav {
                            Image(systemName: "star.fill")
                                .foregroundColor(.accentColor)
                                .font(.system(size: 14))
                        }
                    }
                    Text(track.artist)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }
                Spacer(minLength: 0)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .buttonStyle(.borderless)
        .contextMenu {
            Button {
                queue.toggleFavoriteTrack(track)
            } label: {
                Label(isFav ? "Remove from Favorites" : "Add to Favorites", systemImage: isFav ? "star.fill" : "star")
            }
            Button {
                queue.removeQueueTrack(index)
            } label: {
                Label("Remove from Queue", systemImage: "trash")
            }
        }
    }

    private func timeString(_ ms: Int64) -> String {
        let total = Int(ms / 1000)
        return String(format: "%d:%02d", total / 60, total % 60)
    }
}
