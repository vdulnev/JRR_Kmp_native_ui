import SwiftUI
import SharedLogic

struct LibraryView: View {
    @ObservedObject var audioHandler = PlaybackStateObserver.shared
    let onAlbumClick: (String, String) -> Void // AlbumName, ArtistName
    
    @State private var searchQuery = ""
    @State private var isSearching = false
    @State private var searchResults: [TrackInfo] = []
    @State private var isSearchingLoading = false
    
    @State private var currentTab = 0 // 0 = Artists, 1 = Random, 2 = Browse, 3 = Favorites
    
    // Tab Data states
    @State private var artists: [String] = []
    @State private var selectedArtist: String? = nil
    @State private var artistAlbums: [Album] = []
    @State private var isLoadingArtistAlbums = false
    @State private var isLoadingArtists = false
    
    @State private var randomAlbums: [Album] = []
    @State private var isLoadingRandom = false
    
    // Browse tree stack: list of (Label, ID)
    @State private var browseStack = [("Library", "-1")]
    @State private var browseChildren: [String: String] = [:] // Name -> ID
    @State private var browseTracks: [TrackInfo] = []
    @State private var isLoadingBrowse = false
    
    var body: some View {
        VStack(spacing: 0) {
            // Header with search toggle
            HStack {
                if isSearching {
                    HStack {
                        Image(systemName: "magnifyingglass")
                            .foregroundColor(.textTertiary)
                        
                        TextField("Search tracks, artists...", text: $searchQuery)
                            .foregroundColor(.textPrimary)
                            .font(AppFont.inter(size: 14, weight: .regular))
                            .textInputAutocapitalization(.never)
                            .disableAutocorrection(true)
                            .onChange(of: searchQuery) { newValue in
                                performSearch(query: newValue)
                            }
                        
                        if !searchQuery.isEmpty {
                            Button(action: { searchQuery = "" }) {
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
                        searchQuery = ""
                        searchResults = []
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
                if isSearchingLoading {
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
                            ForEach(searchResults, id: \.fileKey) { track in
                                trackRowItem(track: track) {
                                    JrrDependencies.shared.facade.setQueue(tracks: [track], startIndex: 0)
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
                    tabButton(title: "Artists", index: 0)
                    if !audioHandler.activeZone.isOffline {
                        tabButton(title: "Random", index: 1)
                        tabButton(title: "Browse", index: 2)
                    }
                    tabButton(title: "Favorites", index: 3)
                }
                .background(Color.bg1)
                
                // Tab Content
                Group {
                    switch currentTab {
                    case 0:
                        artistsTab()
                    case 1:
                        randomTab()
                    case 2:
                        browseTab()
                    case 3:
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
            loadTabData()
        }
        .onChange(of: audioHandler.activeZone.isOffline) { isOffline in
            if isOffline && (currentTab == 1 || currentTab == 2) {
                currentTab = 0
            }
            loadTabData()
        }
    }
    
    // Tab Button Helper
    private func tabButton(title: String, index: Int) -> some View {
        Button(action: {
            currentTab = index
            loadTabData()
        }) {
            VStack(spacing: 4) {
                Text(title.uppercased())
                    .font(AppFont.ibmPlexMono(size: 10.5, weight: .medium))
                    .tracking(1.6)
                    .foregroundColor(currentTab == index ? .accentColor : .textTertiary)
                
                // Active Underline
                Rectangle()
                    .fill(currentTab == index ? Color.accentColor : Color.clear)
                    .frame(height: 2)
            }
            .frame(maxWidth: .infinity)
            .padding(.top, 12)
        }
    }
    
    private func loadTabData() {
        switch currentTab {
        case 0:
            if artists.isEmpty {
                isLoadingArtists = true
                Task {
                    do {
                        let list = try await JrrDependencies.shared.libraryRepository.getArtists()
                        await MainActor.run {
                            self.artists = list
                            self.isLoadingArtists = false
                        }
                    } catch {
                        print("Failed to get artists: \(error)")
                        await MainActor.run {
                            self.isLoadingArtists = false
                        }
                    }
                }
            }
        case 1:
            if randomAlbums.isEmpty {
                refreshRandomAlbums()
            }
        case 2:
            if browseChildren.isEmpty && browseTracks.isEmpty {
                loadBrowseLevel()
            }
        default:
            break
        }
    }
    
    private func refreshRandomAlbums() {
        isLoadingRandom = true
        Task {
            do {
                let list = try await JrrDependencies.shared.libraryRepository.getRandomAlbums(limit: 20)
                await MainActor.run {
                    self.randomAlbums = list
                    self.isLoadingRandom = false
                }
            } catch {
                print("Failed to get random albums: \(error)")
                await MainActor.run {
                    self.isLoadingRandom = false
                }
            }
        }
    }
    
    private func performSearch(query: String) {
        let trimmed = query.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmed.isEmpty {
            searchResults = []
            return
        }
        isSearchingLoading = true
        Task {
            do {
                let list = try await JrrDependencies.shared.libraryRepository.searchFiles(query: trimmed)
                let mapped = list.map { $0.toTrackInfo() }
                await MainActor.run {
                    self.searchResults = mapped
                    self.isSearchingLoading = false
                }
            } catch {
                print("Failed to search tracks: \(error)")
                await MainActor.run {
                    self.isSearchingLoading = false
                }
            }
        }
    }
    
    // MARK: - Artists Tab View
    @ViewBuilder
    private func artistsTab() -> some View {
        if let artist = selectedArtist {
            VStack(spacing: 0) {
                // Back to artists list header
                Button(action: { self.selectedArtist = nil }) {
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
                
                if isLoadingArtistAlbums {
                    Spacer()
                    ProgressView().tint(.accentColor)
                    Spacer()
                } else {
                    ScrollView {
                        LazyVStack(spacing: 8) {
                            ForEach(artistAlbums, id: \.name) { album in
                                albumRowItem(album: album) {
                                    onAlbumClick(album.name, album.artist)
                                }
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.top, 8)
                    }
                }
            }
        } else {
            if isLoadingArtists {
                VStack {
                    Spacer()
                    ProgressView().tint(.accentColor)
                    Spacer()
                }
            } else {
                ScrollView {
                    LazyVStack(spacing: 8) {
                        ForEach(artists, id: \.self) { artist in
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
                                selectArtist(artist)
                            }
                        }
                    }
                    .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                    .padding(.top, 12)
                }
            }
        }
    }
    
    private func selectArtist(_ artistName: String) {
        selectedArtist = artistName
        isLoadingArtistAlbums = true
        Task {
            do {
                let albums = try await JrrDependencies.shared.libraryRepository.getAlbumsByArtist(artistName: artistName)
                await MainActor.run {
                    self.artistAlbums = albums
                    self.isLoadingArtistAlbums = false
                }
            } catch {
                print("Failed to get albums for artist: \(error)")
                await MainActor.run {
                    self.isLoadingArtistAlbums = false
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
                Text("SHUFFLED · \(randomAlbums.count) ALBUMS")
                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                    .foregroundColor(.textTertiary)
                
                Spacer()
                
                Button(action: refreshRandomAlbums) {
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
            
            if isLoadingRandom {
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
                        ForEach(randomAlbums, id: \.name) { album in
                            Button(action: { onAlbumClick(album.name, album.artist) }) {
                                VStack(alignment: .leading, spacing: 4) {
                                    ZStack {
                                        if !album.imageUrl.isEmpty, let url = URL(string: album.imageUrl) {
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
                                    
                                    Text(album.artist)
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
            if browseStack.count > 1 {
                Button(action: popBrowseLevel) {
                    HStack {
                        Image(systemName: "chevron.left")
                            .foregroundColor(.accentColor)
                            .font(.system(size: 16, weight: .bold))
                        
                        Text(browseStack.map { $0.0 }.joined(separator: " / "))
                            .font(AppFont.inter(size: 13, weight: .regular))
                            .foregroundColor(.textSecondary)
                            .lineLimit(1)
                        
                        Spacer()
                    }
                    .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                    .padding(.vertical, 12)
                }
            }
            
            if isLoadingBrowse {
                Spacer()
                ProgressView().tint(.accentColor)
                Spacer()
            } else {
                ScrollView {
                    if !browseChildren.isEmpty {
                        LazyVStack(spacing: 8) {
                            ForEach(browseChildren.sorted(by: { $0.key < $1.key }), id: \.value) { nodeLabel, nodeId in
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
                                    pushBrowseLevel(label: nodeLabel, id: nodeId)
                                }
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.top, 12)
                    } else if !browseTracks.isEmpty {
                        LazyVStack(spacing: 8) {
                            ForEach(browseTracks, id: \.fileKey) { track in
                                trackRowItem(track: track) {
                                    if let idx = browseTracks.firstIndex(of: track) {
                                        JrrDependencies.shared.facade.setQueue(tracks: browseTracks, startIndex: Int32(idx))
                                    }
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
    
    private func pushBrowseLevel(label: String, id: String) {
        browseStack.append((label, id))
        loadBrowseLevel()
    }
    
    private func popBrowseLevel() {
        if browseStack.count > 1 {
            browseStack.removeLast()
            loadBrowseLevel()
        }
    }
    
    private func loadBrowseLevel() {
        guard let current = browseStack.last else { return }
        isLoadingBrowse = true
        Task {
            do {
                let ch = try await JrrDependencies.shared.libraryRepository.getBrowseChildren(parentId: current.1)
                if !ch.isEmpty {
                    await MainActor.run {
                        self.browseChildren = ch
                        self.browseTracks = []
                        self.isLoadingBrowse = false
                    }
                } else {
                    let tracks = try await JrrDependencies.shared.libraryRepository.getBrowseFiles(nodeId: current.1)
                    let mapped = tracks.map { $0.toTrackInfo() }
                    await MainActor.run {
                        self.browseChildren = [:]
                        self.browseTracks = mapped
                        self.isLoadingBrowse = false
                    }
                }
            } catch {
                print("Failed to get browse children: \(error)")
                await MainActor.run {
                    self.isLoadingBrowse = false
                }
            }
        }
    }
    
    // MARK: - Favorites Tab View
    @ViewBuilder
    private func favoritesTab() -> some View {
        let favoritedAlbums = audioHandler.favorites.filter { $0.type == "album" }
        
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
                        // Extract artist/albumName
                        let parts = fav.identifier.split(separator: "|")
                        let artist = parts.count > 1 ? String(parts[1]) : "Unknown Artist"
                        let albumName = String(parts[0])
                        
                        HStack {
                            ZStack {
                                // Diagonal stripes artwork placeholder
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
                            onAlbumClick(albumName, artist)
                        }
                    }
                }
                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                .padding(.top, 12)
            }
        }
    }
    
    // MARK: - List Item Row Templates
    private func trackRowItem(track: TrackInfo, action: @escaping () -> Void) -> some View {
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
        HStack {
            ZStack {
                if !album.imageUrl.isEmpty, let url = URL(string: album.imageUrl) {
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
                
                Text(album.year.isEmpty ? "Unknown Year" : album.year)
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
