import SharedLogic
import SwiftUI

/// Full-screen now-playing detail: large artwork, progress, and full transport
/// (shuffle / prev / play-pause / next / repeat). Bound to the shared
/// `NowPlayingViewModel` via [NowPlayingObservable].
struct TvNowPlayingDetailView: View {
    @Bindable var model: NowPlayingObservable

    var body: some View {
        HStack(spacing: 80) {
            TvArtwork(urlString: model.imageUrl, size: 420)

            VStack(alignment: .leading, spacing: 24) {
                Text(model.trackTitle).font(.largeTitle.bold()).lineLimit(2)
                Text(model.artistName).font(.title2).foregroundStyle(.secondary)
                Text(model.albumTitle).font(.title3).foregroundStyle(.secondary)

                ProgressView(value: model.progress)
                    .tint(.yellow)
                HStack {
                    Text(timeString(model.positionMs))
                    Spacer()
                    Text(timeString(model.durationMs))
                }
                .font(.caption.monospacedDigit())
                .foregroundStyle(.secondary)

                HStack(spacing: 50) {
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
        .padding(80)
        .navigationTitle("Now Playing")
        .task { await model.observe() }
    }

    private func timeString(_ ms: Int64) -> String {
        let total = Int(ms / 1000)
        return String(format: "%d:%02d", total / 60, total % 60)
    }
}
