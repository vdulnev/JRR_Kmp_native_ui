import SharedLogic
import SwiftUI

private let log = SwiftLog("ui:iOS:NowPlaying")

@Observable
@MainActor
class NowPlayingObservable {
    let viewModel: NowPlayingViewModel

    var trackTitle: String = "Idle"
    var artistName: String = "Unknown Artist"
    var albumTitle: String = "Unknown Album"
    var isPlaying: Bool = false
    var positionMs: Int64 = 0
    var durationMs: Int64 = 0
    var volume: Float = 0.5
    var isMuted: Bool = false
    var shuffleMode: ShuffleMode = .off
    var repeatMode: RepeatMode = .off
    var sampleRate: Int32 = 0
    var activeZoneName: String = "No Zone Selected"
    var transientError: String?
    var imageUrl: String = ""

    init(viewModel: NowPlayingViewModel) {
        self.viewModel = viewModel
        // Cheap, synchronous seed only. NO flow subscription here: this object is
        // created via `@State(initialValue:)`, whose argument SwiftUI evaluates on
        // every `NowPlayingView.init` — including the throwaway re-inits triggered
        // when an ancestor body re-runs (e.g. once per playback tick). Subscribing
        // in `init` made each throwaway subscribe-then-cancel a Kotlin flow and log
        // init/deinit ~1/sec. The real subscription now runs from the view's
        // `.task` (see `observe()`), which fires once per view *identity*.
        sync(state: viewModel.state.value)
    }

    /// Drives the live state subscription. Call from the owning view's `.task`
    /// so it starts once per view identity and is cancelled automatically when
    /// the view disappears — never from a discarded `@State(initialValue:)` copy.
    func observe() async {
        log.d("observe: start")
        defer { log.d("observe: end") }
        for await state in viewModel.state {
            sync(state: state)
        }
    }

    private func sync(state: NowPlayingViewState) {
        // Only assign when the value actually changed. The `@Observable` macro's
        // generated setter fires an invalidation on EVERY assignment (it does
        // not compare old vs. new), so blindly re-assigning all fields on each
        // ~1/sec state tick would invalidate every view that reads any field —
        // even stable ones like `trackTitle`. That re-evaluates the whole shell
        // (and churns its child observables) once per second during playback.
        // Position/duration/isPlaying still change every tick and correctly
        // re-render only the leaf views that read them.
        if trackTitle != state.trackTitle { trackTitle = state.trackTitle }
        if artistName != state.artistName { artistName = state.artistName }
        if albumTitle != state.albumTitle { albumTitle = state.albumTitle }
        if isPlaying != state.isPlaying { isPlaying = state.isPlaying }
        if positionMs != state.positionMs { positionMs = state.positionMs }
        if durationMs != state.durationMs { durationMs = state.durationMs }
        if volume != state.volume { volume = state.volume }
        if isMuted != state.isMuted { isMuted = state.isMuted }
        if shuffleMode != state.shuffleMode { shuffleMode = state.shuffleMode }
        if repeatMode != state.repeatMode { repeatMode = state.repeatMode }
        if sampleRate != state.sampleRate { sampleRate = state.sampleRate }
        if activeZoneName != state.activeZoneName { activeZoneName = state.activeZoneName }
        if transientError != state.transientError { transientError = state.transientError }
        if imageUrl != state.imageUrl { imageUrl = state.imageUrl }
    }

    func play() {
        viewModel.play()
    }

    func pause() {
        viewModel.pause()
    }

    func stop() {
        viewModel.stop()
    }

    func next() {
        viewModel.next()
    }

    func previous() {
        viewModel.previous()
    }

    func seekTo(positionMs: Int64) {
        viewModel.seekTo(positionMs: positionMs)
    }

    func setVolume(level: Float) {
        viewModel.setVolume(level: level)
    }

    func toggleShuffle() {
        viewModel.toggleShuffle()
    }

    func toggleRepeat() {
        viewModel.toggleRepeat()
    }

    func clearTransientError() {
        viewModel.clearTransientError()
    }
}

struct NowPlayingView: View {
    @State private var observable: NowPlayingObservable
    let onQueueClick: () -> Void

    @State private var isScrubbing = false
    @State private var scrubProgress: Double = 0.0

    init(viewModel: NowPlayingViewModel, onQueueClick: @escaping () -> Void) {
        _observable = State(initialValue: NowPlayingObservable(viewModel: viewModel))
        self.onQueueClick = onQueueClick
    }

