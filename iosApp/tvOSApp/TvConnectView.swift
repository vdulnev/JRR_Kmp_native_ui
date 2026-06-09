import SharedLogic
import SwiftUI

/// Server connection form. Authenticates over MCWS and sets the active server
/// (which the shared `McwsClient` reads to build request URLs).
struct TvConnectView: View {
    @Environment(TvContainer.self) private var container
    let onConnected: () -> Void

    @State private var host = ""
    @State private var port = "52199"
    @State private var username = ""
    @State private var password = ""
    @State private var status = ""
    @State private var busy = false

    var body: some View {
        VStack(spacing: 24) {
            Text("Connect to JRiver Media Center")
                .font(.largeTitle.bold())

            TextField("Host or IP", text: $host)
            TextField("Port", text: $port)
            TextField("Username", text: $username)
            SecureField("Password", text: $password)

            Button(busy ? "Connecting…" : "Connect", action: connect)
                .disabled(busy || host.isEmpty)

            if !status.isEmpty {
                Text(status).foregroundStyle(.red)
            }
        }
        .frame(maxWidth: 900)
        .padding(80)
    }

    private func connect() {
        busy = true
        status = ""
        Task {
            defer { busy = false }
            let p = Int32(port) ?? 52199
            do {
                let token = try await container.serverRepository.authenticate(
                    host: host,
                    port: p,
                    useSsl: false,
                    sslPort: 52200,
                    username: username,
                    passwordVal: password,
                )
                if let token {
                    // Route through the facade (not serverRepository directly):
                    // it sets the active server AND switches the active zone
                    // Offline → Local, which takes the app out of offline mode
                    // so the library and server zones come online.
                    container.facade.setServerConnection(
                        host: host, port: p, useSsl: false, sslPort: 52200, authToken: token,
                    )
                    // Persist so the connection is restored on next launch.
                    let server = SavedServerEntity(
                        id: "\(host):\(p)",
                        host: host,
                        port: p,
                        username: username,
                        passwordKey: password,
                        friendlyName: host,
                        lastUsedAt: Int64(Date().timeIntervalSince1970 * 1000),
                        authToken: token,
                        useSsl: false,
                        sslPort: 52200,
                        groupName: nil,
                    )
                    try? await container.serverRepository.saveServer(server: server)
                    onConnected()
                } else {
                    status = "Authentication failed — check host, port, and credentials."
                }
            } catch {
                status = "Connection error: \(error.localizedDescription)"
            }
        }
    }
}
