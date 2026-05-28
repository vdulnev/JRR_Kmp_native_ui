import UIKit
import SharedLogic

/// Holds the app-scoped [AppContainer]. SwiftUI surfaces reach it via
/// `@Environment(AppContainer.self)`; non-SwiftUI surfaces (CarPlay, scene
/// delegates) reach it via `(UIApplication.shared.delegate as? AppDelegate)?.container`.
final class AppDelegate: NSObject, UIApplicationDelegate {

    private(set) var container: AppContainer!

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Bootstrap logging FIRST so AppContainer init events end up captured.
        #if DEBUG
        let isDebug = true
        #else
        let isDebug = false
        #endif
        AppLogger.shared.configure(isDebug: isDebug, extraWriters: [])

        self.container = AppContainer()
        return true
    }
}
