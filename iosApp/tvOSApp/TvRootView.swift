import SharedLogic
import SwiftUI

/// On launch, tries to restore the last-used server (re-authenticating from the
/// saved credentials, falling back to the saved token). Shows the connect form
/// only when there's nothing to restore or restore fails.
struct TvRootView: View {
    @Environment(TvContainer.self) private var container
    @State private var phase: Phase = .restoring

    private enum Phase { case restoring, connected, disconnected }

    var body: some View {
        switch phase {
        case .restoring:
            ProgressView("Connecting…")
                .task { await restore() }
        case .connected:
            TvMainView(onDisconnect: { phase = .disconnected })
        case .disconnected:
            TvConnectView(onConnected: { phase = .connected })
        }
    }

    private func restore() async {
        guard let last = try? await container.serverRepository.getLastUsedServer() else {
            phase = .disconnected
            return
        }
        // Prefer a fresh token from saved credentials; fall back to the stored one.
        let freshToken = try? await container.serverRepository.authenticate(
            host: last.host,
            port: last.port,
            useSsl: last.useSsl,
            sslPort: last.sslPort,
            username: last.username,
            passwordVal: last.passwordKey
        )
        let token = freshToken ?? last.authToken
        guard let token, !token.isEmpty else {
            phase = .disconnected
            return
        }
        container.serverRepository.setActiveServer(
            host: last.host, port: last.port, useSsl: last.useSsl, sslPort: last.sslPort, token: token
        )
        phase = .connected
    }
}
