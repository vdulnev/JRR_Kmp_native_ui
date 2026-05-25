import SwiftUI
import SharedLogic

struct BrowseNode: Hashable, Identifiable {
    var id: String { nodeId }
    let label: String
    let nodeId: String
}

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
    var browseChildren: [String: String] = [:]
    var browseTracks: [Track] = []
    var isOffline: Bool = false
    var isLoading: Bool = false
    var isTabLoading: Bool = false
    var transientError: String? = nil
    
    private let subscription = FlowSubscription()
    
    init(viewModel: LibraryViewModel) {
        self.viewModel = viewModel
        
        let initial = viewModel.state.value as! LibraryViewState
        sync(state: initial)
        
        self.subscription.disposable = FlowObserver<LibraryViewState>(flow: viewModel.state).start { [weak self] state in
            if let state = state {
                Task { @MainActor in
                    self?.sync(state: state)
                }
            }
        }
    }
    
    private func sync(state: LibraryViewState) {
        self.searchQuery = state.searchQuery
        self.searchResults = state.searchResults
        self.currentTab = state.currentTab
        self.artists = state.artists
        self.selectedArtist = state.selectedArtist
        self.artistAlbums = state.artistAlbums
        self.randomAlbums = state.randomAlbums
        
        // Map browse stack safely
        if let kotlinStack = state.browseStack as? [KotlinPair<AnyObject, AnyObject>] {
            self.browseStack = kotlinStack.map { pair in
                let label = pair.first as? String ?? ""
                let id = pair.second as? String ?? ""
                return BrowseNode(label: label, nodeId: id)
            }
        } else {
            self.browseStack = []
        }
        
        self.browseChildren = state.browseChildren
        self.browseTracks = state.browseTracks
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
    
    func pushBrowseNode(label: String, nodeId: String) {
        viewModel.pushBrowseNode(label: label, nodeId: nodeId)
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
    
    func retry() {
        viewModel.retry()
    }
    
    func clearTransientError() {
        viewModel.clearTransientError()
    }
}

struct LibraryView: View {
    @State private var observable: LibraryObservable
    let onAlbumClick: (Album) -> Void // AlbumName, ArtistName
    @State private var isSearching = false
    @State private var searchQueryText = ""
    
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
                    ScrollView {
                        LazyVStack(spacing: 8) {
                            ForEach(observable.artistAlbums, id: \.name) { album in
                                albumRowItem(album: album) {
                                    onAlbumClick(album)
                                }
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.top, 8)
                    }
                }
            }
        } else {
            if observable.isLoading {
                VStack {
                    Spacer()
                    ProgressView().tint(.accentColor)
                    Spacer()
                }
            } else {
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
                                
                                Image(systemName: "chevron.right")
                                    .font(.system(size: 14))
                                    .foregroundColor(.textTertiary)
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
                    .padding(.top, 12)
                }
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
                            let imageUrl = McwsClient.shared.buildImageUrl(fileKey: album.artworkFileKey)
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
            // Breadcrumb navigation header
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
            
            if observable.isLoading || observable.isTabLoading {
                Spacer()
                ProgressView().tint(.accentColor)
                Spacer()
            } else {
                ScrollView {
                    if !observable.browseChildren.isEmpty {
                        LazyVStack(spacing: 8) {
                            ForEach(observable.browseChildren.sorted(by: { $0.key < $1.key }), id: \.value) { nodeLabel, nodeId in
                                HStack {
                                    Text(nodeLabel)
                                        .font(AppFont.inter(size: 16, weight: .medium))
                                        .foregroundColor(.textPrimary)
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .font(.system(size: 14))
                                        .foregroundColor(.textTertiary)
                                }
                                .padding(16)
                                .background(Color.bg2)
                                .cornerRadius(10)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 10)
                                        .stroke(Color.line, lineWidth: 1)
                                )
                                .onTapGesture {
                                    observable.pushBrowseNode(label: nodeLabel, nodeId: nodeId)
                                }
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.top, 12)
                    } else if !observable.browseTracks.isEmpty {
                        LazyVStack(spacing: 8) {
                            ForEach(observable.browseTracks, id: \.fileKey) { track in
                                trackRowItem(track: track) {
                                    observable.playTracks(observable.browseTracks, startIndex: observable.browseTracks.firstIndex(of: track) ?? 0)
                                }
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.top, 12)
                    } else {
                        VStack {
                            Spacer().frame(height: 80)
                            Text("Empty folder")
                                .font(AppFont.inter(size: 14, weight: .regular))
                                .foregroundColor(.textSecondary)
                        }
                    }
                }
            }
        }
    }
    
    // MARK: - Favorites Tab View
    @ViewBuilder
    private func favoritesTab() -> some View {
        let favoritedAlbums = PlaybackStateObserver.shared.favorites.filter { $0.type == "album" }
        
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
                                      .lineLimit(1)
                                  Text(artist)
                                      .styleItemSubtitle()
                                      .lineLimit(1)
                            }
                            .padding(.leading, 8)
                            
                            Spacer()
                            
                            Image(systemName: "chevron.right")
                                .font(.system(size: 14))
                                .foregroundColor(.textTertiary)
                        }
                        .padding(8)
                        .background(Color.bg2)
                        .cornerRadius(10)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10)
                                .stroke(Color.line, lineWidth: 1)
                        )
                        .onTapGesture {
                            onAlbumClick(Album(name: albumName, albumArtist: artist, folderPath: "", parentFolderPath: "", date: "", artworkFileKey: "", totalDiscs: 1, discNumber: 1))
                        }
                    }
                }
                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                .padding(.top, 12)
            }
        }
    }
    
    // MARK: - List Item Row Templates
    private func trackRowItem(track: Track, action: @escaping () -> Void) -> some View {
        HStack {
            ZStack {
                if !track.imageUrl.isEmpty, let url = URL(string: track.imageUrl) {
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
                
                Text(track.artist)
                    .styleItemSubtitle()
                    .lineLimit(1)
            }
            .padding(.leading, 8)
            
            Spacer()
            
            let secs = track.durationMs / 1000
            Text(String(format: "%d:%02d", secs / 60, secs % 60))
                .styleMonoLabel()
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
        let imageUrl = McwsClient.shared.buildImageUrl(fileKey: album.artworkFileKey)
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
                    .lineLimit(1)
                
                Text(album.date.isEmpty ? "Unknown Year" : album.date)
                    .styleItemSubtitle()
            }
            .padding(.leading, 8)
            
            Spacer()
            
            Image(systemName: "chevron.right")
                .font(.system(size: 14))
                .foregroundColor(.textTertiary)
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
