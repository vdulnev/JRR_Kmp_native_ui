#if os(macOS)
    import SharedLogic
    import SwiftUI

    /// macOS app entry point (SwiftUI lifecycle).
    ///
    /// iOS uses a UIKit lifecycle (`main.swift` → `AppDelegate`/`SceneDelegate`)
    /// so the app delegate can route CarPlay scenes. macOS has no CarPlay/scenes,
    /// so it uses the standard SwiftUI `App`/`WindowGroup`. `AppContainer` creates
    /// and resumes the Decompose lifecycle in its own `init`, so it needs no
    /// scene/app-delegate plumbing — we just build it and inject it.
    ///
    /// Wrapped in `#if os(macOS)` so this `@main` never collides with iOS's
    /// `UIApplicationMain` entry even if the file is added to the iOS target.
    @main
    struct JrrMacApp: App {
        @State private var container: AppContainer

        init() {
            #if DEBUG
                let isDebug = true
            #else
                let isDebug = false
            #endif
            AppLogger.shared.configure(isDebug: isDebug, extraWriters: [])
            _container = State(initialValue: AppContainer())
        }

        var body: some Scene {
            WindowGroup {
                ContentView(container: container)
                    .environment(container)
                    .environmentObject(container.playbackStateObserver)
                    // The UI draws all its own button/field chrome and was built
                    // against iOS's plain control defaults. macOS defaults to
                    // bordered buttons and bezeled text fields, which double up
                    // with the custom visuals (square buttons, boxed text). Force
                    // plain styles app-wide so the custom design shows through;
                    // these propagate to every descendant control.
                    .buttonStyle(.plain)
                    .textFieldStyle(.plain)
                    .tint(.accentColor)
                    .frame(minWidth: 900, minHeight: 640)
            }
            .defaultSize(width: 1200, height: 820)
            .windowResizability(.contentMinSize)
        }
    }
#endif
