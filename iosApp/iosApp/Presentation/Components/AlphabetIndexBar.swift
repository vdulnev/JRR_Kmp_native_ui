import SwiftUI

/// Section bucket letter for an alphabetical list label: the uppercased first
/// character if it's a letter in any script (Latin, Cyrillic, …), otherwise
/// "#" (digits, symbols) so those collapse into a single leading bucket —
/// matching the case-insensitive sort used by the library lists. The bar
/// derives its sections from the live data, so non-Latin letters show up as
/// their own sections in sort order.
func sectionLetter(for label: String) -> Character {
    guard let first = label.trimmingCharacters(in: .whitespacesAndNewlines).first,
          let upper = first.uppercased().first
    else {
        return "#"
    }
    return upper.isLetter ? upper : "#"
}

/// The distinct section letters of `labels`, preserving first-seen order. Since
/// the lists are pre-sorted alphabetically, this yields the letters top-to-bottom.
func orderedSectionLetters(_ labels: [String]) -> [Character] {
    var seen = Set<Character>()
    var result: [Character] = []
    for label in labels {
        let letter = sectionLetter(for: label)
        if seen.insert(letter).inserted { result.append(letter) }
    }
    return result
}

/// A vertical A–Z fast-scroll scrubber. Tapping or dragging a letter invokes
/// `onSelect`; the caller maps that to a row and scrolls via a
/// `ScrollViewReader`. Renders nothing when there are fewer than two sections,
/// since a single-bucket list has nothing to jump between.
///
/// `bottomInset` keeps that many points clear at the bottom so the strip
/// doesn't run behind the floating mini player.
struct AlphabetIndexBar: View {
    let letters: [Character]
    let onSelect: (Character) -> Void
    var bottomInset: CGFloat = 0

    /// Minimum points per label; the strip decimates so it never packs labels
    /// tighter than this.
    private let rowHeight: CGFloat = 18

    @State private var activeLetter: Character?
    @State private var isDragging = false
    @State private var touchY: CGFloat = 0

    var body: some View {
        if letters.count < 2 {
            EmptyView()
        } else {
            GeometryReader { geo in
                let usableHeight = max(rowHeight, geo.size.height - bottomInset)
                // Decimation: when more letters than comfortably fit, render
                // only every `step`-th letter (plus first/last/active) and show
                // "·" for the rest. The gesture still maps across all letters,
                // so every one stays reachable.
                let maxLabels = max(1, Int(usableHeight / rowHeight))
                let step = letters.count <= maxLabels
                    ? 1
                    : (letters.count + maxLabels - 1) / maxLabels

                ZStack(alignment: .topTrailing) {
                    VStack(spacing: 0) {
                        ForEach(Array(letters.enumerated()), id: \.offset) { index, letter in
                            let isActive = letter == activeLetter
                            let showLetter = step == 1
                                || index % step == 0
                                || index == letters.count - 1
                                || isActive
                            Text(showLetter ? String(letter) : "·")
                                .font(AppFont.ibmPlexMono(size: 11, weight: .medium))
                                .foregroundColor(isActive ? .accentColor : .textTertiary)
                                .frame(maxWidth: .infinity, maxHeight: .infinity)
                        }
                    }
                    .frame(height: usableHeight, alignment: .top)
                    .frame(maxHeight: .infinity, alignment: .top)

                    // Magnification bubble tracking the finger.
                    if isDragging, let active = activeLetter {
                        Text(String(active))
                            .font(AppFont.ibmPlexMono(size: 30, weight: .bold))
                            .foregroundColor(.textPrimary)
                            .frame(width: 60, height: 60)
                            .background(
                                Circle()
                                    // Frosted-glass bubble: a translucent
                                    // material blurs the list behind it.
                                    .fill(.ultraThinMaterial)
                                    .overlay(
                                        Circle().stroke(Color.accentColor.opacity(0.7), lineWidth: 1.5),
                                    )
                                    .shadow(color: .black.opacity(0.4), radius: 10, x: 0, y: 3),
                            )
                            .offset(x: -68, y: min(max(touchY - 30, 0), usableHeight - 60))
                            .transition(.scale(scale: 0.6).combined(with: .opacity))
                    }
                }
                .contentShape(Rectangle())
                // minimumDistance 0 so the initial touch (a tap) selects too.
                .gesture(
                    DragGesture(minimumDistance: 0)
                        .onChanged { value in
                            touchY = value.location.y
                            if !isDragging {
                                withAnimation(.spring(response: 0.25, dampingFraction: 0.7)) {
                                    isDragging = true
                                }
                            }
                            select(atY: value.location.y, height: usableHeight)
                        }
                        .onEnded { _ in
                            withAnimation(.easeOut(duration: 0.15)) {
                                isDragging = false
                                activeLetter = nil
                            }
                        },
                )
            }
            .frame(width: 24)
        }
    }

    private func select(atY y: CGFloat, height: CGFloat) {
        guard height > 0 else { return }
        let raw = Int(y / height * CGFloat(letters.count))
        let idx = max(0, min(letters.count - 1, raw))
        let letter = letters[idx]
        if letter != activeLetter {
            activeLetter = letter
            onSelect(letter)
        }
    }
}
