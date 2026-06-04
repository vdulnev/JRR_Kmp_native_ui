import SharedLogic
import SwiftUI

/// Server connection form. Authenticates over MCWS and sets the active server
/// (which the shared `McwsClient` reads to build request URLs).
struct TvConnectView: View {
    @Environment(TvContainer.self) private var container
    @Binding var connected: Bool

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
                    passwordVal: password
                )
                if let token {
                    container.serverRepository.setActiveServer(
                        host: host, port: p, useSsl: false, sslPort: 52200, token: token
                    )
                    connected = true
                } else {
                    status = "Authentication failed — check host, port, and credentials."
                }
            } catch {
                status = "Connection error: \(error.localizedDescription)"
            }
        }
    }
}
