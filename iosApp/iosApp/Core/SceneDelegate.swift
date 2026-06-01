import SharedLogic
import SwiftUI
import UIKit

private let log = SwiftLog("ui:iOS:SceneDelegate")

/// Hosts the SwiftUI `ContentView` for the normal phone/iPad window scene.
///
/// The app uses a UIKit application lifecycle (see `main.swift`) rather than the
/// SwiftUI `App` lifecycle, because SwiftUI's `App` owns scene creation and never
/// hands CarPlay scenes to a `CPTemplateApplicationSceneDelegate`. With UIKit in
/// charge, `AppDelegate.application(_:configurationForConnecting:options:)` routes
/// the window role here and the CarPlay role to `CarPlaySceneDelegate`.
final class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo _: UISceneSession,
        options _: UIScene.ConnectionOptions,
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }
        guard let container = (UIApplication.shared.delegate as? AppDelegate)?.container else {
            log.e("AppContainer missing when window scene connected")
            return
        }
        log.d("window scene connected")

        let root = ContentView(container: container)
            .environment(container)
            .environmentObject(container.playbackStateObserver)

        let window = UIWindow(windowScene: windowScene)
        window.rootViewController = UIHostingController(rootView: root)
        self.window = window
        window.makeKeyAndVisible()
    }
}
