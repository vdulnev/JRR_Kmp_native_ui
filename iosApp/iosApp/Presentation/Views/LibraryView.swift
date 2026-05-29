import SwiftUI
import SharedLogic

private let log = SwiftLog("ui:iOS:Library")

@Observable
@MainActor
class LibraryObservable {
    let viewModel: LibraryViewModel
    
    var searchQuery: String = ""
    var searchResults: [Track] = []
    var currentTab: String = "artists"
    var artists: [String] = []
    var selectedArtist: String? = nil
    var artistAlbums: [Album] = []
    var randomAlbums: [Album] = []
    var browseStack: [BrowseNode] = []
    var browseChildren: [BrowseItem] = []
    var browseTracks: [Track] = []
    var downloadedTracks: [Track] = []
    var isOffline: Bool = false
    var isLoading: Bool = false
    var isTabLoading: Bool = false
    var transientError: String? = nil
    
    @ObservationIgnored private var observeTask: Task<Void, Never>?

    init(viewModel: LibraryViewModel) {
        log.d("init")
        self.viewModel = viewModel

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
    }
    
    private func sync(state: LibraryViewState) {
        self.searchQuery = state.searchQuery
        self.searchResults = state.searchResults
        self.currentTab = state.currentTab
        self.artists = state.artists
        self.selectedArtist = state.selectedArtist
        self.artistAlbums = state.artistAlbums
        self.randomAlbums = state.randomAlbums
        self.browseStack = state.browseStack
        self.browseChildren = state.browseChildren
        self.browseTracks = state.browseTracks
        self.downloadedTracks = state.downloadedTracks
        self.isOffline = state.isOffline
        self.isLoading = state.isLoading
        self.isTabLoading = state.isTabLoading
        self.transientError = state.transientError
    }
    
    func updateSearchQuery(_ query: String) {
        viewModel.updateSearchQuery(query: query)
    }
    
    func switchTab(_ tab: String) {
        viewModel.switchTab(tab: tab)
    }
    
