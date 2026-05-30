import SharedLogic
import SwiftUI

private let log = SwiftLog("ui:iOS:AlbumDetail")

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
    var errorMessage: String?

    @ObservationIgnored private var observeTask: Task<Void, Never>?

    init(viewModel: AlbumDetailViewModel) {
        log.d("init")
        self.viewModel = viewModel
        albumName = viewModel.album.name
        artistName = viewModel.album.albumArtist

        sync(state: viewModel.state.value)

        observeTask = Task { @MainActor [weak self] in
            guard let stateFlow = self?.viewModel.state else { return }
            for await state in stateFlow {
                self?.sync(state: state)
            }
        }
    }

    deinit {
        log.d("deinit")
        observeTask?.cancel()
        // No viewModel.dispose() here: the AlbumDetailViewModel is now owned by
        // the Decompose `LibraryComponent.Child.Detail` and retained in Essenty's
        // InstanceKeeper. It is torn down deterministically (viewModelScope
        // cancelled via onCleared) when the Detail child is popped — disposing
        // it from this transient Swift wrapper would kill a VM the component may
        // still hold alive in its back stack across tab switches.
    }

    private func sync(state: AlbumDetailViewState) {
        isOffline = state.isOfflineMode
        transientError = state.transientError

        switch onEnum(of: state.contentState) {
        case .loading:
            tracks = []
            downloadedTrackKeys = []
            activeDownloadJobs = [:]
            isFavorite = false
            isLoading = true
            errorMessage = nil
        case let .success(success):
            tracks = success.tracks
            downloadedTrackKeys = success.downloadedTrackKeys
            activeDownloadJobs = success.activeDownloadJobs
            isFavorite = success.isFavorite
            isLoading = false
            errorMessage = nil
        case let .error(error):
            tracks = []
            downloadedTrackKeys = []
            activeDownloadJobs = [:]
            isFavorite = false
            isLoading = false
            errorMessage = error.message
        }
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

    func addTrackToQueue(track: Track) {
        viewModel.addTrackToQueue(track: track)
    }

    func playTrackNext(track: Track) {
        viewModel.playTrackNext(track: track)
    }

    func addAlbumToQueue() {
        viewModel.addAlbumToQueue()
    }

    func playAlbumNext() {
        viewModel.playAlbumNext()
    }

    func downloadAlbum() {
        viewModel.downloadAlbum()
    }

    func clearTransientError() {
        viewModel.clearTransientError()
    }

    func retry() {
        viewModel.retry()
    }
}

/// Public wrapper. The `AlbumDetailViewModel` is supplied by the Decompose
/// `LibraryComponent.Child.Detail` (built lazily, keyed per album, retained in
/// the InstanceKeeper), so the old per-album `.id(...)` identity hack is no
/// longer needed — the component already gives a distinct VM per album.
struct AlbumDetailView: View {
    let viewModel: AlbumDetailViewModel
    let onBackClick: () -> Void

    /// Optional + `.task` is the iOS 17+ pattern for lazy `@State` init when
    /// the wrapped type isn't an `ObservableObject` (so `@StateObject` doesn't
    /// apply). SwiftUI invokes the closure exactly once per view identity.
    @State private var observable: AlbumDetailObservable?

    var body: some View {
        Group {
            if let observable {
                AlbumDetailContentView(
                    observable: observable,
                    onBackClick: onBackClick,
                )
            } else {
                Color.bg1.ignoresSafeArea()
            }
        }
        .task {
            if observable == nil {
                observable = AlbumDetailObservable(viewModel: viewModel)
            }
        }
    }
}

/// All the actual rendering. Held inside [AlbumDetailView] which guarantees a
/// non-nil observable by the time this view mounts.
private struct AlbumDetailContentView: View {
    @Environment(AppContainer.self) private var container
    let observable: AlbumDetailObservable
    let onBackClick: () -> Void

    @State private var infoTrack: Track? = nil
    @State private var infoAlbum: Album? = nil

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

