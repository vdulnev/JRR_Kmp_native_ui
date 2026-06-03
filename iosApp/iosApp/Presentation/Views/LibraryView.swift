import SharedLogic
import SwiftUI

private let log = SwiftLog("ui:iOS:Library")

@Observable
@MainActor
class LibraryObservable {
    let viewModel: LibraryViewModel

    var currentTab: String = "artists"
    var artists: [String] = []
    var selectedArtist: String?
    var artistAlbums: [Album] = []
    var compilationMode: Bool = false
    var compilationArtists: [String] = []
    var artistsFilter: String = ""
    var randomAlbums: [Album] = []
    var browseStack: [BrowseNode] = []
    var browseChildren: [BrowseItem] = []
    var browseTracks: [Track] = []
    var downloadedTracks: [Track] = []
    var isOffline: Bool = false
    var isLoading: Bool = false
    var isTabLoading: Bool = false
    var transientError: String?

    init(viewModel: LibraryViewModel) {
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

    private func sync(state: LibraryViewState) {
        currentTab = state.currentTab
        artists = state.artists
        selectedArtist = state.selectedArtist
        artistAlbums = state.artistAlbums
        compilationMode = state.compilationMode
        compilationArtists = state.compilationArtists
        artistsFilter = state.artistsFilter
        randomAlbums = state.randomAlbums
        browseStack = state.browseStack
        browseChildren = state.browseChildren
        browseTracks = state.browseTracks
        downloadedTracks = state.downloadedTracks
        isOffline = state.isOffline
        isLoading = state.isLoading
        isTabLoading = state.isTabLoading
        transientError = state.transientError
    }

    func switchTab(_ tab: String) {
        viewModel.switchTab(tab: tab)
    }

    func selectArtist(_ artistName: String?) {
        viewModel.selectArtist(artistName: artistName)
    }

    /// Pick an entry from the compilations contributing-artists list.
    /// `nil` selects "All" (every compilation).
    func selectCompilationArtist(_ artistName: String?) {
        viewModel.selectCompilationArtist(artistName: artistName)
    }

    func setArtistsFilter(_ query: String) {
        viewModel.setArtistsFilter(query: query)
    }

    func pushBrowseNode(browseItem: BrowseItem) {
        viewModel.pushBrowseNode(label: browseItem.name, nodeId: browseItem.key)
    }

    func popBrowseNode() {
        viewModel.popBrowseNode()
    }

    func playTrack(_ track: Track) {
        viewModel.playTrack(track: track)
    }

    func playTracks(_ tracks: [Track], startIndex: Int) {
        viewModel.playTracks(tracks: tracks, startIndex: Int32(startIndex))
    }

    func addTrackToQueue(_ track: Track) {
        viewModel.addTrackToQueue(track: track)
    }

    func playTrackNext(_ track: Track) {
        viewModel.playTrackNext(track: track)
    }

    func downloadTrack(_ track: Track) {
        viewModel.downloadTrack(track: track)
    }

    func playAlbum(_ album: Album) {
        viewModel.playAlbum(album: album)
    }

    func addAlbumToQueue(_ album: Album) {
        viewModel.addAlbumToQueue(album: album)
    }

    func playAlbumNext(_ album: Album) {
        viewModel.playAlbumNext(album: album)
    }

    func downloadAlbum(_ album: Album) {
        viewModel.downloadAlbum(album: album)
    }

    func playBrowseItem(_ item: BrowseItem) {
        viewModel.playBrowseItem(item: item)
    }

    func addBrowseItemToQueue(_ item: BrowseItem) {
        viewModel.addBrowseItemToQueue(item: item)
    }

    func playBrowseItemNext(_ item: BrowseItem) {
        viewModel.playBrowseItemNext(item: item)
    }

    func downloadBrowseItem(_ item: BrowseItem) {
        viewModel.downloadBrowseItem(item: item)
    }

    func retry() {
        viewModel.retry()
    }

    func clearTransientError() {
        viewModel.clearTransientError()
    }

    func playTracksShuffled(_ tracks: [Track]) {
        viewModel.playTracksShuffled(tracks: tracks)
    }

    func playTracksNext(_ tracks: [Track]) {
        viewModel.playTracksNext(tracks: tracks)
    }

    func addTracksToQueue(_ tracks: [Track]) {
        viewModel.addTracksToQueue(tracks: tracks)
    }
}

/// Collapses the chrome (header / filter / mini-player) when a list is scrolled
/// down, restores it when scrolled up or back at the top.
///
/// Driven by the per-sample scroll *delta* — the same model as the Android
/// `NestedScrollConnection` (`onPreScroll` dy). This is deliberately not based
/// on the absolute offset: collapsing the chrome changes the mini-player inset
/// and container/content size, which moves `contentOffset` on its own. Treating
/// that self-induced shift as a scroll is what made the mini-player pop in and
/// out — worst with a non-empty filter, where the shorter list amplifies the
/// layout jump. So any sample where `maxOffset` changed (a relayout, not a
/// gesture) or where we're in rubber-band overscroll is ignored. With no
/// absolute zones it behaves identically for any list length.
private struct HidesChromeOnScroll: ViewModifier {
    private struct Metrics: Equatable {
        let offset: CGFloat
        let maxOffset: CGFloat
    }

    @Environment(ChromeVisibility.self) private var chrome
    @State private var lastOffset: CGFloat = 0
    /// Signed sum of recent movement since the last toggle. Lets a *slow* drag
    /// (tiny per-frame deltas) accumulate up to the threshold, while jitter
    /// (alternating signs) cancels out near zero.
    @State private var accum: CGFloat = 0

    /// Total accumulated movement in one direction that flips the chrome.
    private let threshold: CGFloat = 20

