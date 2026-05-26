import UIKit

/// Holds the app-scoped [AppContainer]. SwiftUI surfaces reach it via
/// `@Environment(AppContainer.self)`; non-SwiftUI surfaces (CarPlay, scene
/// delegates) reach it via `(UIApplication.shared.delegate as? AppDelegate)?.container`.
final class AppDelegate: NSObject, UIApplicationDelegate {

    private(set) var container: AppContainer!

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        self.container = AppContainer()
        return true
    }
}
