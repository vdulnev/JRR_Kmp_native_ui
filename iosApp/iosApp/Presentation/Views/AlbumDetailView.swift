import SwiftUI
import SharedLogic
import KMPNativeCoroutinesAsync

@Observable
@MainActor
class AlbumDetailObservable {
    let viewModel: AlbumDetailViewModel

    var albumName: String
    var artistName: String
    var transientError: String?

    // UI reactive values — mirrored from AlbumDetailViewState (flat shape, no sealed casts)
    var tracks: [Track] = []
    var downloadedTrackKeys: Set<String> = []
    var activeDownloadJobs: [String: String] = [:]
    var isFavorite: Bool = false
    var isLoading: Bool = true
    var isOffline: Bool = true
    var errorMessage: String? = nil

    nonisolated(unsafe) private var observeTask: Task<Void, Never>?

    init(viewModel: AlbumDetailViewModel) {
        self.viewModel = viewModel
        self.albumName = viewModel.album.name
        self.artistName = viewModel.album.albumArtist

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

    private func sync(state: AlbumDetailViewState) {
        self.isOffline = state.isOfflineMode
        self.transientError = state.transientError
        self.isLoading = state.isLoading
        self.errorMessage = state.errorMessage
        self.tracks = state.tracks
        self.downloadedTrackKeys = state.downloadedTrackKeys
        self.activeDownloadJobs = state.activeDownloadJobs
        self.isFavorite = state.isFavorite
    }
    
    func playTrack(_ track: Track) {
        viewModel.playTrack(track: track)
    }
    
    func playAlbum() {
        viewModel.playAlbum()
    }
    
    func shuffleAlbum() {
        viewModel.shuffleAlbum()
    }
    
    func toggleFavorite() {
        viewModel.toggleFavorite()
    }
    
    func startDownload(track: Track) {
        viewModel.startDownload(track: track)
    }
    
    func clearTransientError() {
        viewModel.clearTransientError()
    }
    
    func retry() {
        viewModel.retry()
    }
}

struct AlbumDetailView: View {
    @Environment(AppContainer.self) private var container
    @State private var observable: AlbumDetailObservable
    let onBackClick: () -> Void
    
