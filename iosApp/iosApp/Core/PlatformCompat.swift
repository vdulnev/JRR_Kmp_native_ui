import Foundation
import SwiftUI

#if canImport(UIKit)
    import UIKit

    /// Platform image type (`UIImage` on iOS, `NSImage` on macOS).
    typealias PlatformImage = UIImage
    /// Platform color type (`UIColor` on iOS, `NSColor` on macOS).
    typealias PlatformColor = UIColor
#elseif canImport(AppKit)
    import AppKit

    typealias PlatformImage = NSImage
    typealias PlatformColor = NSColor
#endif

extension Image {
    /// Cross-platform `Image` from a `PlatformImage` (`uiImage:` on iOS,
    /// `nsImage:` on macOS).
    init(platformImage: PlatformImage) {
        #if canImport(UIKit)
            self.init(uiImage: platformImage)
        #elseif canImport(AppKit)
            self.init(nsImage: platformImage)
        #endif
    }
}

/// View modifiers that are iOS-only in SwiftUI. These wrappers apply them on
/// iOS and are no-ops on macOS, so call sites stay platform-agnostic. The
/// iOS-only type names live inside `#if os(iOS)` so macOS never sees them.
extension View {
    @ViewBuilder
    func numericKeyboard() -> some View {
        #if os(iOS)
            keyboardType(.numberPad)
        #else
            self
        #endif
    }

    @ViewBuilder
    func noAutocapitalization() -> some View {
        #if os(iOS)
            textInputAutocapitalization(.never)
        #else
            self
        #endif
    }

    @ViewBuilder
    func hiddenNavigationBar() -> some View {
        #if os(iOS)
            toolbar(.hidden, for: .navigationBar)
        #else
            self
        #endif
    }

    @ViewBuilder
    func insetGroupedListStyle() -> some View {
        #if os(iOS)
            listStyle(.insetGrouped)
        #else
            listStyle(.inset)
        #endif
    }
}

/// Cross-platform clipboard write (`UIPasteboard` on iOS, `NSPasteboard` on macOS).
enum Clipboard {
    static func copy(_ string: String) {
        #if canImport(UIKit)
            UIPasteboard.general.string = string
        #elseif canImport(AppKit)
            NSPasteboard.general.clearContents()
            NSPasteboard.general.setString(string, forType: .string)
        #endif
    }
}

/// Open a URL in the system browser (`UIApplication` on iOS, `NSWorkspace` on macOS).
func openExternalURL(_ url: URL) {
    #if canImport(UIKit)
        UIApplication.shared.open(url)
    #elseif canImport(AppKit)
        NSWorkspace.shared.open(url)
    #endif
}