    func selectArtist(_ artistName: String?) {
        viewModel.selectArtist(artistName: artistName)
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

struct LibraryView: View {
    @Environment(AppContainer.self) private var container
    @EnvironmentObject private var stateObserver: PlaybackStateObserver
    @State private var observable: LibraryObservable
    let onAlbumClick: (Album) -> Void // AlbumName, ArtistName
    @State private var isSearching = false
    @State private var searchQueryText = ""
    @State private var selectedArtist: String? = nil
    @State private var selectedAlbumGroupId: String? = nil
    
    init(viewModel: LibraryViewModel, onAlbumClick: @escaping (Album) -> Void) {
        self._observable = State(initialValue: LibraryObservable(viewModel: viewModel))
        self.onAlbumClick = onAlbumClick
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Header with search toggle
            HStack {
                if isSearching {
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.textTertiary)
                        
                        TextField("Search tracks, artists...", text: $searchQueryText)
                            .foregroundColor(.textPrimary)
                            .font(AppFont.inter(size: 14, weight: .regular))
                            .textInputAutocapitalization(.never)
                            .disableAutocorrection(true)
                            .onChange(of: searchQueryText) { oldValue, newValue in
                                observable.updateSearchQuery(newValue)
                            }
                        
                        if !searchQueryText.isEmpty {
                            Button(action: {
                                searchQueryText = ""
                                observable.updateSearchQuery("")
                            }) {
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
                            .stroke(Color.line2, lineWidth: 1)
                    )
                    
                    Button(action: {
                        isSearching = false
                        searchQueryText = ""
                        observable.updateSearchQuery("")
                    }) {
                        Text("Cancel")
                            .font(AppFont.inter(size: 14, weight: .medium))
                            .foregroundColor(.accentColor)
                    }
                    .padding(.leading, 8)
                } else {
                    VStack(alignment: .leading, spacing: 4) {
                        Text("LIBRARY")
                            .styleSectionLabel()
                        
                        Text("Browse")
                            .styleScreenTitle()
                    }
                    
                    Spacer()
                    
                    Button(action: { isSearching = true }) {
                        Image(systemName: "magnifyingglass")
                            .font(.system(size: 18, weight: .medium))
                            .foregroundColor(.textPrimary)
                            .frame(width: 44, height: 44)
                    }
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.vertical, 12)
            .background(Color.bg1)
            
            if isSearching {
                // Search Results
                if observable.isTabLoading {
                    VStack {
                        Spacer()
                        ProgressView()
                            .tint(.accentColor)
                        Spacer()
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVStack(spacing: 8) {
                            ForEach(observable.searchResults, id: \.fileKey) { track in
                                trackRowItem(track: track) {
                                    observable.playTrack(track)
                                }
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.top, 8)
                    }
                    .background(Color.bg1)
                }
            } else {
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
                        artistsTab()
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
        }
        .background(Color.bg1.ignoresSafeArea())
        .onAppear {
            if observable.artists.isEmpty {
                observable.retry()
            }
        }
        .onChange(of: observable.currentTab) { oldValue, newValue in
            if newValue != "downloads" {
                selectedArtist = nil
                selectedAlbumGroupId = nil
            }
        }
    }
    
    // Tab Button Helper
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
    @ViewBuilder
    private func artistsTab() -> some View {
        ZStack {
            // Artists List
            VStack(spacing: 0) {
                if observable.isLoading {
                    VStack {
                        Spacer()
                        ProgressView().tint(.accentColor)
                        Spacer()
                    }
                } else {
                    ScrollViewReader { proxy in
                        ScrollView {
                            LazyVStack(spacing: 8) {
                                ForEach(observable.artists, id: \.self) { artist in
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
                                    }
                                    .padding(.vertical, 8)
                                    .padding(.horizontal, 12)
                                    .background(Color.bg2)
                                    .cornerRadius(10)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 10)
                                            .stroke(Color.line, lineWidth: 1)
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
                        .overlay(alignment: .trailing) {
                            AlphabetIndexBar(
                                letters: orderedSectionLetters(observable.artists),
                                bottomInset: 96
                            ) { letter in
                                if let target = observable.artists.first(
                                    where: { sectionLetter(for: $0) == letter }
                                ) {
                                    withAnimation { proxy.scrollTo(target, anchor: .top) }
                                }
                            }
                        }
                    }
                }
            }
            .opacity(observable.selectedArtist == nil ? 1 : 0)
            .disabled(observable.selectedArtist != nil)
            
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
                    
                    if observable.isTabLoading {
                        Spacer()
                        ProgressView().tint(.accentColor)
                        Spacer()
                    } else {
                        ScrollViewReader { proxy in
                            ScrollView {
                                LazyVStack(spacing: 8) {
                                    ForEach(observable.artistAlbums, id: \.albumGroupId) { album in
                                        albumRowItem(album: album) {
                                            onAlbumClick(album)
                                        }
                                    }
                                }
                                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                                .padding(.trailing, 18)
                                .padding(.top, 8)
                            }
                            .overlay(alignment: .trailing) {
                                AlphabetIndexBar(
                                    letters: orderedSectionLetters(observable.artistAlbums.map { $0.name }),
                                    bottomInset: 96
                                ) { letter in
                                    if let target = observable.artistAlbums.first(
                                        where: { sectionLetter(for: $0.name) == letter }
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
    }

    // MARK: - Random Tab View
    @ViewBuilder
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
                    let columns = [
                        GridItem(.flexible(), spacing: 14),
                        GridItem(.flexible(), spacing: 14)
                    ]
                    
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
                                                    style: StrokeStyle(lineWidth: 4)
                                                )
                                            }
                                        }
                                    }
                                    .frame(height: 165)
                                    .cornerRadius(AppSpacing.radiusArt)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: AppSpacing.radiusArt)
                                            .stroke(Color.line2, lineWidth: 1)
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
    @ViewBuilder
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

                    Text(observable.browseStack.map { $0.label }.joined(separator: " / "))
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
        LazyVStack(spacing: 8) {
            ForEach(observable.browseChildren, id: \.key) { browseItem in
                browseChildRow(browseItem: browseItem)
            }
        }
        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
        .padding(.top, 12)
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
                .stroke(Color.line, lineWidth: 1)
        )
        .onTapGesture {
            observable.pushBrowseNode(browseItem: browseItem)
        }
    }

    @ViewBuilder
    private func browseTracksList() -> some View {
        LazyVStack(spacing: 8) {
            ForEach(observable.browseTracks, id: \.fileKey) { track in
                trackRowItem(track: track) {
                    observable.playTracks(observable.browseTracks, startIndex: observable.browseTracks.firstIndex(of: track) ?? 0)
                }
            }
        }
        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
        .padding(.top, 12)
    }

    @ViewBuilder
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
                                        style: StrokeStyle(lineWidth: 4)
                                    )
                                }
                                .frame(width: 48, height: 48)
                                .cornerRadius(4)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 4)
                                        .stroke(Color.line2, lineWidth: 1)
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
                                    .rotationEffect(.degrees(90))
                                    .frame(width: 32, height: 32)
                            }
                            .buttonStyle(PlainButtonStyle())
                        }
                        .padding(8)
                        .background(Color.bg2)
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.line, lineWidth: 1)
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
                                    if let firstTrack = firstTrack {
                                        albumHeaderRow(
                                            albumName: firstTrack.album,
                                            artistName: firstTrack.albumArtist,
                                            artworkFileKey: firstTrack.fileKey
                                        )
                                        
                                        ForEach(0..<albumTracks.count, id: \.self) { idx in
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
                                                    addToQueueAction: { observable.addTracksToQueue([track]) }
                                                )
                                            }
                                            .padding(8)
                                            .background(Color.bg2)
                                            .cornerRadius(10)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 10)
                                                    .stroke(Color.line, lineWidth: 1)
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
                                trackCount: albumTracks.count
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
                                                    .stroke(Color.line2, lineWidth: 1)
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
                                                addToQueueAction: { observable.addTracksToQueue(albumTracks) }
                                            )
                                        }
                                        .padding(8)
                                        .background(Color.bg2)
                                        .cornerRadius(10)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 10)
                                                .stroke(Color.line, lineWidth: 1)
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
                                    addToQueueAction: { observable.addTracksToQueue(observable.downloadedTracks) }
                                )
                            }
                            .padding(.vertical, 8)
                            .padding(.horizontal, 12)
                            .background(Color.bg2)
                            .cornerRadius(10)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.line, lineWidth: 1)
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
                                        addToQueueAction: { observable.addTracksToQueue(artistTracks) }
                                    )
                                }
                                .padding(.vertical, 8)
                                .padding(.horizontal, 12)
                                .background(Color.bg2)
                                .cornerRadius(10)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(Color.line, lineWidth: 1)
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
                            style: StrokeStyle(lineWidth: 4)
                        )
                    }
                }
            }
            .frame(width: 48, height: 48)
            .cornerRadius(4)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(Color.line2, lineWidth: 1)
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
                .stroke(Color.line, lineWidth: 1)
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
                            style: StrokeStyle(lineWidth: 4)
                        )
                    }
                }
            }
            .frame(width: 48, height: 48)
            .cornerRadius(4)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(Color.line2, lineWidth: 1)
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
                .stroke(Color.line, lineWidth: 1)
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
                            style: StrokeStyle(lineWidth: 4)
                        )
                    }
                }
            }
            .frame(width: 60, height: 60)
            .cornerRadius(4)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(Color.line2, lineWidth: 1)
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
                    .rotationEffect(.degrees(90))
                    .frame(width: 32, height: 32)
            }
            .buttonStyle(PlainButtonStyle())
        }
        .padding(8)
        .background(Color.bg2)
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.line, lineWidth: 1)
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
        } label: {
            Image(systemName: "ellipsis")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.textSecondary)
                .rotationEffect(.degrees(90))
                .frame(width: 32, height: 32)
        }
        .buttonStyle(PlainButtonStyle())
    }
}