                HStack(spacing: 0) {
                    Button(action: { observable.toggleFavorite() }) {
                        Image(systemName: observable.isFavorite ? "star.fill" : "star")
                            .font(.system(size: 18))
                            .foregroundColor(observable.isFavorite ? .accentColor : .textTertiary)
                            .frame(width: 44, height: 44)
                    }

                    Menu {
                        Button(action: { infoAlbum = observable.viewModel.album }) {
                            Label("Info", systemImage: "info.circle")
                        }
                        Button(action: { observable.playAlbum() }) {
                            Label("Play Album", systemImage: "play.fill")
                        }
                        Button(action: { observable.playAlbumNext() }) {
                            Label("Play Next", systemImage: "arrow.right.to.line")
                        }
                        Button(action: { observable.addAlbumToQueue() }) {
                            Label("Add to Queue", systemImage: "plus")
                        }
                        if !observable.isOffline {
                            Button(action: { observable.downloadAlbum() }) {
                                Label("Download Album", systemImage: "arrow.down.circle")
                            }
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .font(.system(size: 16, weight: .bold))
                            .foregroundColor(.textPrimary)
                            .frame(width: 44, height: 44)
                    }
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
                                   let url = URL(string: artworkUrl)
                                {
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
                                            style: StrokeStyle(lineWidth: 4),
                                        )
                                    }
                                }
                            }
                            .frame(width: 200, height: 200)
                            .cornerRadius(8)
                            .overlay(
                                RoundedRectangle(cornerRadius: 8)
                                    .stroke(Color.line2, lineWidth: 1),
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
                                            .stroke(Color.line2, lineWidth: 1),
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
                                    sortedDiscKeys: sortedDiscKeys,
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
        .sheet(item: $infoTrack) { track in
            InfoView(title: track.name, fields: track.toInfoFields())
        }
        .sheet(item: $infoAlbum) { album in
            InfoView(title: album.name, fields: album.toInfoFields())
        }
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

    private func trackRow(track: Track, displayIdx: Int32, sortedTracks _: [Track]) -> some View {
        HStack {
            Text(String(format: "%02d", displayIdx))
                .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                .foregroundColor(.accentColor)
                .frame(width: 36, alignment: .leading)

            VStack(alignment: .leading, spacing: 2) {
                Text(track.name)
                    .styleItemTitle()
                    .lineLimit(1)

                let durationSec = track.durationMs / 1000
                let durationStr = String(format: "%d:%02d", durationSec / 60, durationSec % 60)
                let subtitleText = track.artist != observable.artistName ? "\(track.artist) • \(durationStr)" : durationStr
                Text(subtitleText)
                    .styleItemSubtitle()
                    .lineLimit(1)
            }

            Spacer()

            if track.numberPlays > 0 {
                Image(systemName: "headphones")
                    .font(.system(size: 12))
                    .foregroundColor(.textTertiary)
                    .padding(.trailing, 4)
            }

            // Download status
            if !observable.isOffline {
                if observable.downloadedTrackKeys.contains(track.fileKey) {
                    Image(systemName: "square.and.arrow.down")
                        .font(.system(size: 14))
                        .foregroundColor(.accentColor)
                        .padding(.trailing, 8)
                } else if let jobState = observable.activeDownloadJobs[track.fileKey] {
                    if jobState == "downloading" || jobState == "DOWNLOADING" {
                        ProgressView()
                            .controlSize(.small)
                            .tint(.accentColor)
                            .padding(.trailing, 8)
                    }
                }
            }

            Menu {
                Button(action: { infoTrack = track }) {
                    Label("Info", systemImage: "info.circle")
                }
                Button(action: { observable.playTrack(track) }) {
                    Label("Play", systemImage: "play.fill")
                }
                Button(action: { observable.playTrackNext(track: track) }) {
                    Label("Play Next", systemImage: "arrow.right.to.line")
                }
                Button(action: { observable.addTrackToQueue(track: track) }) {
                    Label("Add to Queue", systemImage: "plus")
                }
                if !observable.isOffline {
                    Button(action: { observable.startDownload(track: track) }) {
                        Label("Download", systemImage: "arrow.down.circle")
                    }
                }
            } label: {
                Image(systemName: "ellipsis")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.textSecondary)
                    .frame(width: 32, height: 32)
            }
            .buttonStyle(PlainButtonStyle())
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