    init(viewModel: AlbumDetailViewModel, onBackClick: @escaping () -> Void) {
        self._observable = State(initialValue: AlbumDetailObservable(viewModel: viewModel))
        self.onBackClick = onBackClick
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Top Bar
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
                
                Text("ALBUM")
                    .styleSectionLabel()
                
                Spacer()
                
                Button(action: { observable.toggleFavorite() }) {
                    Image(systemName: observable.isFavorite ? "star.fill" : "star")
                        .font(.system(size: 18))
                        .foregroundColor(observable.isFavorite ? .accentColor : .textTertiary)
                        .frame(width: 44, height: 44)
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .background(Color.bg1)
            
            if observable.isLoading {
                Spacer()
                ProgressView()
                    .tint(.accentColor)
                Spacer()
            } else if let error = observable.errorMessage {
                Spacer()
                VStack(spacing: 16) {
                    Text("Error: \(error)")
                        .foregroundColor(.red)
                        .font(AppFont.inter(size: 14, weight: .semibold))
                    Button("Retry") {
                        observable.retry()
                    }
                }
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 20) {
                        // Artwork and details header
                        VStack(alignment: .center, spacing: 16) {
                            ZStack {
                                let artworkUrl = observable.tracks.first.map { container.mcwsClient.buildImageUrl(fileKey: $0.fileKey) } ?? ""
                                if !artworkUrl.isEmpty,
                                   let url = URL(string: artworkUrl) {
                                    JrrAsyncImage(url: url) { image in
                                        image
                                            .resizable()
                                            .aspectRatio(contentMode: .fill)
                                    } placeholder: {
                                        Color.bg2
                                    }
                                } else {
                                    Canvas { context, size in
                                        context.fill(Path(CGRect(origin: .zero, size: size)), with: .color(Color(hex: 0x1E293B)))
                                        var path = Path()
                                        path.move(to: .zero)
                                        path.addLine(to: CGPoint(x: size.width, y: size.height))
                                        context.stroke(
                                            path,
                                            with: .color(Color.accentColor.opacity(0.5)),
                                            style: StrokeStyle(lineWidth: 4)
                                        )
                                    }
                                }
                            }
                            .frame(width: 200, height: 200)
                            .cornerRadius(8)
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.line2, lineWidth: 1)
                            )
                            
                            VStack(spacing: 4) {
                                Text(observable.albumName)
                                    .font(AppFont.inter(size: 20, weight: .bold))
                                    .foregroundColor(.textPrimary)
                                    .lineLimit(2)
                                    .multilineTextAlignment(.center)
                                
                                Text(observable.artistName)
                                    .font(AppFont.inter(size: 13, weight: .regular))
                                    .foregroundColor(.textSecondary)
                                    .lineLimit(1)
                                    .multilineTextAlignment(.center)
                            }
                            .padding(.horizontal, 8)
                            
                            // Play / Shuffle Buttons
                            HStack(spacing: 12) {
                                Button(action: {
                                    observable.playAlbum()
                                }) {
                                    HStack(spacing: 8) {
                                        Image(systemName: "play.fill")
                                            .font(.system(size: 12))
                                            .foregroundColor(.bg0)
                                        Text("PLAY")
                                            .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                            .foregroundColor(.bg0)
                                    }
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 38)
                                    .background(Color.accentColor)
                                    .cornerRadius(6)
                                }
                                
                                Button(action: {
                                    observable.shuffleAlbum()
                                }) {
                                    HStack(spacing: 8) {
                                        Text("🔀")
                                            .font(.system(size: 12))
                                            .foregroundColor(.textPrimary)
                                        Text("SHUFFLE")
                                            .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                            .foregroundColor(.textPrimary)
                                    }
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 38)
                                    .background(Color.bg2)
                                    .cornerRadius(6)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 6)
                                            .stroke(Color.line2, lineWidth: 1)
                                    )
                                }
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        
                        // Tracks listing
                        VStack(alignment: .leading, spacing: 0) {
                            ForEach(sortedDiscKeys, id: \.self) { discNum in
                                discSection(
                                    discNum: discNum,
                                    discTracks: discGroups[discNum] ?? [],
                                    sortedTracks: sortedTracks,
                                    sortedDiscKeys: sortedDiscKeys
                                )
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                    }
                    .padding(.bottom, 30)
                }
            }
        }
        .background(Color.bg1.ignoresSafeArea())
    }
    
    private var sortedTracks: [Track] {
        observable.tracks.sorted { ($0.discNumber, $0.trackNumber) < ($1.discNumber, $1.trackNumber) }
    }
    
    private var discGroups: [Int32: [Track]] {
        Dictionary(grouping: sortedTracks, by: { $0.discNumber })
    }
    
    private var sortedDiscKeys: [Int32] {
        discGroups.keys.sorted()
    }
    
    private func trackRow(track: Track, displayIdx: Int32, sortedTracks: [Track]) -> some View {
        HStack {
            Text(String(format: "%02d", displayIdx))
                .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                .foregroundColor(.accentColor)
                .frame(width: 36, alignment: .leading)
            
            VStack(alignment: .leading, spacing: 2) {
                Text(track.name)
                    .styleItemTitle()
                    .lineLimit(1)
                
                if track.artist != observable.artistName {
                    Text(track.artist)
                        .styleItemSubtitle()
                        .lineLimit(1)
                }
            }
            
            Spacer()
            
            // Download status / action button
            if !observable.isOffline {
                if observable.downloadedTrackKeys.contains(track.fileKey) {
                    Image(systemName: "checkmark")
                        .font(.system(size: 11, weight: .bold))
                        .foregroundColor(.accentColor)
                        .padding(.trailing, 8)
                } else if let jobState = observable.activeDownloadJobs[track.fileKey] {
                    if jobState == "downloading" || jobState == "DOWNLOADING" {
                        ProgressView()
                            .controlSize(.small)
                            .tint(.accentColor)
                            .padding(.trailing, 8)
                    } else {
                        Image(systemName: "ellipsis.circle")
                            .font(.system(size: 12))
                            .foregroundColor(.textTertiary)
                            .padding(.trailing, 8)
                    }
                } else {
                    Button(action: {
                        observable.startDownload(track: track)
                    }) {
                        Image(systemName: "arrow.down.circle")
                            .font(.system(size: 14))
                            .foregroundColor(.textSecondary)
                    }
                    .buttonStyle(PlainButtonStyle())
                    .padding(.trailing, 8)
                }
            }
            
            let durationSec = track.durationMs / 1000
            Text(String(format: "%d:%02d", durationSec / 60, durationSec % 60))
                .styleMonoLabel()
        }
        .contentShape(Rectangle())
        .padding(.vertical, 12)
        .onTapGesture {
            observable.playTrack(track)
        }
    }
    
    private func discSection(discNum: Int32, discTracks: [Track], sortedTracks: [Track], sortedDiscKeys: [Int32]) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            // Header
            HStack {
                if sortedDiscKeys.count > 1 {
                    Text("DISC \(discNum)".uppercased())
                        .styleSectionLabel()
                } else {
                    Text("SIDE A".uppercased())
                        .styleSectionLabel()
                }
                Spacer()
            }
            .padding(.top, 16)
            .padding(.bottom, 8)
            
            ForEach(discTracks, id: \.fileKey) { track in
                let trackIdx = sortedTracks.firstIndex(of: track) ?? 0
                let displayIdx = track.trackNumber != 0 ? track.trackNumber : Int32(trackIdx + 1)
                
                trackRow(track: track, displayIdx: displayIdx, sortedTracks: sortedTracks)
                
                Divider()
                    .background(Color.line)
            }
        }
    }
}
