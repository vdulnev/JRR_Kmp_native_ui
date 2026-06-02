import SharedLogic
import SwiftUI

private let log = SwiftLog("ui:iOS:Queue")

@Observable
@MainActor
class QueueObservable {
    let viewModel: QueueViewModel

    var queueTracks: [Track] = []
    var activeIndex: Int = -1
    var isPlaying: Bool = false
    var isLoading: Bool = false
    var isLocal: Bool = true
    var transientError: String?
    var downloadedTrackKeys: Set<String> = []

    init(viewModel: QueueViewModel) {
        self.viewModel = viewModel
        // Cheap synchronous seed only. The live subscription runs from the owning
        // view's `.task` (see `observe()`), never from `init`. SwiftUI re-runs
        // `init` for the throwaway `@State(initialValue:)` copy each time an
        // ancestor body re-evaluates (e.g. ~1/sec during playback); subscribing
        // here made every throwaway subscribe-then-cancel a Kotlin flow and log
        // init/deinit once per second.
        sync(state: viewModel.state.value)
    }

    /// Drives the live state subscription. Call from the owning view's `.task`
    /// so it starts once per view identity and stops when the view disappears.
    func observe() async {
        log.d("observe: start")
        defer { log.d("observe: end") }
        for await state in viewModel.state {
            sync(state: state)
        }
    }

    private func sync(state: QueueViewState) {
        queueTracks = state.queueTracks
        activeIndex = Int(state.activeIndex)
        isPlaying = state.isPlaying
        isLoading = state.isLoading
        isLocal = state.isLocal
        transientError = state.transientError
        downloadedTrackKeys = state.downloadedTrackKeys
    }

    func playByIndex(index: Int) {
        viewModel.playByIndex(index: Int32(index))
    }

    func removeQueueTrack(index: Int) {
        viewModel.removeQueueTrack(index: Int32(index))
    }

    func moveQueueTrack(from: Int, to: Int) {
        viewModel.moveQueueTrack(from: Int32(from), to: Int32(to))
    }

    func clearQueue() {
        viewModel.clearQueue()
    }

    func clearTransientError() {
        viewModel.clearTransientError()
    }
}

struct QueueView: View {
    @State private var observable: QueueObservable
    let onBackClick: () -> Void
    /// Large-screen queue rail: no back button (the queue is always visible
    /// beside Now Playing); shows the track count instead.
    var isRail: Bool = false

    @State private var editMode: EditMode = .inactive

    init(viewModel: QueueViewModel, onBackClick: @escaping () -> Void, isRail: Bool = false) {
        _observable = State(initialValue: QueueObservable(viewModel: viewModel))
        self.onBackClick = onBackClick
        self.isRail = isRail
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                if isRail {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("PLAY QUEUE")
                            .styleSectionLabel()
                        Text("\(observable.queueTracks.count) \(observable.queueTracks.count == 1 ? "TRACK" : "TRACKS")")
                            .font(AppFont.ibmPlexMono(size: 10, weight: .regular))
                            .tracking(1.2)
                            .foregroundColor(.textTertiary)
                    }
                    Spacer()
                } else {
                    Button(action: onBackClick) {
                        HStack(spacing: 4) {
                            Image(systemName: "chevron.left")
                                .font(.system(size: 16, weight: .bold))
                            Text("BACK")
                                .font(AppFont.ibmPlexMono(size: 11, weight: .medium))
                        }
                        .foregroundColor(.textPrimary)
                        .frame(height: 44)
                    }

                    Spacer()

                    Text("PLAY QUEUE")
                        .styleSectionLabel()

                    Spacer()
                }

                HStack(spacing: 12) {
                    Button(action: {
                        withAnimation {
                            editMode = editMode == .active ? .inactive : .active
                        }
                    }) {
                        Text(editMode == .active ? "DONE" : "EDIT")
                            .font(AppFont.ibmPlexMono(size: 11, weight: .medium))
                            .foregroundColor(.accentColor)
                            .frame(height: 44)
                    }

                    Button(action: { observable.clearQueue() }) {
                        Text("CLEAR")
                            .font(AppFont.ibmPlexMono(size: 11, weight: .medium))
                            .foregroundColor(.errorColor)
                            .frame(height: 44)
                    }
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .background(Color.bg1)

            // Queue Content
            if observable.isLoading {
                Spacer()
                ProgressView()
                    .tint(.accentColor)
                Spacer()
            } else if observable.queueTracks.isEmpty {
                Spacer()
                Text("QUEUE IS EMPTY")
                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                    .foregroundColor(.textTertiary)
                Spacer()
            } else {
                List {
                    ForEach(Array(observable.queueTracks.enumerated()), id: \.element.fileKey) { index, track in
                        let isActive = index == observable.activeIndex

                        HStack(spacing: 12) {
                            // Index / VuMeter
                            Group {
                                if isActive {
                                    VuMeter(isPlaying: observable.isPlaying)
                                } else {
                                    Text(String(format: "%02d", index + 1))
                                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                                        .foregroundColor(.textTertiary)
                                }
                            }
                            .frame(width: 24, alignment: .leading)

                            // Track info
                            VStack(alignment: .leading, spacing: 2) {
                                Text(track.name)
                                    .font(AppFont.inter(size: 15, weight: .medium))
                                    .foregroundColor(isActive ? .accentColor : .textPrimary)
                                    .lineLimit(1)

                                Text(track.artist)
                                    .font(AppFont.inter(size: 12, weight: .regular))
                                    .foregroundColor(.textSecondary)
                                    .lineLimit(1)
                            }

                            Spacer()

                            let isDownloaded = observable.downloadedTrackKeys.contains(track.fileKey)

                            if track.numberPlays > 0 {
                                Image(systemName: "headphones")
                                    .font(.system(size: 12))
                                    .foregroundColor(.textTertiary)
                            }

                            if isDownloaded {
                                Image(systemName: "square.and.arrow.down")
                                    .font(.system(size: 14))
                                    .foregroundColor(.accentColor)
                            }

                            let durationSec = track.durationMs / 1000
                            Text(String(format: "%d:%02d", durationSec / 60, durationSec % 60))
                                .styleMonoLabel()
                        }
                        .contentShape(Rectangle())
                        .onTapGesture {
                            if editMode != .active {
                                observable.playByIndex(index: index)
                            }
                        }
                        .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                        .listRowSeparator(.hidden)
                        .listRowBackground(
                            RoundedRectangle(cornerRadius: 8)
                                .fill(isActive ? Color.bg3 : Color.bg2)
                                .padding(.vertical, 4)
                                .padding(.horizontal, 16)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 8)
                                        .stroke(isActive ? Color.accentColor : Color.line, lineWidth: 1)
                                        .padding(.vertical, 4)
                                        .padding(.horizontal, 16),
                                ),
                        )
                    }
                    .onDelete(perform: deleteTracks)
                    .onMove(perform: moveTracks)
                }
                .listStyle(PlainListStyle())
                .scrollContentBackground(.hidden)
                .background(Color.bg1)
                .environment(\.editMode, $editMode)
            }
        }
        .background(Color.bg1.ignoresSafeArea())
        .task { await observable.observe() }
    }

    private func deleteTracks(at offsets: IndexSet) {
        for index in offsets {
            observable.removeQueueTrack(index: index)
        }
    }

    private func moveTracks(from source: IndexSet, to destination: Int) {
        guard let fromIndex = source.first else { return }
        // Adjust destination index because of the item removed from source
        let toIndex = destination > fromIndex ? destination - 1 : destination
        observable.moveQueueTrack(from: fromIndex, to: toIndex)
    }
}
