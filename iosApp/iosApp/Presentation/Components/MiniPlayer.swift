import SwiftUI

struct MiniPlayer: View {
    let title: String
    let artist: String
    let imageUrl: String?
    let isPlaying: Bool
    let progress: Double // value between 0.0 and 1.0
    let onPlayPauseClick: () -> Void
    let onNextClick: () -> Void
    let onPrevClick: () -> Void
    let onBodyClick: () -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            // 1. Flush Top-edge 2px gold progress indicator
            GeometryReader { geometry in
                let width = geometry.size.width * CGFloat(min(max(progress, 0.0), 1.0))
                Color.accentColor
                    .frame(width: width, height: 2)
            }
            .frame(height: 2)
            
            // 2. Mini-Player Body
            HStack(spacing: 10) {
                // 38dp Artwork preview
                ZStack {
                    if let imageUrl = imageUrl, let url = URL(string: imageUrl) {
                        JrrAsyncImage(url: url) { image in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color.bg2
                        }
                    } else {
                        // Custom fallback placeholder (gold/blue diagonal stripe style)
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
                .frame(width: 38, height: 38)
                .cornerRadius(4)
                .overlay(
                    RoundedRectangle(cornerRadius: 4)
                        .stroke(Color.line2, lineWidth: 1)
                )
                
                // Track metadata
                VStack(alignment: .leading, spacing: 2) {
                    Text(title)
                        .font(AppFont.inter(size: 13.5, weight: .medium))
                        .foregroundColor(.textPrimary)
                        .lineLimit(1)
                    
                    Text(artist)
                        .font(AppFont.inter(size: 11.5, weight: .regular))
                        .foregroundColor(.textSecondary)
                        .lineLimit(1)
                }
                
                Spacer()
                
                // Transport Row Controls: Prev -> Play Disc -> Next
                HStack(spacing: 6) {
                    // Previous Button
                    Button(action: onPrevClick) {
                        Canvas { context, size in
                            // Vertical bar
                            var pathBar = Path()
                            pathBar.move(to: CGPoint(x: size.width * 0.2, y: size.height * 0.2))
                            pathBar.addLine(to: CGPoint(x: size.width * 0.2, y: size.height * 0.8))
                            context.stroke(pathBar, with: .color(.textSecondary), style: StrokeStyle(lineWidth: 1.5))
                            
                            // Left-pointing triangle outline
                            var pathTri = Path()
                            pathTri.move(to: CGPoint(x: size.width * 0.8, y: size.height * 0.2))
                            pathTri.addLine(to: CGPoint(x: size.width * 0.35, y: size.height * 0.5))
                            pathTri.addLine(to: CGPoint(x: size.width * 0.8, y: size.height * 0.8))
                            pathTri.closeSubpath()
                            context.stroke(pathTri, with: .color(.textSecondary), style: StrokeStyle(lineWidth: 1.5))
                        }
                        .frame(width: 16, height: 16)
                    }
                    .frame(width: 36, height: 36)
                    
                    // Play / Pause gold disc
                    Button(action: onPlayPauseClick) {
                        ZStack {
                            Circle()
                                .fill(Color.accentColor)
                                .frame(width: 32, height: 32)
                                .shadow(color: Color.accentColor.opacity(0.45), radius: 8, x: 0, y: 0)
                            
                            Canvas { context, size in
                                if isPlaying {
                                    let barWidth = size.width * 0.2
                                    let barHeight = size.height * 0.6
                                    let rect1 = CGRect(x: size.width * 0.25, y: size.height * 0.2, width: barWidth, height: barHeight)
                                    let rect2 = CGRect(x: size.width * 0.55, y: size.height * 0.2, width: barWidth, height: barHeight)
                                    context.fill(Path(rect1), with: .color(.bg0))
                                    context.fill(Path(rect2), with: .color(.bg0))
                                } else {
                                    var path = Path()
                                    path.move(to: CGPoint(x: size.width * 0.3, y: size.height * 0.2))
                                    path.addLine(to: CGPoint(x: size.width * 0.8, y: size.height * 0.5))
                                    path.addLine(to: CGPoint(x: size.width * 0.3, y: size.height * 0.8))
                                    path.closeSubpath()
                                    context.fill(path, with: .color(.bg0))
                                }
                            }
                            .frame(width: 14, height: 14)
                        }
                    }
                    .frame(width: 32, height: 32)
                    
                    // Next Button
                    Button(action: onNextClick) {
                        Canvas { context, size in
                            // Right-pointing triangle outline
                            var pathTri = Path()
                            pathTri.move(to: CGPoint(x: size.width * 0.2, y: size.height * 0.2))
                            pathTri.addLine(to: CGPoint(x: size.width * 0.65, y: size.height * 0.5))
                            pathTri.addLine(to: CGPoint(x: size.width * 0.2, y: size.height * 0.8))
                            pathTri.closeSubpath()
                            context.stroke(pathTri, with: .color(.textSecondary), style: StrokeStyle(lineWidth: 1.5))
                            
                            // Vertical bar
                            var pathBar = Path()
                            pathBar.move(to: CGPoint(x: size.width * 0.8, y: size.height * 0.2))
                            pathBar.addLine(to: CGPoint(x: size.width * 0.8, y: size.height * 0.8))
                            context.stroke(pathBar, with: .color(.textSecondary), style: StrokeStyle(lineWidth: 1.5))
                        }
                        .frame(width: 16, height: 16)
                    }
                    .frame(width: 36, height: 36)
                }
            }
            .padding(.horizontal, 12)
            .frame(maxHeight: .infinity)
        }
        .frame(height: 64)
        .background(Color.bg3)
        .cornerRadius(12)
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.line2, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.55), radius: 16, x: 0, y: 8)
        .onTapGesture(perform: onBodyClick)
    }
}

#Preview {
    ZStack {
        Color.bg0.ignoresSafeArea()
        MiniPlayer(
            title: "Comfortably Numb",
            artist: "Pink Floyd",
            imageUrl: nil,
            isPlaying: false,
            progress: 0.45,
            onPlayPauseClick: {},
            onNextClick: {},
            onPrevClick: {},
            onBodyClick: {}
        )
        .padding(.horizontal, 16)
    }
}
