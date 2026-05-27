import SwiftUI
import SharedLogic
import KMPNativeCoroutinesAsync

@Observable
@MainActor
class QueueObservable {
    let viewModel: QueueViewModel

    var queueTracks: [Track] = []
    var activeIndex: Int = -1
    var isPlaying: Bool = false
    var isLoading: Bool = false
    var isLocal: Bool = true
    var transientError: String? = nil

    nonisolated(unsafe) private var observeTask: Task<Void, Never>?

    init(viewModel: QueueViewModel) {
        self.viewModel = viewModel

        sync(state: viewModel.state)

        observeTask = Task { @MainActor [weak self] in
            guard let stateFlow = self?.viewModel.stateFlow else { return }
            do {
                for try await state in asyncSequence(for: stateFlow) {
                    self?.sync(state: state)
                }
            } catch {
                // Flow cancelled
            }
        }
    }

    deinit {
        observeTask?.cancel()
    }
    
    private func sync(state: QueueViewState) {
        self.queueTracks = state.queueTracks
        self.activeIndex = Int(state.activeIndex)
        self.isPlaying = state.isPlaying
        self.isLoading = state.isLoading
        self.isLocal = state.isLocal
        self.transientError = state.transientError
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
    
    @State private var editMode: EditMode = .inactive
    
    init(viewModel: QueueViewModel, onBackClick: @escaping () -> Void) {
        self._observable = State(initialValue: QueueObservable(viewModel: viewModel))
        self.onBackClick = onBackClick
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
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
                                        .padding(.horizontal, 16)
                                )
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
