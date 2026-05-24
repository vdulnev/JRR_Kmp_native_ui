import SwiftUI
import SharedLogic

struct NowPlayingView: View {
    @ObservedObject var audioHandler = PlaybackStateObserver.shared
    
    let onQueueClick: () -> Void
    
    @State private var isScrubbing = false
    @State private var scrubProgress: Double = 0.0
    
    var body: some View {
        let status = audioHandler.playerStatus
        
        let isPlaying = status?.state == .playing
        let currentPosition = status?.positionMs ?? 0
        let duration = status?.durationMs ?? 0
        
        let displayProgress = isScrubbing ? scrubProgress : (duration > 0 ? Double(currentPosition) / Double(duration) : 0.0)
        
        VStack(spacing: 0) {
            // Header
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("NOW PLAYING")
                        .styleSectionLabel()
                    
                    Text(audioHandler.activeZone.name)
                        .font(AppFont.inter(size: 14, weight: .semibold))
                        .foregroundColor(.textSecondary)
                }
                
                Spacer()
                
                Button(action: onQueueClick) {
                    Image(systemName: "list.bullet")
                        .font(.system(size: 18, weight: .medium))
                        .foregroundColor(.textPrimary)
                        .frame(width: 44, height: 44)
                }
            }
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)
            .padding(.top, 16)
            
            Spacer()
            
            // Vinyl Sleeve Hero
            VinylSleeve(
                albumTitle: status?.trackAlbum ?? "No Track Selected",
                artistName: status?.trackArtist ?? "Unknown Artist",
                year: status != nil ? "2026" : "—", // Fallback decorative year
                side: "SIDE A",
                //TODO: implenent later
                imageUrl: nil,
                isPlaying: isPlaying
            )
            .padding(.vertical, 20)
            
            Spacer()
            
            // Metadata & details
            VStack(spacing: 8) {
                Text(status?.trackName ?? "Not Playing")
                    .styleNowPlayingTitle()
                    .lineLimit(1)
                    .multilineTextAlignment(.center)
                
                Text((status?.trackArtist ?? "Unknown Artist") + " — " + (status?.trackAlbum ?? "Unknown Album"))
                    .styleItemSubtitle()
                    .lineLimit(1)
                    .multilineTextAlignment(.center)
                
                // Format Badge
                if let status = status, status.sampleRate > 0 {
                    //TODO: implement later
                    //let formatString = buildFormatString(track: track)
//                    Text(formatString)
//                        .font(AppFont.ibmPlexMono(size: 9, weight: .medium))
//                        .tracking(1.6)
//                        .foregroundColor(.textTertiary)
//                        .padding(.horizontal, 8)
//                        .padding(.vertical, 4)
//                        .background(Color.bg2)
//                        .cornerRadius(4)
//                        .overlay(
//                            RoundedRectangle(cornerRadius: 4)
//                                .stroke(Color.line2, lineWidth: 1)
//                        )
                }
            }
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)
            
            Spacer()
            
            // Scrub Slider
            VStack(spacing: 6) {
                Slider(
                    value: Binding(
                        get: { displayProgress },
                        set: { newValue in
                            isScrubbing = true
                            scrubProgress = newValue
                        }
                    ),
                    in: 0.0...1.0,
                    onEditingChanged: { editing in
                        if !editing {
                            isScrubbing = false
                            if duration > 0 {
                                JrrDependencies.shared.facade.seekTo(positionMs: Int64(scrubProgress * Double(duration)))
                            }
                        }
                    }
                )
                .tint(.accentColor)
                
                HStack {
                    let currentSecs = isScrubbing ? Int64(scrubProgress * Double(duration) / 1000) : currentPosition / 1000
                    let totalSecs = duration / 1000
                    
                    Text(formatTime(seconds: currentSecs))
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .foregroundColor(.textSecondary)
                    
                    Spacer()
                    
                    Text("-" + formatTime(seconds: max(0, totalSecs - currentSecs)))
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .foregroundColor(.textSecondary)
                }
            }
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)
            
            Spacer()
            
            // Transport Controls Row
            HStack(spacing: 0) {
                // Shuffle Button
                Button(action: {
                    let currentMode = status?.shuffleMode ?? .off
                    let nextMode: ShuffleMode = {
                        switch currentMode {
                        case .off: return .on
                        case .on: return .automatic
                        case .automatic: return .off
                        default: return .off
                        }
                    }()
                    JrrDependencies.shared.facade.setShuffleMode(mode: nextMode)
                }) {
                    VStack(spacing: 2) {
                        Image(systemName: "shuffle")
                            .font(.system(size: 18))
                        if status?.shuffleMode == .automatic {
                            Text("AUTO")
                                .font(AppFont.ibmPlexMono(size: 8, weight: .bold))
                        }
                    }
                    .foregroundColor(status?.shuffleMode != .off ? .accentColor : .textTertiary)
                    .frame(width: 44, height: 44)
                }
                
                Spacer()
                
                // Previous Button
                Button(action: { JrrDependencies.shared.facade.previous() }) {
                    Image(systemName: "backward.end.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.textPrimary)
                        .frame(width: 44, height: 44)
                }
                
                Spacer()
                
                // Play / Pause Circle Disc
                Button(action: {
                    if isPlaying {
                        JrrDependencies.shared.facade.pause()
                    } else {
                        JrrDependencies.shared.facade.play()
                    }
                }) {
                    ZStack {
                        Circle()
                            .fill(Color.accentColor)
                            .frame(width: 60, height: 60)
                            .shadow(color: Color.accentColor.opacity(0.45), radius: 24, x: 0, y: 8)
                        
                        Image(systemName: isPlaying ? "pause.fill" : "play.fill")
                            .font(.system(size: 24))
                            .foregroundColor(.bg0)
                            .offset(x: isPlaying ? 0 : 2) // optical centering
                    }
                }
                
                Spacer()
                
                // Next Button
                Button(action: { JrrDependencies.shared.facade.next() }) {
                    Image(systemName: "forward.end.fill")
                        .font(.system(size: 24))
                        .foregroundColor(.textPrimary)
                        .frame(width: 44, height: 44)
                }
                
                Spacer()
                
                // Repeat Button
                Button(action: {
                    let currentMode = status?.repeatMode ?? .off
                    let nextMode: RepeatMode = {
                        switch currentMode {
                        case .off: return .playlist
                        case .playlist: return .track
                        case .track: return .off
                        default: return .off
                        }
                    }()
                    JrrDependencies.shared.facade.setRepeatMode(mode: nextMode)
                }) {
                    Image(systemName: status?.repeatMode == .track ? "repeat.1" : "repeat")
                        .font(.system(size: 18))
                        .foregroundColor(status?.repeatMode != .off ? .accentColor : .textTertiary)
                        .frame(width: 44, height: 44)
                }
            }
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)
            
            Spacer()
            
            // Volume Slider Row
            HStack(spacing: 12) {
                let volume = status?.volume ?? 0.5
                Image(systemName: volume == 0 ? "speaker.slash.fill" : "speaker.wave.2.fill")
                    .font(.system(size: 14))
                    .foregroundColor(.textTertiary)
                    .frame(width: 20)
                
                Slider(
                    value: Binding(
                        get: { Double(volume) },
                        set: { JrrDependencies.shared.facade.setVolume(level: Float($0)) }
                    ),
                    in: 0.0...1.0
                )
                .tint(.textSecondary)
                
                Text("\(Int(volume * 100))")
                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                    .foregroundColor(.textTertiary)
                    .frame(width: 24, alignment: .trailing)
            }
            .padding(.horizontal, AppSpacing.nowPlayingHorizontalMargin)
            .padding(.bottom, 24)
        }
        .background(Color.bg1.ignoresSafeArea())
    }
    
    private func formatTime(seconds: Int64) -> String {
        let m = seconds / 60
        let s = seconds % 60
        return String(format: "%d:%02d", m, s)
    }
    
    private func buildFormatString(track: Track) -> String {
        var parts: [String] = []
        // Try to guess from imageUrl query parameter or just use default
        if let url = URL(string: track.imageUrl),
           let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
           let formatItem = components.queryItems?.first(where: { $0.name == "Format" })?.value {
            parts.append(formatItem.uppercased())
        } else {
            parts.append("AUDIO")
        }
        
        if track.bitDepth > 0 {
            parts.append("\(track.bitDepth)-BIT")
        }
        if track.sampleRate > 0 {
            parts.append("\(track.sampleRate / 1000)KHZ")
        }
        if track.bitrate > 0 {
            parts.append("\(track.bitrate)KBPS")
        }
        
        return parts.joined(separator: " | ")
    }
}
