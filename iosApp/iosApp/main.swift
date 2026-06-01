import UIKit

// UIKit application entry point. We deliberately use a UIKit lifecycle (rather
// than a SwiftUI `App`) so the app delegate owns scene configuration and can
// hand CarPlay scenes to `CarPlaySceneDelegate`. The normal window scene is
// hosted by `SceneDelegate`, which mounts the SwiftUI `ContentView`.
UIApplicationMain(
    CommandLine.argc,
    CommandLine.unsafeArgv,
    nil,
    NSStringFromClass(AppDelegate.self),
)