    var body: some View {
        let isPlaying = observable.isPlaying
        let currentPosition = observable.positionMs
        let duration = observable.durationMs
        let displayProgress = isScrubbing ? scrubProgress : (duration > 0 ? Double(currentPosition) / Double(duration) : 0.0)

        VStack(spacing: 0) {
            // Header
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("NOW PLAYING")
                        .styleSectionLabel()

                    Text(observable.activeZoneName)
                        .font(AppFont.inter(size: 14, weight: .semibold))
                        .foregroundColor(.textSecondary)
                }

                Spacer()

                Button(action: onQueueClick) {
                    Image(systemName: "list.bullet")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(.textPrimary)
                        .frame(width: 44, height: 44)
                }
            }
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)
            .padding(.top, 16)

            Spacer()

            // Vinyl Sleeve Hero
            VinylSleeve(
                albumTitle: observable.albumTitle,
                artistName: observable.artistName,
                year: "2026", // Fallback decorative year
                side: "SIDE A",
                imageUrl: observable.imageUrl.isEmpty ? nil : observable.imageUrl,
                isPlaying: isPlaying,
            )
            .padding(.vertical, 20)

            Spacer()

            // Metadata & details
            VStack(spacing: 8) {
                Text(observable.trackTitle)
                    .styleNowPlayingTitle()
                    .lineLimit(1)
                    .multilineTextAlignment(.center)

                Text(observable.artistName + " — " + observable.albumTitle)
                    .styleItemSubtitle()
                    .lineLimit(1)
                    .multilineTextAlignment(.center)

                // Format Badge
                if observable.sampleRate > 0 {
                    Text("AUDIO | \(observable.sampleRate / 1000)KHZ")
                        .font(AppFont.ibmPlexMono(size: 9, weight: .medium))
                        .tracking(1.6)
                        .foregroundColor(.textTertiary)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(Color.bg2)
                        .cornerRadius(4)
                        .overlay(
                            RoundedRectangle(cornerRadius: 4)
                                .stroke(Color.line2, lineWidth: 1),
                        )
                }
            }
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)

            Spacer()

            // Scrub Slider
            VStack(spacing: 6) {
                Slider(
                    value: Binding(
                        get: { displayProgress },
                        set: { newValue in
                            isScrubbing = true
                            scrubProgress = newValue
                        },
                    ),
                    in: 0.0 ... 1.0,
                    onEditingChanged: { editing in
                        if !editing {
                            isScrubbing = false
                            if duration > 0 {
                                observable.seekTo(positionMs: Int64(scrubProgress * Double(duration)))
                            }
                        }
                    },
                )
                .tint(.accentColor)

                HStack {
                    let currentSecs = isScrubbing ? Int64(scrubProgress * Double(duration) / 1000) : currentPosition / 1000
                    let totalSecs = duration / 1000

                    Text(formatTime(seconds: currentSecs))
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .foregroundColor(.textSecondary)

                    Spacer()

                    Text("-" + formatTime(seconds: max(0, totalSecs - currentSecs)))
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .foregroundColor(.textSecondary)
                }
            }
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)

            Spacer()

            // Transport Controls Row
            HStack(spacing: 0) {
                // Shuffle Button
                Button(action: {
                    observable.toggleShuffle()
                }) {
                    VStack(spacing: 2) {
                        Image(systemName: "shuffle")
                            .font(.system(size: 18))
                        if observable.shuffleMode == .automatic {
                            Text("AUTO")
                                .font(AppFont.ibmPlexMono(size: 8, weight: .bold))
                        }
                    }
                    .foregroundColor(observable.shuffleMode != .off ? .accentColor : .textTertiary)
                    .frame(width: 44, height: 44)
                }

                Spacer()

                // Previous Button
                Button(action: { observable.previous() }) {
                    Image(systemName: "backward.end.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.textPrimary)
                        .frame(width: 44, height: 44)
                }

                Spacer()

                // Play / Pause Circle Disc
                Button(action: {
                    if isPlaying {
                        observable.pause()
                    } else {
                        observable.play()
                    }
                }) {
                    ZStack {
                        Circle()
                            .fill(Color.accentColor)
                            .frame(width: 60, height: 60)
                            .shadow(color: Color.accentColor.opacity(0.45), radius: 24, x: 0, y: 8)

                        Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.bg0)
                            .offset(x: isPlaying ? 0 : 2) // optical centering
                    }
                }

                Spacer()

                // Next Button
                Button(action: { observable.next() }) {
                    Image(systemName: "forward.end.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.textPrimary)
                        .frame(width: 44, height: 44)
                }

                Spacer()

                // Repeat Button
                Button(action: {
                    observable.toggleRepeat()
                }) {
                    Image(systemName: observable.repeatMode == .track ? "repeat.1" : "repeat")
                        .font(.system(size: 18))
                        .foregroundColor(observable.repeatMode != .off ? .accentColor : .textTertiary)
                        .frame(width: 44, height: 44)
                }
            }
            .padding(.vertical, 8)
            .padding(.horizontal, 18)
            // Glass control bar (OS 26+). `clipsContent: false` lets the gold
            // play-disc's glow bleed past the capsule; clear fallbacks keep the
            // pre-26 look unchanged (no bar behind the controls).
            .liquidGlass(
                in: Capsule(style: .continuous),
                fallbackFill: .clear,
                fallbackBorder: .clear,
                clipsContent: false,
            )
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)

            Spacer()

            // Volume Slider Row
            HStack(spacing: 12) {
                let volume = observable.volume
                Image(systemName: volume == 0 ? "speaker.slash.fill" : "speaker.wave.2.fill")
                    .font(.system(size: 14))
                    .foregroundColor(.textTertiary)
                    .frame(width: 20)

                Slider(
                    value: Binding(
                        get: { Double(volume) },
                        set: { observable.setVolume(level: Float($0)) },
                    ),
                    in: 0.0 ... 1.0,
                )
                .tint(.textSecondary)

                Text("\(Int(volume * 100))")
                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                    .foregroundColor(.textTertiary)
                    .frame(width: 24, alignment: .trailing)
            }
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)
            .padding(.bottom, 24)
        }
        .background(Color.bg1.ignoresSafeArea())
        .task { await observable.observe() }
    }

    private func formatTime(seconds: Int64) -> String {
        let m = seconds / 60
        let s = seconds % 60
        return String(format: "%d:%02d", m, s)
    }
}
