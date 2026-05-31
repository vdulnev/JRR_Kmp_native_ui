import SharedLogic
import SwiftUI

/// Large-screen (iPad regular width) chrome: a persistent left sidebar (brand,
/// nav, docked now-playing cell, active-zone chip) beside the routed `content`.
/// Mirrors the `Tablet & Desktop` design handoff. Navigation stays driven by
/// `RootConfig`; this view is presentation only.
struct LargeScreenShell<Content: View>: View {
    let activeTag: Int
    let onSelect: (Int) -> Void
    var nowPlaying: NowPlayingObservable
    @ViewBuilder let content: () -> Content

    /// Holds keyboard focus so the shell receives hardware-keyboard shortcuts.
    /// A focused text field (the library filter) takes focus on tap and
    /// consumes character keys first, so typing is never hijacked.
    @FocusState private var keyboardFocused: Bool

    private static var sidebarWidth: CGFloat {
        256
    }

    var body: some View {
        HStack(spacing: 0) {
            Sidebar(activeTag: activeTag, onSelect: onSelect, nowPlaying: nowPlaying)
                .frame(width: Self.sidebarWidth)
            Rectangle().fill(Color.line).frame(width: 1).ignoresSafeArea()
            content()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.bg1)
        }
        .background(Color.bg1)
        .focusable()
        .focused($keyboardFocused)
        .onAppear { keyboardFocused = true }
        .onKeyPress { press in handleKey(press) }
    }

    private func handleKey(_ press: KeyPress) -> KeyPress.Result {
        switch press.key {
        case .space:
            if nowPlaying.isPlaying { nowPlaying.pause() } else { nowPlaying.play() }
            return .handled
        case .leftArrow:
            nowPlaying.previous(); return .handled
        case .rightArrow:
            nowPlaying.next(); return .handled
        case .upArrow:
            nowPlaying.setVolume(level: min(1, nowPlaying.volume + 0.05)); return .handled
        case .downArrow:
            nowPlaying.setVolume(level: max(0, nowPlaying.volume - 0.05)); return .handled
        case KeyEquivalent("q"):
            onSelect(2); return .handled
        case KeyEquivalent("l"):
            onSelect(0); return .handled
        default:
            return .ignored
        }
    }
}

private struct Sidebar: View {
    let activeTag: Int
    let onSelect: (Int) -> Void
    var nowPlaying: NowPlayingObservable

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Brand
            HStack(spacing: 12) {
                Circle().fill(Color.accentColor).frame(width: 34, height: 34)
                VStack(alignment: .leading, spacing: 2) {
                    Text("JRIVER")
                        .font(AppFont.ibmPlexMono(size: 12.5, weight: .medium))
                        .tracking(2)
                        .foregroundColor(.textPrimary)
                    Text("REMOTE")
                        .font(AppFont.ibmPlexMono(size: 9, weight: .regular))
                        .tracking(2)
                        .foregroundColor(.accentColor)
                }
            }
            .padding(.horizontal, 22)
            .padding(.top, 22)
            .padding(.bottom, 18)

            // Nav
            VStack(spacing: 2) {
                NavItem(label: "Now Playing", systemImage: "play.circle.fill", selected: activeTag == 2) { onSelect(2) }
                NavItem(label: "Library", systemImage: "music.note.house.fill", selected: activeTag == 0) { onSelect(0) }
                NavItem(label: "Zones", systemImage: "speaker.wave.3.fill", selected: activeTag == 3) { onSelect(3) }
                NavItem(label: "Settings", systemImage: "gearshape.fill", selected: activeTag == 4) { onSelect(4) }
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 8)

            Spacer()

            if nowPlaying.trackTitle != "Idle" {
                DockedNowPlaying(nowPlaying: nowPlaying, onBodyClick: { onSelect(2) })
                    .padding(12)
            }

