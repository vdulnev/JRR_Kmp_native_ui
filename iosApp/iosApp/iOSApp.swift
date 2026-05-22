import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    init() {
        // Access JrrDependencies to trigger lazy initialization and setup CorePlayer & Repositories
        _ = JrrDependencies.shared
        
        // Initialize download manager
        DownloadManager.shared.setup(libraryRepository: JrrDependencies.shared.libraryRepository)
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}