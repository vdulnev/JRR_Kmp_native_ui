import SwiftUI

struct VinylSleeve: View {
    let albumTitle: String
    let artistName: String
    let year: String
    var side: String = "SIDE A"
    let imageUrl: String?
    let isPlaying: Bool

    var body: some View {
        ZStack {
            // 1. Underglow: 220x220 radial gold gradient, blurred
            RadialGradient(
                colors: [Color.accentColor.opacity(0.25), Color.clear],
                center: .center,
                startRadius: 0,
                endRadius: 110,
            )
            .frame(width: 220, height: 220)
            .blur(radius: 32)

            // Wrapper that applies the 3D perspective transforms
            ZStack(alignment: .leading) {
                // 2. LP record disc: 248x248, offset to the right when playing
                Canvas { context, size in
                    let rect = CGRect(origin: .zero, size: size)
                    let center = CGPoint(x: size.width / 2, y: size.height / 2)
                    let outerRadius = size.width / 2

                    // Draw the black matte vinyl disc body
                    context.fill(Path(ellipseIn: rect), with: .color(Color(hex: 0x0F0F11)))

                    // Draw concentric vinyl grooves (concentric circles)
                    var grooveRadius = outerRadius - 12 // 12pt
                    let minGrooveRadius: CGFloat = 42
                    let grooveSpacing: CGFloat = 6

                    while grooveRadius > minGrooveRadius {
                        let grooveRect = CGRect(
                            x: center.x - grooveRadius,
                            y: center.y - grooveRadius,
                            width: grooveRadius * 2,
                            height: grooveRadius * 2,
                        )
                        context.stroke(
                            Path(ellipseIn: grooveRect),
                            with: .color(Color(hex: 0x1E1E21)),
                            style: StrokeStyle(lineWidth: 1),
                        )
                        grooveRadius -= grooveSpacing
                    }

                    // Draw the center 72pt gold label disc (radius = 36pt)
                    let labelRect = CGRect(
                        x: center.x - 36,
                        y: center.y - 36,
                        width: 72,
                        height: 72,
                    )
                    context.fill(Path(ellipseIn: labelRect), with: .color(.accentColor))

                    // Draw center hole 6pt (radius = 3pt) in deepest background color bg0
                    let holeRect = CGRect(
                        x: center.x - 3,
                        y: center.y - 3,
                        width: 6,
                        height: 6,
                    )
                    context.fill(Path(ellipseIn: holeRect), with: .color(.bg0))
                }
                .frame(width: 248, height: 248)
                .offset(x: isPlaying ? 64 : 0)
                .animation(.easeInOut(duration: 0.5), value: isPlaying)

                // 3. Sleeve: 256x256, 4pt radius, dark gradient + repeating 135° stripe
                ZStack {
                    // Background artwork or gradient
                    if let imageUrl, let url = URL(string: imageUrl) {
                        JrrAsyncImage(url: url) { image in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        } placeholder: {
                            LinearGradient(
                                colors: [.bg2, .bg0],
                                startPoint: .top,
                                endPoint: .bottom,
                            )
                        }
                    } else {
                        LinearGradient(
                            colors: [.bg2, .bg0],
                            startPoint: .top,
                            endPoint: .bottom,
                        )
                    }

                    // Sleeve decoration (stripes & inset border)
                    Canvas { context, size in
                        // Draw 135-degree repeating gold stripe at 6% opacity
                        let step: CGFloat = 20
                        let lineCount = Int((size.width + size.height) / step)
                        for i in 0 ... lineCount {
                            let offset = CGFloat(i) * step
                            var path = Path()
                            path.move(to: CGPoint(x: offset, y: 0))
                            path.addLine(to: CGPoint(x: offset - size.height, y: size.height))
                            context.stroke(
                                path,
                                with: .color(Color.accentColor.opacity(0.06)),
                                style: StrokeStyle(lineWidth: 2),
                            )
                        }

                        // 1-px inset border at 18px (which is 6pt)
                        let inset: CGFloat = 6
                        let insetRect = CGRect(
                            x: inset,
                            y: inset,
                            width: size.width - 2 * inset,
                            height: size.height - 2 * inset,
                        )
                        context.stroke(
                            Path(insetRect),
                            with: .color(.accentSoft),
                            style: StrokeStyle(lineWidth: 1),
                        )
                    }

                    // Overlaid text content inside the sleeve
                    VStack(alignment: .leading) {
                        // Top-Left metadata: "SIDE A · YEAR"
                        HStack {
                            Text("\(side) · \(year)".uppercased())
                                .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                                .tracking(0.2)
                                .foregroundColor(.accentColor)
                                .monospacedDigit()
                            Spacer()
                        }

                        Spacer()

                        // Bottom-Left Titles
                        VStack(alignment: .leading, spacing: 4) {
                            Text(albumTitle.uppercased())
                                .font(AppFont.inter(size: 22, weight: .bold))
                                .tracking(-0.5)
                                .foregroundColor(.textPrimary)
                                .lineLimit(2)

                            Text(artistName.uppercased())
                                .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                                .tracking(1.6)
                                .foregroundColor(.textSecondary)
                                .lineLimit(1)
                        }
                    }
                    .padding(18)
                }
                .frame(width: 256, height: 256)
                .background(Color.bg3) // Fallback backplate
                .cornerRadius(4)
            }
            .frame(width: 256, height: 256)
            .rotation3DEffect(.degrees(8), axis: (x: 1, y: 0, z: 0))
            .rotation3DEffect(.degrees(-14), axis: (x: 0, y: 1, z: 0))
            .rotation3DEffect(.degrees(-3), axis: (x: 0, y: 0, z: 1))
        }
        .frame(width: 260, height: 260)
    }
}

#Preview {
    ZStack {
        Color.bg0.ignoresSafeArea()
        VinylSleeve(
            albumTitle: "Dark Side of the Moon",
            artistName: "Pink Floyd",
            year: "1973",
            imageUrl: nil,
            isPlaying: true,
        )
    }
}
