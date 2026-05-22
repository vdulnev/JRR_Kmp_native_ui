import SwiftUI

struct VuMeter: View {
    let isPlaying: Bool
    
    @State private var animate = false
    
    // Base heights: 7, 11, 5, 13, 8
    private let baseHeights: [CGFloat] = [7, 11, 5, 13, 8]
    
    // Animation durations for each bar: 180ms, 220ms, 150ms, 250ms, 200ms
    private let durations: [Double] = [0.18, 0.22, 0.15, 0.25, 0.20]
    
    // Scale ranges for each bar
    private let initialScales: [CGFloat] = [0.3, 0.2, 0.4, 0.1, 0.3]
    
    var body: some View {
        HStack(alignment: .bottom, spacing: 2) {
            ForEach(0..<5) { index in
                BarView(
                    baseHeight: baseHeights[index],
                    duration: durations[index],
                    initialScale: initialScales[index],
                    isPlaying: isPlaying,
                    animate: animate
                )
            }
        }
        .frame(width: 18, height: 14)
        .onAppear {
            animate = isPlaying
        }
        .onChange(of: isPlaying) { oldVal, newVal in
            animate = newVal
        }
    }
    
    struct BarView: View {
        let baseHeight: CGFloat
        let duration: Double
        let initialScale: CGFloat
        let isPlaying: Bool
        let animate: Bool
        
        @State private var scale: CGFloat = 1.0
        
        var body: some View {
            RoundedRectangle(cornerRadius: 1)
                .fill(Color.accentColor)
                .frame(width: 2, height: isPlaying ? baseHeight * scale : 2)
                .onAppear {
                    scale = initialScale
                    triggerAnimation()
                }
                .onChange(of: animate) { oldVal, newVal in
                    triggerAnimation()
                }
        }
        
        private func triggerAnimation() {
            if animate {
                withAnimation(Animation.linear(duration: duration).repeatForever(autoreverses: true)) {
                    scale = 1.0
                }
            } else {
                withAnimation(.easeInOut(duration: 0.15)) {
                    scale = initialScale
                }
            }
        }
    }
}

#Preview {
    ZStack {
        Color.bg0.ignoresSafeArea()
        VuMeter(isPlaying: true)
    }
}
