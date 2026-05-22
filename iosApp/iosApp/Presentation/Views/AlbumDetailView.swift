import SwiftUI
import SharedLogic

struct AlbumDetailView: View {
    @ObservedObject var stateObserver = PlaybackStateObserver.shared
    
    let albumName: String
    let artistName: String
    let onBackClick: () -> Void
    
    @State private var tracks: [Track] = []
    @State private var isLoading = true
    
    var isFavorite: Bool {
        stateObserver.favorites.contains(where: { $0.type == "album" && $0.identifier == "\(albumName)|\(artistName)" })
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
                
                Button(action: toggleFavorite) {
                    Image(systemName: isFavorite ? "star.fill" : "star")
                        .font(.system(size: 18))
                        .foregroundColor(isFavorite ? .accentColor : .textTertiary)
                        .frame(width: 44, height: 44)
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .background(Color.bg1)
            
            if isLoading {
                Spacer()
                ProgressView()
                    .tint(.accentColor)
                Spacer()
            } else {
                ScrollView {
                    VStack(spacing: 20) {
                        // Artwork and details header
                        VStack(alignment: .center, spacing: 16) {
                            // 2D artwork
                            ZStack {
                                if let artworkUrl = tracks.first(where: { !$0.imageUrl.isEmpty })?.imageUrl,
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
                                Text(albumName)
                                    .font(AppFont.inter(size: 20, weight: .bold))
                                    .foregroundColor(.textPrimary)
                                    .lineLimit(2)
                                    .multilineTextAlignment(.center)
                                
                                Text(artistName)
                                    .font(AppFont.inter(size: 13, weight: .regular))
                                    .foregroundColor(.textSecondary)
                                    .lineLimit(1)
                                    .multilineTextAlignment(.center)
                            }
                            .padding(.horizontal, 8)
                            
                            // Play / Shuffle Buttons
                            HStack(spacing: 12) {
                                Button(action: {
                                    if !tracks.isEmpty {
                                        let trackInfos = tracks.map { $0.toTrackInfo() }
                                        JrrDependencies.shared.facade.setQueue(tracks: trackInfos, startIndex: 0)
                                    }
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
                                    if !tracks.isEmpty {
                                        let shuffled = tracks.shuffled()
                                        let trackInfos = shuffled.map { $0.toTrackInfo() }
                                        JrrDependencies.shared.facade.setQueue(tracks: trackInfos, startIndex: 0)
                                    }
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
                            let sortedTracks = tracks.sorted { ($0.discNumber, $0.trackNumber) < ($1.discNumber, $1.trackNumber) }
                            let discGroups = Dictionary(grouping: sortedTracks, by: { $0.discNumber })
                            let sortedDiscKeys = discGroups.keys.sorted()
                            
                            ForEach(sortedDiscKeys, id: \.self) { discNum in
                                let discTracks = discGroups[discNum] ?? []
                                
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
                                    
                                    HStack {
                                        Text(String(format: "%02d", displayIdx))
                                            .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                                            .foregroundColor(.accentColor)
                                            .frame(width: 36, alignment: .leading)
                                        
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(track.name)
                                                .styleItemTitle()
                                                .lineLimit(1)
                                            
                                            if track.artist != artistName {
                                                Text(track.artist)
                                                    .styleItemSubtitle()
                                                    .lineLimit(1)
                                            }
                                        }
                                        
                                        Spacer()
                                        
                                        // Download status / action button
                                        if !stateObserver.activeZone.isOffline {
                                            if stateObserver.downloadedTracks.contains(where: { $0.fileKey == track.fileKey }) {
                                                Image(systemName: "checkmark")
                                                    .font(.system(size: 11, weight: .bold))
                                                    .foregroundColor(.accentColor)
                                                    .padding(.trailing, 8)
                                            } else if let job = stateObserver.downloadJobs.first(where: { $0.fileKey == track.fileKey }) {
                                                if job.state == "downloading" || job.state == "DOWNLOADING" {
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
                                                    triggerDownload(track: track)
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
                                        let index = sortedTracks.firstIndex(of: track) ?? 0
                                        let trackInfos = sortedTracks.map { $0.toTrackInfo() }
                                        JrrDependencies.shared.facade.setQueue(tracks: trackInfos, startIndex: Int32(index))
                                    }
                                    
                                    Divider()
                                        .background(Color.line)
                                }
                            }
                        }
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                    }
                    .padding(.bottom, 30)
                }
            }
        }
        .background(Color.bg1.ignoresSafeArea())
        .task {
            await loadTracks()
        }
    }
    
    private func loadTracks() async {
        isLoading = true
        do {
            let list = try await JrrDependencies.shared.libraryRepository.getAlbumTracks(albumName: albumName, artistName: artistName)
            await MainActor.run {
                self.tracks = list
                self.isLoading = false
            }
        } catch {
            print("Failed to load album tracks: \(error)")
            await MainActor.run {
                self.isLoading = false
            }
        }
    }
    
    private func triggerDownload(track: Track) {
        Task {
            do {
                _ = try await JrrDependencies.shared.libraryRepository.startDownload(track: track)
            } catch {
                print("Failed to start download: \(error)")
            }
        }
    }
    
    private func toggleFavorite() {
        Task {
            do {
                let db = JrrDependencies.shared.database
                let identifier = "\(albumName)|\(artistName)"
                if let existing = try await db.favoriteDao().getFavorite(type: "album", identifier: identifier) {
                    try await db.favoriteDao().delete(favorite: existing)
                } else {
                    let newFav = FavoriteEntity(
                        id: 0,
                        type: "album",
                        identifier: identifier,
                        displayName: albumName,
                        addedAt: Int64(Date().timeIntervalSince1970 * 1000)
                    )
                    try await db.favoriteDao().insert(favorite: newFav)
                }
                PlaybackStateObserver.shared.refreshFavorites()
            } catch {
                print("Failed to toggle favorite: \(error)")
            }
        }
    }
}