    func body(content: Content) -> some View {
        // `onScrollGeometryChange` requires macOS 15; we target 14. On iOS it
        // drives the hide-on-scroll chrome; on macOS it's a no-op (chrome stays).
        #if os(iOS)
            content.onScrollGeometryChange(for: Metrics.self) { geo in
                Metrics(
                    offset: geo.contentOffset.y,
                    maxOffset: max(0, geo.contentSize.height - geo.containerSize.height),
                )
            } action: { old, new in
                let offset = new.offset
                // A relayout (chrome toggled → inset / container size changed) moves
                // the offset without any gesture. Resync and ignore.
                if new.maxOffset != old.maxOffset {
                    lastOffset = offset
                    return
                }
                // Pull-down past the top is a deliberate "give me the chrome back"
                // gesture — and the only escape hatch on a list that fits once
                // collapsed. (Purely gesture-driven otherwise, like Android: no
                // absolute top-reveal, so a short list that fits after collapsing
                // just stays collapsed instead of popping the chrome back.)
                if offset < 0 {
                    chrome.setCollapsed(false)
                    lastOffset = offset
                    accum = 0
                    return
                }
                // Rubber-band overscroll past the bottom is not a real position;
                // don't let the bounce read as a direction change.
                if offset > new.maxOffset {
                    lastOffset = offset
                    return
                }
                let dy = offset - lastOffset
                lastOffset = offset
                // Reset the accumulator when direction reverses so a flick the other
                // way responds immediately instead of unwinding a stale sum.
                if dy > 0, accum < 0 { accum = 0 }
                if dy < 0, accum > 0 { accum = 0 }
                accum += dy
                if accum > threshold {
                    chrome.setCollapsed(true) // scrolled down enough
                    accum = 0
                } else if accum < -threshold {
                    chrome.setCollapsed(false) // scrolled up enough
                    accum = 0
                }
            }
        #else
            content
        #endif
    }
}

extension View {
    func hidesChromeOnScroll() -> some View {
        modifier(HidesChromeOnScroll())
    }
}

struct LibraryView: View {
    @Environment(AppContainer.self) private var container
    @Environment(ChromeVisibility.self) private var chrome
    @EnvironmentObject private var stateObserver: PlaybackStateObserver
    @State private var observable: LibraryObservable
    let onAlbumClick: (Album) -> Void // AlbumName, ArtistName
    /// Large-screen (tablet) layout: Artists tab renders as a master/detail
    /// split instead of the phone's one-pane-at-a-time drill-down.
    var isLarge: Bool = false
    @State private var selectedArtist: String? = nil
    @State private var selectedAlbumGroupId: String? = nil
    @State private var infoTrack: Track? = nil
    @State private var infoAlbum: Album? = nil
    /// Persisted top-visible row id of the compilations contributing-artists
    /// list, so its scroll position survives drilling into a compilation and
    /// back (a plain ScrollView resets to top when occluded and reshown).
    @State private var compilationScrollID: String? = nil
    /// Stable id for the leading "All" row; namespaced so it can't collide with
    /// an artist name.
    private let compilationAllRowID = "\u{1}__all_compilations__"
    /// Local mirror of the shared artists filter, so the text field stays
    /// responsive without round-tripping every keystroke through the flow.
    @State private var artistFilterText = ""
    /// Split-pane (tablet) only: a separate client-side filter for the selected
    /// artist's albums in the detail pane, independent of the master artist
    /// filter (both panes are visible at once).
    @State private var albumFilterText = ""

    init(viewModel: LibraryViewModel, onAlbumClick: @escaping (Album) -> Void, isLarge: Bool = false) {
        _observable = State(initialValue: LibraryObservable(viewModel: viewModel))
        self.onAlbumClick = onAlbumClick
        self.isLarge = isLarge
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header — collapses while scrolling to maximise the list area.
            if !chrome.collapsed {
                HStack {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("LIBRARY")
                            .styleSectionLabel()

                        Text("Browse")
                            .styleScreenTitle()
                    }

                    Spacer()
                }
                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                .padding(.vertical, 12)
                .background(Color.bg1)
                .transition(.move(edge: .top).combined(with: .opacity))
            }

            // Tab strip
            HStack(spacing: 0) {
                tabButton(title: "Artists", id: "artists")
                if !observable.isOffline {
                    tabButton(title: "Random", id: "random")
                    tabButton(title: "Browse", id: "browse")
                }
                tabButton(title: "Downloads", id: "downloads")
                tabButton(title: "Favorites", id: "favorites")
            }
            .background(Color.bg1)

            // Tab Content
            Group {
                switch observable.currentTab {
                case "artists":
                    if isLarge {
                        artistsTabLarge()
                    } else {
                        artistsTab()
                    }
                case "random":
                    randomTab()
                case "browse":
                    browseTab()
                case "downloads":
                    downloadsTab()
                case "favorites":
                    favoritesTab()
                default:
                    EmptyView()
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
        .background(Color.bg1.ignoresSafeArea())
        // Reserve room at the bottom for the floating mini-player (shown on the
        // Library tab whenever a track is loaded) so the last list row isn't
        // hidden behind it. Insets every descendant scroll view at once.
        .safeAreaInset(edge: .bottom, spacing: 0) {
            // No floating mini-player on large screens (it's docked in the
            // sidebar), so don't reserve space for it there.
            if !isLarge, !chrome.collapsed, !(stateObserver.playerStatus?.trackName ?? "").isEmpty {
                Color.clear.frame(height: 76)
            }
        }
        .onAppear {
            if observable.artists.isEmpty {
                observable.retry()
            }
        }
        .onChange(of: observable.currentTab) { _, newValue in
            if newValue != "downloads" {
                selectedArtist = nil
                selectedAlbumGroupId = nil
            }
        }
        .sheet(item: $infoTrack) { track in
            InfoView(title: track.name, fields: track.toInfoFields())
        }
        .sheet(item: $infoAlbum) { album in
            InfoView(title: album.name, fields: album.toInfoFields())
        }
        .task { await observable.observe() }
    }

    /// Tab Button Helper
    private func tabButton(title: String, id: String) -> some View {
        Button(action: {
            observable.switchTab(id)
        }) {
            VStack(spacing: 4) {
                Text(title.uppercased())
                    .font(AppFont.ibmPlexMono(size: 10.5, weight: .medium))
                    .tracking(1.6)
                    .foregroundColor(observable.currentTab == id ? .accentColor : .textTertiary)
                    .lineLimit(1)
                    .truncationMode(.tail)

                // Active Underline
                Rectangle()
                    .fill(observable.currentTab == id ? Color.accentColor : Color.clear)
                    .frame(height: 2)
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 12)
        }
    }

    // MARK: - Artists Tab View

    private func artistsTab() -> some View {
        ZStack {
            // Artists List
            VStack(spacing: 0) {
                listFilterField(placeholder: "Filter artists")
                if observable.isLoading {
                    VStack {
                        Spacer()
                        ProgressView().tint(.accentColor)
                        Spacer()
                    }
                } else {
                    let displayArtists = observable.artists.filter { matchesFilter($0) }
                    ScrollViewReader { proxy in
                        ScrollView {
                            LazyVStack(spacing: 8) {
                                ForEach(displayArtists, id: \.self) { artist in
                                    HStack {
                                        // Avatar circle with initial
                                        ZStack {
                                            Circle()
                                                .fill(Color.bg3)
                                                .frame(width: 36, height: 36)

                                            Text(artist.prefix(1).uppercased())
                                                .font(AppFont.ibmPlexMono(size: 14, weight: .medium))
                                                .foregroundColor(.accentColor)
                                        }

                                        highlighted(artist)
                                            .font(AppFont.inter(size: 16, weight: .medium))
                                            .foregroundColor(.textPrimary)
                                            .padding(.leading, 8)

                                        Spacer()
                                    }
                                    .padding(.vertical, 8)
                                    .padding(.horizontal, 12)
                                    .background(Color.bg2)
                                    .cornerRadius(10)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 10)
                                            .stroke(Color.line, lineWidth: 1),
                                    )
                                    .onTapGesture {
                                        observable.selectArtist(artist)
                                    }
                                }
                            }
                            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                            .padding(.trailing, 18)
                            .padding(.top, 12)
                        }
                        .hidesChromeOnScroll()
                        .overlay(alignment: .trailing) {
                            AlphabetIndexBar(
                                letters: orderedSectionLetters(displayArtists),
                                bottomInset: 96,
                            ) { letter in
                                if let target = displayArtists.first(
                                    where: { sectionLetter(for: $0) == letter },
                                ) {
                                    withAnimation { proxy.scrollTo(target, anchor: .top) }
                                }
                            }
                        }
                    }
                }
            }
            .opacity(observable.selectedArtist == nil && !observable.compilationMode ? 1 : 0)
            .disabled(observable.selectedArtist != nil || observable.compilationMode)

            // Compilations drill-down: "All" + the artists found inside
            // compilation albums. Kept mounted (opacity-gated) so its scroll
            // position survives drilling into a compilation and back.
            let showCompilations = observable.compilationMode && observable.selectedArtist == nil
            compilationArtistsList()
                .background(Color.bg1)
                .opacity(showCompilations ? 1 : 0)
                .disabled(!showCompilations)

            // Selected Artist's Albums List
            if let artist = observable.selectedArtist {
                VStack(spacing: 0) {
                    // Back to artists list header
                    Button(action: { observable.selectArtist(nil) }) {
                        HStack {
                            Image(systemName: "chevron.left")
                                .foregroundColor(.accentColor)
                                .font(.system(size: 16, weight: .bold))

                            Text(artist)
                                .styleSubScreenTitle()

                            Spacer()
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.vertical, 12)
                    }

                    listFilterField(placeholder: "Filter albums")

                    if observable.isTabLoading {
                        Spacer()
                        ProgressView().tint(.accentColor)
                        Spacer()
                    } else {
                        let displayAlbums = observable.artistAlbums.filter { matchesFilter($0.name) }
                        ScrollViewReader { proxy in
                            ScrollView {
                                LazyVStack(spacing: 8) {
                                    ForEach(displayAlbums, id: \.albumGroupId) { album in
                                        albumRowItem(album: album) {
                                            onAlbumClick(album)
                                        }
                                    }
                                }
                                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                                .padding(.trailing, 18)
                                .padding(.top, 8)
                            }
                            .hidesChromeOnScroll()
                            .overlay(alignment: .trailing) {
                                AlphabetIndexBar(
                                    letters: orderedSectionLetters(displayAlbums.map(\.name)),
                                    bottomInset: 96,
                                ) { letter in
                                    if let target = displayAlbums.first(
                                        where: { sectionLetter(for: $0.name) == letter },
                                    ) {
                                        withAnimation { proxy.scrollTo(target.albumGroupId, anchor: .top) }
                                    }
                                }
                            }
                        }
                    }
                }
                .background(Color.bg1)
            }
        }
        .onChange(of: artistFilterText) { _, value in
            observable.setArtistsFilter(value)
        }
        .onChange(of: observable.artistsFilter) { _, value in
            // The shared VM clears the filter on navigation; mirror that locally.
            if value.isEmpty, !artistFilterText.isEmpty {
                artistFilterText = ""
            }
        }
    }

