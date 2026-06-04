import SharedLogic
import SwiftUI

/// Apple TV app entry point. Online-only: connects to a JRiver server over
/// MCWS and browses the library. Reuses the shared `SharedLogic` framework
/// (Room DB, MCWS client, repositories). Local audio playback is a follow-up;
/// this milestone proves the shared stack runs on tvOS end-to-end.
@main
struct TvOSApp: App {
    @State private var container = TvContainer()

    var body: some Scene {
        WindowGroup {
            TvRootView()
                .environment(container)
        }
    }
}
