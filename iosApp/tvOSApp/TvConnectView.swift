import SharedLogic
import SwiftUI

/// Server connection form with a saved-connections picker. Authenticates over
/// MCWS and sets the active server (which the shared `McwsClient` reads to build
/// request URLs).
struct TvConnectView: View {
    @Environment(TvContainer.self) private var container
    let onConnected: () -> Void

    @State private var host = ""
    @State private var port = "52199"
    @State private var username = ""
    @State private var password = ""
    @State private var status = ""
    @State private var busy = false
    @State private var savedGroups: [ServerGroup] = []
    /// Profile awaiting a "same server as…" target pick.
    @State private var mergeProfile: SavedServerEntity? = nil

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                Text("Connect to JRiver Media Center")
                    .font(.largeTitle.bold())

                // "Same server as…" chooser: pick which real server this profile
                // belongs to. Its favorites merge into the chosen server.
                if let pending = mergeProfile {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("Same server as… (\(pending.friendlyName ?? pending.host))")
                            .font(.headline)
                        ForEach(savedGroups.filter { $0.serverId != pending.serverId }, id: \.serverId) { group in
                            Button(group.displayName) {
                                Task {
                                    try? await container.serverRepository.mergeProfileIntoServer(
                                        profileId: pending.id, targetServerId: group.serverId,
                                    )
                                    mergeProfile = nil
                                    await reloadSaved()
                                }
                            }
                        }
                        Button("Cancel") { mergeProfile = nil }
                    }
                    .frame(maxWidth: 900, alignment: .leading)
                } else if !savedGroups.isEmpty {
                    // Saved connections picker — reconnect to a remembered server.
                    VStack(alignment: .leading, spacing: 8) {
                        Text("SAVED CONNECTIONS").font(.caption).foregroundStyle(.secondary)
                        ForEach(savedGroups, id: \.serverId) { group in
                            if group.profiles.count > 1 {
                                Text("\(group.displayName)  (\(group.profiles.count))")
                                    .font(.caption).foregroundStyle(.secondary)
                            }
                            ForEach(group.profiles, id: \.id) { server in
                                HStack(spacing: 12) {
                                    Button {
                                        connect(
                                            host: server.host, port: server.port,
                                            username: server.username, password: server.passwordKey,
                                            useSsl: server.useSsl, sslPort: server.sslPort,
                                        )
                                    } label: {
                                        HStack {
                                            Text(server.friendlyName ?? server.host)
                                            Spacer()
                                            Text("\(server.host):\(server.useSsl ? server.sslPort : server.port)")
                                                .foregroundStyle(.secondary)
                                        }
                                    }
                                    .disabled(busy)
                                    if savedGroups.count > 1 {
                                        Button("Same as…") { mergeProfile = server }
                                    }
                                    if group.profiles.count > 1 {
                                        Button("Separate") {
                                            Task {
                                                _ = try? await container.serverRepository.splitProfileToNewServer(profileId: server.id)
                                                await reloadSaved()
                                            }
                                        }
                                    }
                                    Button(role: .destructive) {
                                        Task {
                                            try? await container.serverRepository.deleteServer(server: server)
                                            await reloadSaved()
                                        }
                                    } label: { Image(systemName: "trash") }
                                }
                            }
                        }
                        Text("Or add a new connection:").font(.headline).padding(.top, 12)
                    }
                    .frame(maxWidth: 900, alignment: .leading)
                }

                TextField("Host or IP", text: $host)
                TextField("Port", text: $port)
                TextField("Username", text: $username)
                SecureField("Password", text: $password)

                Button(busy ? "Connecting…" : "Connect") {
                    connect(
                        host: host, port: Int32(port) ?? 52199,
                        username: username, password: password,
                        useSsl: false, sslPort: 52200,
                    )
                }
                .disabled(busy || host.isEmpty)

                if !status.isEmpty {
                    Text(status).foregroundStyle(.red)
                }
            }
            .frame(maxWidth: 900)
            .padding(80)
        }
        .task { await reloadSaved() }
    }

    private func reloadSaved() async {
        savedGroups = await (try? container.serverRepository.getServerGroups()) ?? []
    }

    private func connect(host: String, port: Int32, username: String, password: String, useSsl: Bool, sslPort: Int32) {
        busy = true
        status = ""
        Task {
            defer { busy = false }
            do {
                let token = try await container.serverRepository.authenticate(
                    host: host,
                    port: port,
                    useSsl: useSsl,
                    sslPort: sslPort,
                    username: username,
                    passwordVal: password,
                )
                if let token {
                    // Route through the facade (not serverRepository directly):
                    // it sets the active server AND switches the active zone
                    // Offline → Local, which takes the app out of offline mode
                    // so the library and server zones come online.
                    container.facade.setServerConnection(
                        host: host, port: port, useSsl: useSsl, sslPort: sslPort, authToken: token,
                    )
                    // Persist so the connection is restored on next launch.
                    let server = SavedServerEntity(
                        id: "\(host):\(port)",
                        host: host,
                        port: port,
                        username: username,
                        passwordKey: password,
                        friendlyName: host,
                        lastUsedAt: Int64(Date().timeIntervalSince1970 * 1000),
                        authToken: token,
                        useSsl: useSsl,
                        sslPort: sslPort,
                        serverId: "",
                    )
                    if let sid = try? await container.serverRepository.saveServer(server: server) {
                        container.serverRepository.setActiveServerId(serverId: sid)
                    }
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