    // MARK: - Artists Tab (large / split-pane)

    /// Tablet layout: artist master list (left) + selected artist's albums
    /// (right), both visible at once. Reuses the same observable + row builders
    /// as the phone path; selecting an artist only updates VM state, so opening
    /// an album still pushes the detail route (sidebar persists outside).
    private func artistsTabLarge() -> some View {
        HStack(spacing: 0) {
            // Master pane
            Group {
                if observable.compilationMode {
                    compilationArtistsList()
                } else {
                    masterArtistList()
                }
            }
            .frame(width: 340)
            .background(Color.bg1)

            Rectangle().fill(Color.line).frame(width: 1)

            // Detail pane
            Group {
                if let artist = observable.selectedArtist {
                    artistAlbumsDetailLarge(artist: artist)
                } else {
                    VStack(spacing: 8) {
                        Text("Select an artist")
                            .styleSubScreenTitle()
                        Text("Pick a name on the left to browse their albums.")
                            .font(AppFont.inter(size: 13, weight: .regular))
                            .foregroundColor(.textTertiary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
            .frame(maxWidth: .infinity)
            .background(Color.bg1)
        }
        .onChange(of: artistFilterText) { _, value in
            observable.setArtistsFilter(value)
        }
        .onChange(of: observable.artistsFilter) { _, value in
            if value.isEmpty, !artistFilterText.isEmpty {
                artistFilterText = ""
            }
        }
        .onChange(of: observable.selectedArtist) { _, _ in
            albumFilterText = ""
        }
    }

    /// Master artist list for the split-pane (no drill-down; taps just select).
    private func masterArtistList() -> some View {
        VStack(spacing: 0) {
            listFilterField(placeholder: "Filter artists")
            if observable.isLoading {
                Spacer()
                ProgressView().tint(.accentColor)
                Spacer()
            } else {
                let displayArtists = observable.artists.filter { matchesFilter($0) }
                ScrollViewReader { proxy in
                    ScrollView {
                        LazyVStack(spacing: 6) {
                            ForEach(displayArtists, id: \.self) { artist in
                                let selected = observable.selectedArtist == artist
                                HStack {
                                    ZStack {
                                        Circle()
                                            .fill(selected ? Color.accentColor : Color.bg3)
                                            .frame(width: 36, height: 36)
                                        Text(artist.replacingOccurrences(of: "The ", with: "").prefix(1).uppercased())
                                            .font(AppFont.ibmPlexMono(size: 14, weight: .medium))
                                            .foregroundColor(selected ? .bg0 : .accentColor)
                                    }
                                    highlighted(artist)
                                        .font(AppFont.inter(size: 16, weight: .medium))
                                        .foregroundColor(selected ? .accentColor : .textPrimary)
                                        .padding(.leading, 8)
                                    Spacer()
                                }
                                .padding(.vertical, 8)
                                .padding(.horizontal, 12)
                                .background(selected ? Color.accentColor.opacity(0.13) : Color.clear)
                                .cornerRadius(10)
                                .contentShape(Rectangle())
                                .onTapGesture { observable.selectArtist(artist) }
                                .id(artist)
                            }
                        }
                        .padding(.horizontal, 12)
                        .padding(.trailing, 18)
                        .padding(.top, 12)
                    }
                    .overlay(alignment: .trailing) {
                        AlphabetIndexBar(
                            letters: orderedSectionLetters(displayArtists),
                            bottomInset: 24,
                        ) { letter in
                            if let target = displayArtists.first(where: { sectionLetter(for: $0) == letter }) {
                                withAnimation { proxy.scrollTo(target, anchor: .top) }
                            }
                        }
                    }
                }
            }
        }
    }

    /// Detail pane: a selected artist's albums + PLAY ALL.
    private func artistAlbumsDetailLarge(artist: String) -> some View {
        VStack(spacing: 0) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("\(observable.artistAlbums.count) \(observable.artistAlbums.count == 1 ? "ALBUM" : "ALBUMS")")
                        .styleSectionLabel()
                    Text(artist)
                        .styleSubScreenTitle()
                }
                Spacer()
                if !observable.artistAlbums.isEmpty {
                    Button(action: {
                        let albums = observable.artistAlbums
                        observable.playAlbum(albums[0])
                        for album in albums.dropFirst() {
                            observable.addAlbumToQueue(album)
                        }
                    }) {
                        HStack(spacing: 8) {
                            Image(systemName: "play.fill").font(.system(size: 12))
                            Text("PLAY ALL").font(AppFont.ibmPlexMono(size: 11, weight: .bold)).tracking(1.4)
                        }
                        .foregroundColor(.bg0)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 9)
                        .background(Color.accentColor)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                    }
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.top, 22)
            .padding(.bottom, 8)

            // Filter for this artist's albums (independent of the master list).
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.textTertiary)
                TextField("Filter albums", text: $albumFilterText)
                    .noAutocapitalization()
                    .disableAutocorrection(true)
                    .foregroundColor(.textPrimary)
                    .font(AppFont.inter(size: 14, weight: .regular))
                if !albumFilterText.isEmpty {
                    Button(action: { albumFilterText = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.textSecondary)
                    }
                }
            }
            .padding(.horizontal, 10)
            .frame(height: 38)
            .background(Color.bg2)
            .cornerRadius(8)
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.line, lineWidth: 1))
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.bottom, 6)

            if observable.isTabLoading {
                Spacer()
                ProgressView().tint(.accentColor)
                Spacer()
            } else {
                let displayAlbums = observable.artistAlbums.filter {
                    albumFilterText.isEmpty || $0.name.range(of: albumFilterText, options: .caseInsensitive) != nil
                }
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(displayAlbums, id: \.albumGroupId) { album in
                            albumRowItem(album: album) { onAlbumClick(album) }
                        }
                    }
                    .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                    .padding(.top, 8)
                    .padding(.bottom, 32)
                }
            }
        }
    }

    // MARK: - Compilations contributing-artists list

    private func compilationArtistsList() -> some View {
        VStack(spacing: 0) {
            // Back to the artists list
            Button(action: { observable.selectArtist(nil) }) {
                HStack {
                    Image(systemName: "chevron.left")
                        .foregroundColor(.accentColor)
                        .font(.system(size: 16, weight: .bold))

                    Text("Compilations")
                        .styleSubScreenTitle()

                    Spacer()
                }
                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                .padding(.vertical, 12)
            }

            listFilterField(placeholder: "Filter artists")

            if observable.isTabLoading {
                Spacer()
                ProgressView().tint(.accentColor)
                Spacer()
            } else {
                let displayCompArtists = observable.compilationArtists.filter { matchesFilter($0) }
                ScrollView {
                    LazyVStack(spacing: 8) {
                        // Hide "All" while filtering — the user is hunting a
                        // specific artist, not the everything bucket.
                        if artistFilterText.isEmpty {
                            compilationRow(label: Text("All"), avatar: "∗", highlighted: true) {
                                observable.selectCompilationArtist(nil)
                            }
                            .id(compilationAllRowID)
                        }
                        ForEach(displayCompArtists, id: \.self) { artist in
                            compilationRow(
                                label: highlighted(artist),
                                avatar: String(artist.prefix(1)).uppercased(),
                                highlighted: false,
                            ) {
                                observable.selectCompilationArtist(artist)
                            }
                            .id(artist)
                        }
                    }
                    .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                    .padding(.trailing, 18)
                    .padding(.top, 8)
                    .scrollTargetLayout()
                }
                // Bind the top-visible row id to @State so the position is
                // restored when the list is reshown after a drill-down.
                .scrollPosition(id: $compilationScrollID, anchor: .top)
                .hidesChromeOnScroll()
                .overlay(alignment: .trailing) {
                    AlphabetIndexBar(
                        letters: orderedSectionLetters(displayCompArtists),
                        bottomInset: 96,
                    ) { letter in
                        if let target = displayCompArtists.first(
                            where: { sectionLetter(for: $0) == letter },
                        ) {
                            withAnimation { compilationScrollID = target }
                        }
                    }
                }
            }
        }
    }

    private func compilationRow(
        label: Text,
        avatar: String,
        highlighted: Bool,
        onTap: @escaping () -> Void,
    ) -> some View {
        HStack {
            ZStack {
                Circle()
                    .fill(highlighted ? Color.accentColor : Color.bg3)
                    .frame(width: 36, height: 36)

                Text(avatar)
                    .font(AppFont.ibmPlexMono(size: 14, weight: .medium))
                    .foregroundColor(highlighted ? .bg0 : .accentColor)
            }

            label
                .font(AppFont.inter(size: 16, weight: .medium))
                .foregroundColor(highlighted ? .accentColor : .textPrimary)
                .padding(.leading, 8)

            Spacer()
        }
        .padding(.vertical, 8)
        .padding(.horizontal, 12)
        .background(Color.bg2)
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.line, lineWidth: 1),
        )
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
    }

    // MARK: - In-list quick filter

    /// Slim type-to-filter row pinned above an Artists-tab list. Collapses with
    /// the rest of the chrome while scrolling.
    @ViewBuilder
    private func listFilterField(placeholder: String) -> some View {
        if !chrome.collapsed {
            HStack(spacing: 8) {
                Image(systemName: "magnifyingglass")
                    .foregroundColor(.textTertiary)
                TextField(placeholder, text: $artistFilterText)
                    .noAutocapitalization()
                    .disableAutocorrection(true)
                    .foregroundColor(.textPrimary)
                    .font(AppFont.inter(size: 14, weight: .regular))
                if !artistFilterText.isEmpty {
                    Button(action: { artistFilterText = "" }) {
                        Image(systemName: "xmark.circle.fill")
                            .foregroundColor(.textSecondary)
                    }
                }
            }
            .padding(.horizontal, 10)
            .frame(height: 38)
            .background(Color.bg2)
            .cornerRadius(8)
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(Color.line, lineWidth: 1),
            )
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.vertical, 6)
            .transition(.move(edge: .top).combined(with: .opacity))
        }
    }

    private func matchesFilter(_ text: String) -> Bool {
        artistFilterText.isEmpty || text.range(of: artistFilterText, options: .caseInsensitive) != nil
    }

    /// `text` with the first case-insensitive occurrence of the active filter
    /// bolded in the accent colour.
    private func highlighted(_ text: String) -> Text {
        guard !artistFilterText.isEmpty,
              let range = text.range(of: artistFilterText, options: .caseInsensitive)
        else {
            return Text(text)
        }
        let pre = String(text[text.startIndex ..< range.lowerBound])
        let match = String(text[range])
        let post = String(text[range.upperBound...])
        return Text(pre)
            + Text(match).foregroundColor(.accentColor).fontWeight(.bold)
            + Text(post)
    }

    // MARK: - Random Tab View

    private func randomTab() -> some View {
        VStack(spacing: 0) {
            // Meta strip
            HStack {
                Text("SHUFFLED · \(observable.randomAlbums.count) ALBUMS")
                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                    .foregroundColor(.textTertiary)

                Spacer()

                Button(action: { observable.retry() }) {
                    HStack(spacing: 4) {
                        Image(systemName: "arrow.clockwise")
                            .font(.system(size: 11))
                        Text("REFRESH")
                            .font(AppFont.ibmPlexMono(size: 11, weight: .medium))
                    }
                    .foregroundColor(.accentColor)
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.vertical, 8)

            if observable.isLoading {
                Spacer()
                ProgressView().tint(.accentColor)
                Spacer()
            } else {
                ScrollView {
                    let columns = isLarge
                        ? [GridItem(.adaptive(minimum: 168), spacing: 14)]
                        : [GridItem(.flexible(), spacing: 14), GridItem(.flexible(), spacing: 14)]

                    LazyVGrid(columns: columns, spacing: 14) {
                        ForEach(observable.randomAlbums, id: \.name) { album in
                            let imageUrl = container.mcwsClient.buildImageUrl(fileKey: album.artworkFileKey)
                            Button(action: { onAlbumClick(album) }) {
                                VStack(alignment: .leading, spacing: 4) {
                                    ZStack {
                                        if !imageUrl.isEmpty, let url = URL(string: imageUrl) {
                                            JrrAsyncImage(url: url) { image in
                                                image
                                                    .resizable()
                                                    .aspectRatio(contentMode: .fill)
                                            } placeholder: {
                                                Color.bg2
                                            }
                                        } else {
                                            // Fallback diagonal stripe tile
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
                                    .frame(maxWidth: .infinity)
                                    .aspectRatio(1, contentMode: .fit)
                                    .clipped()
                                    .cornerRadius(AppSpacing.radiusArt)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: AppSpacing.radiusArt)
                                            .stroke(Color.line2, lineWidth: 1),
                                    )

                                    Text(album.name)
                                        .font(AppFont.inter(size: 13, weight: .medium))
                                        .foregroundColor(.textPrimary)
                                        .lineLimit(1)

                                    Text(album.albumArtist)
                                        .font(AppFont.inter(size: 11.5, weight: .regular))
                                        .foregroundColor(.textSecondary)
                                        .lineLimit(1)
                                }
                            }
                            .buttonStyle(PlainButtonStyle())
                            .contextMenu {
                                Button(action: { observable.playAlbum(album) }) {
                                    Label("Play", systemImage: "play.fill")
                                }
                                Button(action: { observable.playAlbumNext(album) }) {
                                    Label("Play Next", systemImage: "arrow.right.to.line")
                                }
                                Button(action: { observable.addAlbumToQueue(album) }) {
                                    Label("Add to Queue", systemImage: "plus")
                                }
                                if !observable.isOffline {
                                    Button(action: { observable.downloadAlbum(album) }) {
                                        Label("Download", systemImage: "arrow.down.circle")
                                    }
                                }
                            }
                        }
                    }
                    .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                    .padding(.bottom, 20)
                }
            }
        }
    }

    // MARK: - Browse Tab View

    private func browseTab() -> some View {
        VStack(spacing: 0) {
            browseBreadcrumb()
            browseContent()
        }
    }

    @ViewBuilder
    private func browseBreadcrumb() -> some View {
        if observable.browseStack.count > 1 {
            Button(action: { observable.popBrowseNode() }) {
                HStack {
                    Image(systemName: "chevron.left")
                        .foregroundColor(.accentColor)
                        .font(.system(size: 16, weight: .bold))

                    Text(observable.browseStack.map(\.label).joined(separator: " / "))
                        .font(AppFont.inter(size: 13, weight: .regular))
                        .foregroundColor(.textSecondary)
                        .lineLimit(1)

                    Spacer()
                }
                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                .padding(.vertical, 12)
            }
        }
    }

    @ViewBuilder
    private func browseContent() -> some View {
        if observable.isLoading || observable.isTabLoading {
            Spacer()
            ProgressView().tint(.accentColor)
            Spacer()
        } else {
            ScrollView {
                if !observable.browseChildren.isEmpty {
                    browseChildrenList()
                } else if !observable.browseTracks.isEmpty {
                    browseTracksList()
                } else {
                    browseEmptyState()
                }
            }
        }
    }

    @ViewBuilder
    private func browseChildrenList() -> some View {
        if isLarge {
            LazyVGrid(columns: [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)], spacing: 8) {
                ForEach(observable.browseChildren, id: \.key) { browseItem in
                    browseChildRow(browseItem: browseItem)
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.top, 12)
        } else {
            LazyVStack(spacing: 8) {
                ForEach(observable.browseChildren, id: \.key) { browseItem in
                    browseChildRow(browseItem: browseItem)
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.top, 12)
        }
    }

    private func browseChildRow(browseItem: BrowseItem) -> some View {
        HStack {
            Text(browseItem.name)
                .font(AppFont.inter(size: 16, weight: .medium))
                .foregroundColor(.textPrimary)
            Spacer()

            Menu {
                Button(action: { observable.playBrowseItem(browseItem) }) {
                    Label("Play", systemImage: "play.fill")
                }
                Button(action: { observable.playBrowseItemNext(browseItem) }) {
                    Label("Play Next", systemImage: "arrow.right.to.line")
                }
                Button(action: { observable.addBrowseItemToQueue(browseItem) }) {
                    Label("Add to Queue", systemImage: "plus")
                }
                if !observable.isOffline {
                    Button(action: { observable.downloadBrowseItem(browseItem) }) {
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

            Image(systemName: "chevron.right")
                .font(.system(size: 14))
                .foregroundColor(.textTertiary)
                .padding(.trailing, 4)
        }
        .padding(16)
        .background(Color.bg2)
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.line, lineWidth: 1),
        )
        .onTapGesture {
            observable.pushBrowseNode(browseItem: browseItem)
        }
    }

    @ViewBuilder
    private func browseTracksList() -> some View {
        let cols = isLarge
            ? [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)]
            : [GridItem(.flexible())]
        LazyVGrid(columns: cols, spacing: 8) {
            ForEach(observable.browseTracks, id: \.fileKey) { track in
                trackRowItem(track: track) {
                    observable.playTracks(observable.browseTracks, startIndex: observable.browseTracks.firstIndex(of: track) ?? 0)
                }
            }
        }
        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
        .padding(.top, 12)
    }

    private func browseEmptyState() -> some View {
        VStack {
            Spacer().frame(height: 80)
            Text("Empty folder")
                .font(AppFont.inter(size: 14, weight: .regular))
                .foregroundColor(.textSecondary)
        }
    }

    // MARK: - Favorites Tab View

    @ViewBuilder
    private func favoritesTab() -> some View {
        let favoritedAlbums = stateObserver.favorites.filter { $0.type == "album" }

        if favoritedAlbums.isEmpty {
            VStack {
                Image(systemName: "star.fill")
                    .font(.system(size: 48))
                    .foregroundColor(.accentColor)
                    .padding(.bottom, 8)
                Text("Your Favorites")
                    .font(AppFont.inter(size: 16, weight: .bold))
                    .foregroundColor(.textPrimary)
                Text("Pinned albums will appear here.")
                    .font(AppFont.inter(size: 13, weight: .regular))
                    .foregroundColor(.textTertiary)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            ScrollView {
                LazyVStack(spacing: 8) {
                    ForEach(favoritedAlbums, id: \.identifier) { fav in
                        let parts = fav.identifier.split(separator: "|")
                        let artist = parts.count > 1 ? String(parts[1]) : "Unknown Artist"
                        let albumName = String(parts[0])
                        let album = Album(name: albumName, albumArtist: artist, folderPath: "", parentFolderPath: "", date: "", artworkFileKey: "", totalDiscs: 1, discNumber: 1)

                        HStack {
                            ZStack {
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
                                .frame(width: 48, height: 48)
                                .cornerRadius(4)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 4)
                                        .stroke(Color.line2, lineWidth: 1),
                                )
                            }

                            VStack(alignment: .leading, spacing: 4) {
                                Text(albumName)
                                    .styleItemTitle()
                                    .lineLimit(2)
                                Text(artist)
                                    .styleItemSubtitle()
                                    .lineLimit(1)
                            }
                            .padding(.leading, 8)

                            Spacer()

                            Menu {
                                Button(action: { infoAlbum = album }) {
                                    Label("Info", systemImage: "info.circle")
                                }
                                Button(action: { observable.playAlbum(album) }) {
                                    Label("Play", systemImage: "play.fill")
                                }
                                Button(action: { observable.playAlbumNext(album) }) {
                                    Label("Play Next", systemImage: "arrow.right.to.line")
                                }
                                Button(action: { observable.addAlbumToQueue(album) }) {
                                    Label("Add to Queue", systemImage: "plus")
                                }
                                if !observable.isOffline {
                                    Button(action: { observable.downloadAlbum(album) }) {
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
                        .padding(8)
                        .background(Color.bg2)
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.line, lineWidth: 1),
                        )
                        .onTapGesture {
                            onAlbumClick(album)
                        }
                    }
                }
                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                .padding(.top, 12)
            }
        }
    }

    // MARK: - Downloads Tab View

    @ViewBuilder
    private func downloadsTab() -> some View {
        if observable.isLoading {
            VStack {
                Spacer()
                ProgressView().tint(.accentColor)
                Spacer()
            }
        } else {
            if observable.downloadedTracks.isEmpty {
                VStack {
                    Spacer()
                    Text("No downloaded tracks")
                        .font(AppFont.inter(size: 14, weight: .regular))
                        .foregroundColor(.textSecondary)
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                if let artist = selectedArtist {
                    let artistTracks = observable.downloadedTracks.filter { track in
                        let artistVal = track.albumArtist.isEmpty ? "Unknown Artist" : track.albumArtist
                        return artistVal.lowercased() == artist.lowercased()
                    }

                    if let albumGroupId = selectedAlbumGroupId {
                        // Screen 3: Tracks for the selected album
                        let albumTracks = artistTracks.filter { $0.albumGroupId == albumGroupId }
                            .sorted { t1, t2 in
                                if t1.discNumber != t2.discNumber {
                                    return t1.discNumber < t2.discNumber
                                }
                                return t1.trackNumber < t2.trackNumber
                            }
                        let firstTrack = albumTracks.first

                        VStack(alignment: .leading, spacing: 0) {
                            // Back Button / Breadcrumb
                            Button(action: {
                                selectedAlbumGroupId = nil
                            }) {
                                HStack(spacing: 8) {
                                    Image(systemName: "chevron.left")
                                        .foregroundColor(.accentColor)
                                        .font(.system(size: 16, weight: .bold))
                                    Text(firstTrack?.album ?? "Unknown Album")
                                        .styleSubScreenTitle()
                                    Spacer()
                                }
                                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                                .padding(.vertical, 12)
                            }

                            ScrollView {
                                LazyVStack(alignment: .leading, spacing: 12) {
                                    if let firstTrack {
                                        albumHeaderRow(
                                            albumName: firstTrack.album,
                                            artistName: firstTrack.albumArtist,
                                            artworkFileKey: firstTrack.fileKey,
                                        )

                                        ForEach(0 ..< albumTracks.count, id: \.self) { idx in
                                            let track = albumTracks[idx]
                                            HStack(spacing: 12) {
                                                let trackNum = track.trackNumber == 0 ? idx + 1 : Int(track.trackNumber)
                                                Text(String(format: "%02d", trackNum))
                                                    .styleMonoLabel()
                                                    .foregroundColor(.accentColor)
                                                    .frame(width: 24, alignment: .leading)

                                                VStack(alignment: .leading, spacing: 2) {
                                                    Text(track.name)
                                                        .styleItemTitle()
                                                        .lineLimit(1)

                                                    if track.artist != track.albumArtist {
                                                        Text(track.artist)
                                                            .styleItemSubtitle()
                                                            .lineLimit(1)
                                                    }
                                                }

                                                Spacer()

                                                let secs = Int(track.durationMs / 1000)
                                                Text(String(format: "%d:%02d", secs / 60, secs % 60))
                                                    .styleMonoLabel()
                                                    .padding(.trailing, 4)

                                                PlaybackActionMenu(
                                                    playAction: { observable.playTracks([track], startIndex: 0) },
                                                    playShuffleAction: { observable.playTracksShuffled([track]) },
                                                    playNextAction: { observable.playTracksNext([track]) },
                                                    addToQueueAction: { observable.addTracksToQueue([track]) },
                                                    infoAction: { infoTrack = track },
                                                    rotateEllipsis: true,
                                                )
                                            }
                                            .padding(8)
                                            .background(Color.bg2)
                                            .cornerRadius(10)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 10)
                                                    .stroke(Color.line, lineWidth: 1),
                                            )
                                            .onTapGesture {
                                                observable.playTracks(albumTracks, startIndex: idx)
                                            }
                                        }
                                    }
                                }
                                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                                .padding(.top, 12)
                            }
                        }
                    } else {
                        // Screen 2: Albums for the selected artist
                        let groupedDict = Dictionary(grouping: artistTracks) { $0.albumGroupId }
                        let albums = groupedDict.map { groupId, albumTracks -> DownloadAlbum in
                            let firstTrack = albumTracks.first
                            let albumName = firstTrack?.album ?? "Unknown Album"
                            let artworkFileKey = firstTrack?.fileKey ?? ""
                            return DownloadAlbum(
                                groupId: groupId,
                                name: albumName,
                                artworkFileKey: artworkFileKey,
                                trackCount: albumTracks.count,
                            )
                        }.sorted { $0.name.lowercased() < $1.name.lowercased() }

                        VStack(alignment: .leading, spacing: 0) {
                            // Back Button / Breadcrumb
                            Button(action: {
                                selectedArtist = nil
                            }) {
                                HStack(spacing: 8) {
                                    Image(systemName: "chevron.left")
                                        .foregroundColor(.accentColor)
                                        .font(.system(size: 16, weight: .bold))
                                    Text(artist)
                                        .styleSubScreenTitle()
                                    Spacer()
                                }
                                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                                .padding(.vertical, 12)
                            }

                            ScrollView {
                                LazyVStack(spacing: 8) {
                                    ForEach(albums, id: \.self) { album in
                                        let albumTracks = artistTracks.filter { $0.albumGroupId == album.groupId }
                                        HStack {
                                            // Artwork
                                            let imageUrl = container.mcwsClient.buildImageUrl(fileKey: album.artworkFileKey)
                                            ZStack {
                                                if !imageUrl.isEmpty, let url = URL(string: imageUrl) {
                                                    JrrAsyncImage(url: url) { image in
                                                        image
                                                            .resizable()
                                                            .aspectRatio(contentMode: .fill)
                                                    } placeholder: {
                                                        Color.bg3
                                                    }
                                                } else {
                                                    Color.bg3
                                                }
                                            }
                                            .frame(width: 60, height: 60)
                                            .cornerRadius(4)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 4)
                                                    .stroke(Color.line2, lineWidth: 1),
                                            )

                                            VStack(alignment: .leading, spacing: 4) {
                                                Text(album.name)
                                                    .styleItemTitle()
                                                    .lineLimit(2)

                                                Text("\(album.trackCount) \(album.trackCount == 1 ? "track" : "tracks")")
                                                    .styleItemSubtitle()
                                                    .foregroundColor(.textSecondary)
                                                    .lineLimit(1)
                                            }
                                            .padding(.leading, 8)

                                            Spacer()

                                            PlaybackActionMenu(
                                                playAction: { observable.playTracks(albumTracks, startIndex: 0) },
                                                playShuffleAction: { observable.playTracksShuffled(albumTracks) },
                                                playNextAction: { observable.playTracksNext(albumTracks) },
                                                addToQueueAction: { observable.addTracksToQueue(albumTracks) },
                                                infoAction: {
                                                    if let ft = albumTracks.first {
                                                        infoAlbum = Album(track: ft)
                                                    }
                                                },
                                            )
                                        }
                                        .padding(8)
                                        .background(Color.bg2)
                                        .cornerRadius(10)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(Color.line, lineWidth: 1),
                                        )
                                        .onTapGesture {
                                            selectedAlbumGroupId = album.groupId
                                        }
                                    }
                                }
                                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                                .padding(.top, 8)
                            }
                        }
                    }
                } else {
                    // Screen 1: List of Artists
                    let artists = Array(Set(observable.downloadedTracks.map { track in
                        track.albumArtist.isEmpty ? "Unknown Artist" : track.albumArtist
                    })).sorted { $0.lowercased() < $1.lowercased() }

                    ScrollView {
                        LazyVStack(spacing: 8) {
                            // All Downloads header/special item
                            HStack {
                                ZStack {
                                    Circle()
                                        .fill(Color.bg3)
                                        .frame(width: 36, height: 36)

                                    Image(systemName: "arrow.down.circle.fill")
                                        .font(.system(size: 16, weight: .bold))
                                        .foregroundColor(.accentColor)
                                }

                                VStack(alignment: .leading, spacing: 4) {
                                    Text("All Downloads")
                                        .font(AppFont.inter(size: 16, weight: .medium))
                                        .foregroundColor(.textPrimary)
                                    Text("\(observable.downloadedTracks.count) \(observable.downloadedTracks.count == 1 ? "track" : "tracks")")
                                        .font(AppFont.inter(size: 13, weight: .regular))
                                        .foregroundColor(.textSecondary)
                                }
                                .padding(.leading, 8)

                                Spacer()

                                PlaybackActionMenu(
                                    playAction: { observable.playTracks(observable.downloadedTracks, startIndex: 0) },
                                    playShuffleAction: { observable.playTracksShuffled(observable.downloadedTracks) },
                                    playNextAction: { observable.playTracksNext(observable.downloadedTracks) },
                                    addToQueueAction: { observable.addTracksToQueue(observable.downloadedTracks) },
                                )
                            }
                            .padding(.vertical, 8)
                            .padding(.horizontal, 12)
                            .background(Color.bg2)
                            .cornerRadius(10)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.line, lineWidth: 1),
                            )
                            .onTapGesture {
                                observable.playTracks(observable.downloadedTracks, startIndex: 0)
                            }

                            ForEach(artists, id: \.self) { artist in
                                let artistTracks = observable.downloadedTracks.filter { track in
                                    let artistVal = track.albumArtist.isEmpty ? "Unknown Artist" : track.albumArtist
                                    return artistVal.lowercased() == artist.lowercased()
                                }
                                HStack {
                                    // Avatar circle with initial
                                    ZStack {
                                        Circle()
                                            .fill(Color.bg3)
                                            .frame(width: 36, height: 36)

                                        Text(artist.prefix(1).uppercased())
                                            .font(AppFont.ibmPlexMono(size: 14, weight: .medium))
                                            .foregroundColor(.accentColor)
                                    }

                                    Text(artist)
                                        .font(AppFont.inter(size: 16, weight: .medium))
                                        .foregroundColor(.textPrimary)
                                        .padding(.leading, 8)

                                    Spacer()

                                    PlaybackActionMenu(
                                        playAction: { observable.playTracks(artistTracks, startIndex: 0) },
                                        playShuffleAction: { observable.playTracksShuffled(artistTracks) },
                                        playNextAction: { observable.playTracksNext(artistTracks) },
                                        addToQueueAction: { observable.addTracksToQueue(artistTracks) },
                                    )
                                }
                                .padding(.vertical, 8)
                                .padding(.horizontal, 12)
                                .background(Color.bg2)
                                .cornerRadius(10)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(Color.line, lineWidth: 1),
                                )
                                .onTapGesture {
                                    selectedArtist = artist
                                }
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.top, 8)
                    }
                }
            }
        }
    }

    private func albumHeaderRow(albumName: String, artistName: String, artworkFileKey: String) -> some View {
        let imageUrl = container.mcwsClient.buildImageUrl(fileKey: artworkFileKey)
        return HStack(spacing: 12) {
            ZStack {
                if !imageUrl.isEmpty, let url = URL(string: imageUrl) {
                    JrrAsyncImage(url: url) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Color.bg3
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
            .frame(width: 48, height: 48)
            .cornerRadius(4)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(Color.line2, lineWidth: 1),
            )

            VStack(alignment: .leading, spacing: 2) {
                Text(albumName.isEmpty ? "Unknown Album" : albumName)
                    .font(AppFont.inter(size: 16, weight: .bold))
                    .foregroundColor(.textPrimary)
                    .lineLimit(1)

                Text(artistName.isEmpty ? "Unknown Artist" : artistName)
                    .font(AppFont.inter(size: 13, weight: .regular))
                    .foregroundColor(.textSecondary)
                    .lineLimit(1)
            }

            Spacer()
        }
        .padding(.top, 12)
        .padding(.bottom, 4)
    }

    private func groupedTrackRowItem(track: Track, indexInAlbum: Int, action: @escaping () -> Void) -> some View {
        HStack(spacing: 12) {
            let trackNum = track.trackNumber == 0 ? indexInAlbum + 1 : Int(track.trackNumber)
            Text(String(format: "%02d", trackNum))
                .styleMonoLabel()
                .foregroundColor(.accentColor)
                .frame(width: 24, alignment: .leading)

            VStack(alignment: .leading, spacing: 2) {
                Text(track.name)
                    .styleItemTitle()
                    .lineLimit(1)

                let secs = track.durationMs / 1000
                let timeStr = String(format: "%d:%02d", secs / 60, secs % 60)
                let subtitleText = track.artist != track.albumArtist ? "\(track.artist) • \(timeStr)" : timeStr
                Text(subtitleText)
                    .styleItemSubtitle()
                    .lineLimit(1)
            }
            .padding(.leading, 4)

            Spacer()

            if track.numberPlays > 0 {
                Image(systemName: "headphones")
                    .font(.system(size: 12))
                    .foregroundColor(.textTertiary)
                    .padding(.trailing, 4)
            }
        }
        .padding(8)
        .background(Color.bg2)
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.line, lineWidth: 1),
        )
        .contentShape(Rectangle())
        .onTapGesture(perform: action)
    }

    // MARK: - List Item Row Templates

    private func trackRowItem(track: Track, action: @escaping () -> Void) -> some View {
        let trackImageUrl = container.mcwsClient.buildImageUrl(fileKey: track.fileKey)
        return HStack {
            ZStack {
                if !trackImageUrl.isEmpty, let url = URL(string: trackImageUrl) {
                    JrrAsyncImage(url: url) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Color.bg3
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
            .frame(width: 48, height: 48)
            .cornerRadius(4)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(Color.line2, lineWidth: 1),
            )

            VStack(alignment: .leading, spacing: 4) {
                Text(track.name)
                    .styleItemTitle()
                    .lineLimit(1)

                let secs = track.durationMs / 1000
                let timeStr = String(format: "%d:%02d", secs / 60, secs % 60)
                Text("\(track.artist) • \(timeStr)")
                    .styleItemSubtitle()
                    .lineLimit(1)
            }
            .padding(.leading, 8)

            Spacer()

            if track.numberPlays > 0 {
                Image(systemName: "headphones")
                    .font(.system(size: 12))
                    .foregroundColor(.textTertiary)
                    .padding(.trailing, 4)
            }

            Menu {
                Button(action: { infoTrack = track }) {
                    Label("Info", systemImage: "info.circle")
                }
                Button(action: { observable.playTrack(track) }) {
                    Label("Play", systemImage: "play.fill")
                }
                Button(action: { observable.playTrackNext(track) }) {
                    Label("Play Next", systemImage: "arrow.right.to.line")
                }
                Button(action: { observable.addTrackToQueue(track) }) {
                    Label("Add to Queue", systemImage: "plus")
                }
                if !observable.isOffline {
                    Button(action: { observable.downloadTrack(track) }) {
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
        .padding(8)
        .background(Color.bg2)
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.line, lineWidth: 1),
        )
        .onTapGesture(perform: action)
    }

    private func albumRowItem(album: Album, action: @escaping () -> Void) -> some View {
        let imageUrl = container.mcwsClient.buildImageUrl(fileKey: album.artworkFileKey)
        return HStack {
            ZStack {
                if !imageUrl.isEmpty, let url = URL(string: imageUrl) {
                    JrrAsyncImage(url: url) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fill)
                    } placeholder: {
                        Color.bg3
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
            .frame(width: 60, height: 60)
            .cornerRadius(4)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(Color.line2, lineWidth: 1),
            )

            VStack(alignment: .leading, spacing: 4) {
                Text(album.name)
                    .styleItemTitle()
                    .lineLimit(2)

                let pathParts = [album.parentFolderPath, album.folderPath]
                    .flatMap { $0.replacingOccurrences(of: "\\", with: "/").components(separatedBy: "/") }
                    .filter { !$0.isEmpty }
                let path = pathParts.suffix(2).joined(separator: "/")
                if !path.isEmpty {
                    Text(path)
                        .font(AppFont.inter(size: 11, weight: .regular))
                        .foregroundColor(.textTertiary)
                }

                Text(album.date.isEmpty ? "Unknown Year" : album.date)
                    .styleItemSubtitle()
            }
            .padding(.leading, 8)

            Spacer()

            Menu {
                Button(action: { infoAlbum = album }) {
                    Label("Info", systemImage: "info.circle")
                }
                Button(action: { observable.playAlbum(album) }) {
                    Label("Play", systemImage: "play.fill")
                }
                Button(action: { observable.playAlbumNext(album) }) {
                    Label("Play Next", systemImage: "arrow.right.to.line")
                }
                Button(action: { observable.addAlbumToQueue(album) }) {
                    Label("Add to Queue", systemImage: "plus")
                }
                if !observable.isOffline {
                    Button(action: { observable.downloadAlbum(album) }) {
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
        .padding(8)
        .background(Color.bg2)
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.line, lineWidth: 1),
        )
        .onTapGesture(perform: action)
    }
}

struct DownloadAlbum: Hashable {
    let groupId: String
    let name: String
    let artworkFileKey: String
    let trackCount: Int
}

struct PlaybackActionMenu: View {
    let playAction: () -> Void
    let playShuffleAction: () -> Void
    let playNextAction: () -> Void
    let addToQueueAction: () -> Void
    var infoAction: (() -> Void)?
    var rotateEllipsis: Bool = false

    var body: some View {
        Menu {
            Button(action: playAction) {
                Label("Play", systemImage: "play.fill")
            }
            Button(action: playShuffleAction) {
                Label("Play Shuffle", systemImage: "shuffle")
            }
            Button(action: playNextAction) {
                Label("Play Next", systemImage: "arrow.right.to.line")
            }
            Button(action: addToQueueAction) {
                Label("Add to Queue", systemImage: "plus")
            }
            if let infoAction {
                Button(action: infoAction) {
                    Label("Info", systemImage: "info.circle")
                }
            }
        } label: {
            if rotateEllipsis {
                Image(systemName: "ellipsis")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.textSecondary)
                    .rotationEffect(.degrees(90))
                    .frame(width: 32, height: 32)
            } else {
                Image(systemName: "ellipsis")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.textSecondary)
                    .frame(width: 32, height: 32)
            }
        }
        .buttonStyle(PlainButtonStyle())
    }
}
