import SwiftUI

// MARK: - Color Tokens

extension Color {
    static let bg0 = Color(hex: 0x080809)
    static let bg1 = Color(hex: 0x0E0E10)
    static let bg2 = Color(hex: 0x161618)
    static let bg3 = Color(hex: 0x1E1E21)
    static let bg4 = Color(hex: 0x26262A)
    static let line = Color(.sRGB, red: 1.0, green: 1.0, blue: 1.0, opacity: 0.06)
    static let line2 = Color(.sRGB, red: 1.0, green: 1.0, blue: 1.0, opacity: 0.10)
    static let textPrimary = Color(hex: 0xF0EDE8)
    static let textSecondary = Color(.sRGB, red: 240 / 255, green: 237 / 255, blue: 232 / 255, opacity: 0.55)
    static let textTertiary = Color(.sRGB, red: 240 / 255, green: 237 / 255, blue: 232 / 255, opacity: 0.30)
    static let accentColor = Color(hex: 0xC8922A)
    static let accentDim = Color(hex: 0xC8922A).opacity(0.13)
    static let accentSoft = Color(hex: 0xC8922A).opacity(0.32)
    static let errorColor = Color(hex: 0xE5484D)
    static let successColor = Color(hex: 0x5BCE8A)

    /// Hex initializer
    init(hex: UInt32, alpha: Double = 1.0) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(.sRGB, red: r, green: g, blue: b, opacity: alpha)
    }
}

// MARK: - Typography & Fonts

enum AppFont {
    static func inter(size: CGFloat, weight: Font.Weight) -> Font {
        let systemFont = Font.system(size: size, weight: weight)
        let fontName = switch weight {
        case .bold: "Inter-Bold"
        case .semibold: "Inter-SemiBold"
        case .medium: "Inter-Medium"
        default: "Inter-Regular"
        }
        return Font.custom(fontName, size: size).fallback(to: systemFont)
    }

    static func ibmPlexMono(size: CGFloat, weight: Font.Weight) -> Font {
        let systemFont = Font.system(size: size, weight: weight, design: .monospaced)
        let fontName = switch weight {
        case .medium: "IBMPlexMono-Medium"
        default: "IBMPlexMono-Regular"
        }
        return Font.custom(fontName, size: size).fallback(to: systemFont)
    }
}

extension Font {
    func fallback(to _: Font) -> Font {
        // SwiftUI handles custom font fallbacks gracefully. Returning self.
        self
    }
}

// MARK: - Typography Style Tokens

struct ScreenTitleModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.inter(size: 22, weight: .bold))
            .tracking(-0.5)
            .foregroundColor(.textPrimary)
    }
}

struct SubScreenTitleModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.inter(size: 20, weight: .bold))
            .tracking(-0.4)
            .foregroundColor(.textPrimary)
    }
}

struct NowPlayingTitleModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.inter(size: 18, weight: .bold))
            .tracking(-0.4)
            .foregroundColor(.textPrimary)
    }
}

struct ItemTitleModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.inter(size: 16, weight: .medium))
            .tracking(-0.2)
            .foregroundColor(.textPrimary)
    }
}

struct ItemSubtitleModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.inter(size: 13, weight: .regular))
            .tracking(0)
            .foregroundColor(.textSecondary)
    }
}

struct LabelLargeModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.inter(size: 14, weight: .medium))
            .tracking(0.1)
            .foregroundColor(.textPrimary)
    }
}

struct SectionLabelModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
            .tracking(2.5)
            .foregroundColor(.accentColor)
    }
}

struct SectionHeadingModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
            .tracking(1.6)
            .foregroundColor(.textTertiary)
    }
}

struct MonoLabelModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
            .tracking(0.2)
            .foregroundColor(.textTertiary)
            .monospacedDigit()
    }
}

struct ChipMonoModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .font(AppFont.ibmPlexMono(size: 10, weight: .medium))
            .tracking(1.6)
            .foregroundColor(.textPrimary)
    }
}

extension View {
    func styleScreenTitle() -> some View {
        modifier(ScreenTitleModifier())
    }

    func styleSubScreenTitle() -> some View {
        modifier(SubScreenTitleModifier())
    }

    func styleNowPlayingTitle() -> some View {
        modifier(NowPlayingTitleModifier())
    }

    func styleItemTitle() -> some View {
        modifier(ItemTitleModifier())
    }

    func styleItemSubtitle() -> some View {
        modifier(ItemSubtitleModifier())
    }

    func styleLabelLarge() -> some View {
        modifier(LabelLargeModifier())
    }

    func styleSectionLabel() -> some View {
        modifier(SectionLabelModifier())
    }

    func styleSectionHeading() -> some View {
        modifier(SectionHeadingModifier())
    }

    func styleMonoLabel() -> some View {
        modifier(MonoLabelModifier())
    }

    func styleChipMono() -> some View {
        modifier(ChipMonoModifier())
    }
}

// MARK: - Spacing & Layout Tokens

enum AppSpacing {
    static let screenHorizontalMargin: CGFloat = 20
    static let nowPlayingHorizontalMargin: CGFloat = 24
    static let rowVerticalPadding: CGFloat = 12
    static let headerVerticalPaddingTop: CGFloat = 14
    static let headerVerticalPaddingBottom: CGFloat = 16
    static let cardPadding: CGFloat = 16

    // Radii
    static let radiusList: CGFloat = 10
    static let radiusArt: CGFloat = 4
    static let radiusPill: CGFloat = 999

    /// Hairline
    static let hairline: CGFloat = 1.0
}
