import SharedLogic
import SwiftUI

/// Shows the connect screen until a server is active, then the library.
struct TvRootView: View {
    @Environment(TvContainer.self) private var container
    @State private var connected = false

    var body: some View {
        if connected {
            TvLibraryView()
        } else {
            TvConnectView(connected: $connected)
        }
    }
}
