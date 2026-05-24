import SwiftUI
import SharedLogic

struct QueueView: View {
    @ObservedObject var audioHandler = PlaybackStateObserver.shared
    
    let onBackClick: () -> Void
    
    @State private var remoteQueue: [Track] = []
    @State private var isLoadingRemote = false
    @State private var editMode: EditMode = .inactive
    
    var isLocal: Bool {
        audioHandler.activeZone.isLocal || audioHandler.activeZone.isOffline || audioHandler.activeZone.isAndroidAuto
    }
    
    var currentQueue: [Track] {
        isLocal ? audioHandler.localQueue : remoteQueue
    }
    
    var activeIndex: Int {
        Int(audioHandler.playerStatus?.playingNowPosition ?? -1)
    }
    
    var isPlaying: Bool {
        audioHandler.playerStatus?.state == .playing
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
                    
                    Button(action: clearQueue) {
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
            if isLoadingRemote && !isLocal {
                Spacer()
                ProgressView()
                    .tint(.accentColor)
                Spacer()
            } else if currentQueue.isEmpty {
                Spacer()
                Text("QUEUE IS EMPTY")
                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                    .foregroundColor(.textTertiary)
                Spacer()
            } else {
                List {
                    ForEach(Array(currentQueue.enumerated()), id: \.element.fileKey) { index, track in
                        let isActive = index == activeIndex
                        
                        HStack(spacing: 12) {
                            // Index / VuMeter
                            Group {
                                if isActive {
                                    VuMeter(isPlaying: isPlaying)
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
                                JrrDependencies.shared.facade.playByIndex(index: Int32(index))
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
        .task(id: audioHandler.activeZone) {
            await loadRemoteQueueIfNeeded()
        }
        .task(id: audioHandler.playerStatus?.playingNowTracks) {
            await loadRemoteQueueIfNeeded()
        }
    }
    
    private func loadRemoteQueueIfNeeded() async {
        guard !isLocal else { return }
        isLoadingRemote = true
        do {
            let tracks = try await JrrDependencies.shared.libraryRepository.getRemoteQueue()
            await MainActor.run {
                self.remoteQueue = tracks
                self.isLoadingRemote = false
            }
        } catch {
            print("Failed to load remote queue: \(error)")
            await MainActor.run {
                self.isLoadingRemote = false
            }
        }
    }
    
    private func deleteTracks(at offsets: IndexSet) {
        for index in offsets {
            JrrDependencies.shared.facade.removeQueueTrack(index: Int32(index))
            if !isLocal {
                remoteQueue.remove(at: index)
            }
        }
    }
    
    private func moveTracks(from source: IndexSet, to destination: Int) {
        guard let fromIndex = source.first else { return }
        // Adjust destination index because of the item removed from source
        let toIndex = destination > fromIndex ? destination - 1 : destination
        
        JrrDependencies.shared.facade.moveQueueTrack(from: Int32(fromIndex), to: Int32(toIndex))
        
        if !isLocal {
            var tempQueue = remoteQueue
            let item = tempQueue.remove(at: fromIndex)
            tempQueue.insert(item, at: toIndex)
            remoteQueue = tempQueue
        }
    }
    
    private func clearQueue() {
        JrrDependencies.shared.facade.clearQueue()
        if !isLocal {
            remoteQueue = []
        }
    }
}

#Preview {
    QueueView(onBackClick: {})
}
