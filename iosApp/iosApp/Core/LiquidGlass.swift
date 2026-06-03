import SwiftUI

// MARK: - Liquid Glass support (iOS 26 / macOS 26) with graceful back-deployment

//
// The app's deployment floor is macOS 14 / iOS 18.2, where the Liquid Glass
// APIs (`Glass`, `.glassEffect(_:in:)`, `GlassEffectContainer`) don't exist.
// These helpers apply a real `.glassEffect(...)` on OSes that have it and fall
// back to the existing opaque-fill + hairline-border + drop-shadow look on
// everything older, so a single call site renders correctly on every OS.
//
// Glass is reserved for interactive / floating chrome — the mini-player, the
// docked now-playing cell, transient toasts, the auto-connect card, the
// transport bar, and the selection pills in the sidebar nav and Library tab
// strip. Plain structural surfaces (the sidebar panel itself, list rows,
// full-screen backgrounds) stay solid by design.

/// Drop-shadow recipe used on the legacy (pre-26) fallback path, and as the
/// floating elevation under glass surfaces on OS 26+.
struct GlassShadow {
    let color: Color
    let radius: CGFloat
    let x: CGFloat
    let y: CGFloat

    static let none = GlassShadow(color: .clear, radius: 0, x: 0, y: 0)
    static let miniPlayer = GlassShadow(color: .black.opacity(0.55), radius: 16, x: 0, y: 8)
    static let toast = GlassShadow(color: .black.opacity(0.4), radius: 8, x: 0, y: 4)
    static let card = GlassShadow(color: .black.opacity(0.5), radius: 24, x: 0, y: 12)
}

private struct LiquidGlassModifier<S: Shape>: ViewModifier {
    let shape: S
    let tint: Color?
    let fallbackFill: Color
    let fallbackBorder: Color
    let shadow: GlassShadow
    let clipsContent: Bool
    let interactive: Bool

    func body(content: Content) -> some View {
        if #available(iOS 26.0, macOS 26.0, *) {
            // `clipsContent` clips the inner content (e.g. a flush-edge progress
            // bar) to the glass shape so nothing pokes past the rounded corners.
            // Surfaces whose content intentionally bleeds past the shape — like
            // the transport bar's glowing play disc — opt out so the glow shows.
            content
                .modifier(ConditionalClip(shape: shape, enabled: clipsContent))
                .glassEffect(glass, in: shape)
                .shadow(color: shadow.color, radius: shadow.radius, x: shadow.x, y: shadow.y)
        } else {
            content
                .background(fallbackFill)
                .modifier(ConditionalClip(shape: shape, enabled: clipsContent))
                .overlay(shape.stroke(fallbackBorder, lineWidth: 1))
                .shadow(color: shadow.color, radius: shadow.radius, x: shadow.x, y: shadow.y)
        }
    }

    @available(iOS 26.0, macOS 26.0, *)
    private var glass: Glass {
        var glass = Glass.regular
        if let tint { glass = glass.tint(tint) }
        if interactive { glass = glass.interactive() }
        return glass
    }
}

/// Clips to `shape` only when `enabled`; otherwise passes content through.
private struct ConditionalClip<S: Shape>: ViewModifier {
    let shape: S
    let enabled: Bool

    func body(content: Content) -> some View {
        if enabled {
            content.clipShape(shape)
        } else {
            content
        }
    }
}

extension View {
    /// Glassifies a floating surface, clipping content to `shape`.
    ///
    /// On iOS 26 / macOS 26 this lays a Liquid Glass material behind the
    /// content; on older OSes it reproduces the previous opaque look
    /// (`fallbackFill` + a hairline `fallbackBorder` stroke + `shadow`), so the
    /// same call site is correct everywhere.
    ///
    /// - Parameters:
    ///   - shape: the surface outline (e.g. `RoundedRectangle`, `Capsule`).
    ///   - tint: optional accent wash blended into the glass (and ignored on
    ///     the fallback path, which uses `fallbackFill`).
    ///   - fallbackFill: opaque fill drawn on pre-26 OSes.
    ///   - fallbackBorder: hairline stroke drawn on pre-26 OSes.
    ///   - shadow: floating elevation; applied on both paths.
    ///   - clipsContent: clip content to `shape` (default `true`). Set `false`
    ///     when content intentionally bleeds past the shape (e.g. a glowing
    ///     button on a glass bar).
    ///   - interactive: give the glass a fluid press/scale response (OS 26+).
    func liquidGlass(
        in shape: some Shape,
        tint: Color? = nil,
        fallbackFill: Color = .bg3,
        fallbackBorder: Color = .line2,
        shadow: GlassShadow = .none,
        clipsContent: Bool = true,
        interactive: Bool = false,
    ) -> some View {
        modifier(LiquidGlassModifier(
            shape: shape,
            tint: tint,
            fallbackFill: fallbackFill,
            fallbackBorder: fallbackBorder,
            shadow: shadow,
            clipsContent: clipsContent,
            interactive: interactive,
        ))
    }
}

// MARK: - Sliding selection pill

//
// A glass selection indicator that *slides* between sibling items (sidebar nav,
// the Library tab strip) as the selection moves. There is only ever ONE pill —
// it lives in the selected item's background and is moved with
// `matchedGeometryEffect`, so when the selection changes (inside an animated
// transaction) SwiftUI interpolates the pill's frame from the old item to the
// new one: it physically slides/stretches across. The pill wears a *neutral*
// glass material (tinting it gold over the near-black UI just reads as a flat
// amber block, not glass — selection is already signalled by the gold label).
//
// Usage: declare one `@Namespace`, give every sibling `.slidingGlassPill(...)`
// with the same `id` + namespace, and put an `.animation(_:value:)` keyed on
// the selection on their common ancestor (selection arrives asynchronously, so
// a `withAnimation` around the tap can't capture it).

private struct SlidingGlassPill<S: Shape, ID: Hashable>: ViewModifier {
    let selected: Bool
    let id: ID
    let namespace: Namespace.ID
    let shape: S
    let tint: Color?
    let fallbackFill: Color

    func body(content: Content) -> some View {
        content.background {
            if selected {
                pill.matchedGeometryEffect(id: id, in: namespace)
            }
        }
    }

    @ViewBuilder private var pill: some View {
        if #available(iOS 26.0, macOS 26.0, *) {
            Color.clear.glassEffect(glass, in: shape)
        } else {
            shape.fill(fallbackFill)
        }
    }

    @available(iOS 26.0, macOS 26.0, *)
    private var glass: Glass {
        if let tint { return Glass.regular.tint(tint) }
        return Glass.regular
    }
}

extension View {
    /// Marks this item as a member of a sliding-selection group. When
    /// `selected`, a single glass pill lives in its background and slides here
    /// (via `matchedGeometryEffect`) from the previously-selected sibling that
    /// shared `id`. Animate by putting `.animation(_:value:)` on the common
    /// ancestor, keyed on the selected value.
    ///
    /// - Parameters:
    ///   - tint: optional accent wash; default `nil` (neutral glass), since a
    ///     gold tint over the dark UI reads as a flat block rather than glass.
    ///   - fallbackFill: pre-26 fill for the pill.
    func slidingGlassPill(
        selected: Bool,
        id: some Hashable,
        in namespace: Namespace.ID,
        shape: some Shape,
        tint: Color? = nil,
        fallbackFill: Color = .accentDim,
    ) -> some View {
        modifier(SlidingGlassPill(
            selected: selected,
            id: id,
            namespace: namespace,
            shape: shape,
            tint: tint,
            fallbackFill: fallbackFill,
        ))
    }
}
