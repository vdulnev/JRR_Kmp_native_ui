import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) private var appDelegate

    var body: some Scene {
        WindowGroup {
            ContentView(container: appDelegate.container)
                .environment(appDelegate.container)
                .environmentObject(appDelegate.container.playbackStateObserver)
        }
    }
}
