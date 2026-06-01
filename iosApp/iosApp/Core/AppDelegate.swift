import CarPlay
import SharedLogic
import UIKit

private let log = SwiftLog("ui:iOS:AppDelegate")

/// Holds the app-scoped [AppContainer]. SwiftUI surfaces reach it via
/// `@Environment(AppContainer.self)`; non-SwiftUI surfaces (CarPlay, scene
/// delegates) reach it via `(UIApplication.shared.delegate as? AppDelegate)?.container`.
final class AppDelegate: NSObject, UIApplicationDelegate {
    private(set) var container: AppContainer!

    func application(
        _: UIApplication,
        didFinishLaunchingWithOptions _: [UIApplication.LaunchOptionsKey: Any]? = nil,
    ) -> Bool {
        // Bootstrap logging FIRST so AppContainer init events end up captured.
        #if DEBUG
            let isDebug = true
        #else
            let isDebug = false
        #endif
        AppLogger.shared.configure(isDebug: isDebug, extraWriters: [])

        container = AppContainer()
        return true
    }

    /// Route each scene role to its delegate. Because the app runs a UIKit
    /// lifecycle (`main.swift`), this is always consulted — the SwiftUI `App`
    /// lifecycle previously swallowed the CarPlay role and crashed on connect.
    func application(
        _: UIApplication,
        configurationForConnecting connectingSceneSession: UISceneSession,
        options _: UIScene.ConnectionOptions,
    ) -> UISceneConfiguration {
        log.d("configurationForConnecting role=\(connectingSceneSession.role.rawValue)")
        let config = UISceneConfiguration(
            name: nil,
            sessionRole: connectingSceneSession.role,
        )
        if connectingSceneSession.role == .carTemplateApplication {
            config.delegateClass = CarPlaySceneDelegate.self
        } else {
            config.delegateClass = SceneDelegate.self
        }
        return config
    }
}