            // Active zone chip
            Button(action: { onSelect(3) }) {
                HStack(spacing: 10) {
                    Circle().fill(Color.successColor).frame(width: 7, height: 7)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("ACTIVE ZONE")
                            .font(AppFont.ibmPlexMono(size: 9, weight: .regular))
                            .tracking(1.6)
                            .foregroundColor(.textTertiary)
                        Text(nowPlaying.activeZoneName)
                            .font(AppFont.inter(size: 13, weight: .medium))
                            .foregroundColor(.textPrimary)
                            .lineLimit(1)
                    }
                    Spacer()
                }
                .padding(.horizontal, 14)
                .padding(.vertical, 11)
                .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.line, lineWidth: 1))
            }
            .buttonStyle(.plain)
            .padding(.horizontal, 12)
            .padding(.bottom, 14)
        }
        .frame(maxHeight: .infinity, alignment: .top)
        .background(Color.bg2)
    }
}

private struct NavItem: View {
    let label: String
    let systemImage: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Image(systemName: systemImage)
                    .font(.system(size: 18))
                    .frame(width: 20, height: 20)
                Text(label)
                    .font(AppFont.inter(size: 14.5, weight: .medium))
                    .lineLimit(1)
                Spacer()
            }
            .foregroundColor(selected ? .accentColor : .textTertiary)
            .padding(.horizontal, 14)
            .frame(height: 46)
            .background(
                RoundedRectangle(cornerRadius: 10)
                    .fill(selected ? Color.accentColor.opacity(0.13) : Color.clear),
            )
        }
        .buttonStyle(.plain)
    }
}

private struct DockedNowPlaying: View {
    var nowPlaying: NowPlayingObservable
    let onBodyClick: () -> Void

    private var progress: Double {
        nowPlaying.durationMs > 0 ? Double(nowPlaying.positionMs) / Double(nowPlaying.durationMs) : 0
    }

    var body: some View {
        VStack(spacing: 0) {
            // Progress hairline
            GeometryReader { geo in
                Rectangle()
                    .fill(Color.accentColor)
                    .frame(width: geo.size.width * CGFloat(min(max(progress, 0), 1)), height: 2)
            }
            .frame(height: 2)

            VStack(spacing: 12) {
                HStack(spacing: 10) {
                    JrrAsyncImage(
                        url: nowPlaying.imageUrl.isEmpty ? nil : URL(string: nowPlaying.imageUrl),
                        content: { image in image.resizable().aspectRatio(contentMode: .fill) },
                        placeholder: { Color.bg4 },
                    )
                    .frame(width: 44, height: 44)
                    .clipShape(RoundedRectangle(cornerRadius: 4))

                    VStack(alignment: .leading, spacing: 2) {
                        Text(nowPlaying.trackTitle)
                            .font(AppFont.inter(size: 13, weight: .medium))
                            .foregroundColor(.textPrimary)
                            .lineLimit(1)
                        Text(nowPlaying.artistName)
                            .font(AppFont.inter(size: 11.5, weight: .regular))
                            .foregroundColor(.textSecondary)
                            .lineLimit(1)
                    }
                    Spacer()
                }

                HStack {
                    VuMeter(isPlaying: nowPlaying.isPlaying)
                    Spacer()
                    HStack(spacing: 4) {
                        ctlButton("backward.fill", color: .textSecondary, size: 16) { nowPlaying.previous() }
                        ctlButton(nowPlaying.isPlaying ? "pause.fill" : "play.fill", color: .accentColor, size: 18) {
                            if nowPlaying.isPlaying { nowPlaying.pause() } else { nowPlaying.play() }
                        }
                        ctlButton("forward.fill", color: .textSecondary, size: 16) { nowPlaying.next() }
                    }
                }
            }
            .padding(12)
        }
        .background(Color.bg3)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.line2, lineWidth: 1))
        .contentShape(Rectangle())
        .onTapGesture(perform: onBodyClick)
    }

    private func ctlButton(_ systemName: String, color: Color, size: CGFloat, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: systemName)
                .font(.system(size: size))
                .foregroundColor(color)
                .frame(width: 32, height: 32)
        }
        .buttonStyle(.plain)
    }
}
