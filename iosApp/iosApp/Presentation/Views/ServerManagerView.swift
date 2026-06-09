import SharedLogic
import SwiftUI

private let log = SwiftLog("ui:iOS:ServerManager")

struct ServerManagerView: View {
    @Environment(AppContainer.self) private var container
    @Environment(\.horizontalSizeClass) private var hSizeClass
    @EnvironmentObject private var stateObserver: PlaybackStateObserver

    let onConnectSuccess: () -> Void

    private var isLarge: Bool {
        hSizeClass == .regular
    }

    @State private var savedServers: [SavedServerEntity] = []
    /// Saved profiles folded into groups (one entry per real server).
    @State private var serverGroups: [ServerGroup] = []
    /// Profile being marked as the same real server as another.
    @State private var mergeTarget: SavedServerEntity? = nil

    @State private var activeTab = 0 // 0 = Access Key, 1 = Manual IP
    @State private var accessKey = ""
    @State private var host = ""
    @State private var port = "52199"
    @State private var useSsl = false
    @State private var sslPort = "52200"
    @State private var username = ""
    @State private var password = ""
    @State private var isConnecting = false
    @State private var errorMessage: String? = nil

    var body: some View {
        Group {
            if isLarge {
                // Two columns: login form (left), saved connections (right).
                HStack(alignment: .top, spacing: 32) {
                    ScrollView { formColumn() }
                        .frame(maxWidth: .infinity)
                    ScrollView { savedColumn(showWhenEmpty: true) }
                        .frame(maxWidth: .infinity)
                }
                .padding(32)
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 20) {
                        formColumn()
                        savedColumn(showWhenEmpty: false)
                    }
                    .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                    .padding(.bottom, 30)
                }
            }
        }
        .background(Color.bg1.ignoresSafeArea())
        .onAppear { loadServers() }
        .confirmationDialog(
            "Same server as…",
            isPresented: Binding(get: { mergeTarget != nil }, set: { if !$0 { mergeTarget = nil } }),
            titleVisibility: .visible,
        ) {
            if let target = mergeTarget {
                ForEach(serverGroups.filter { $0.serverId != target.serverId }, id: \.serverId) { group in
                    Button(group.displayName) {
                        mergeProfile(target, into: group.serverId)
                        mergeTarget = nil
                    }
                }
            }
            Button("Cancel", role: .cancel) { mergeTarget = nil }
        } message: {
            Text("Pick the real server this connection points at. Its favorites merge with that server's.")
        }
    }

    private func formColumn() -> some View {
        VStack(alignment: .leading, spacing: 20) {
            // Section Title Overline
            VStack(alignment: .leading, spacing: 4) {
                Text("JRiver Remote".uppercased())
                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                    .tracking(2.5)
                    .foregroundColor(.accentColor)

                Text("Server Manager")
                    .font(AppFont.inter(size: 24, weight: .bold))
                    .foregroundColor(.textPrimary)
            }
            .padding(.top, isLarge ? 0 : 20)

            // Connection Status Card if connected
            if let status = stateObserver.playerStatus, !stateObserver.activeZone.isOffline {
                activeServerCard(friendlyName: status.zoneName)
            } else if !stateObserver.activeZone.isOffline, container.facade.currentServerHost != nil {
                activeServerCard(friendlyName: "JRiver Server")
            }

            // Form Card
            VStack(spacing: 0) {
                // Segmented Control
                HStack(spacing: 0) {
                    tabButton(title: "Access Key", index: 0)
                    tabButton(title: "Manual IP", index: 1)
                }
                .background(Color.bg0)
                .cornerRadius(8)
                .padding(16)

                VStack(spacing: 12) {
                    if activeTab == 0 {
                        customTextField(title: "6-Digit Access Key", text: $accessKey, placeholder: "e.g. aB3xZ9")
                            // Access keys are case-sensitive — do not force caps.
                            .noAutocapitalization()
                            .disableAutocorrection(true)
                            .onChange(of: accessKey) { _, newVal in
                                if newVal.count > 6 {
                                    accessKey = String(newVal.prefix(6))
                                }
                            }
                    } else {
                        customTextField(title: "Host Address / IP", text: $host, placeholder: "e.g. 192.168.1.100")

                        HStack(spacing: 12) {
                            customTextField(title: "Port", text: useSsl ? $sslPort : $port, placeholder: useSsl ? "52200" : "52199")
                                .numericKeyboard()

                            Toggle(isOn: $useSsl) {
                                Text("Use SSL")
                                    .font(AppFont.inter(size: 13, weight: .regular))
                                    .foregroundColor(.textPrimary)
                            }
                            .toggleStyle(SwitchToggleStyle(tint: .accentColor))
                            .frame(maxWidth: 130)
                            .padding(.top, 24)
                        }
                    }

                    customTextField(title: "Username (Optional)", text: $username, placeholder: "username")

                    customSecureField(title: "Password (Optional)", text: $password, placeholder: "password")

                    if let error = errorMessage {
                        Text(error)
                            .font(AppFont.inter(size: 13, weight: .medium))
                            .foregroundColor(.errorColor)
                            .padding(.top, 4)
                    }

                    // Connect Button
                    Button(action: triggerConnect) {
                        HStack {
                            Spacer()
                            if isConnecting {
                                ProgressView()
                                    .tint(.bg0)
                            } else {
                                Text("CONNECT")
                                    .font(AppFont.ibmPlexMono(size: 12, weight: .medium))
                                    .tracking(1.6)
                                    .foregroundColor(.bg0)
                            }
                            Spacer()
                        }
                        .frame(height: 48)
                        .background(Color.accentColor)
                        .cornerRadius(10)
                    }
                    .disabled(isConnecting)
                    .padding(.top, 12)

                    // Offline Mode Button
                    Button(action: enterOfflineMode) {
                        Text("ENTER OFFLINE MODE")
                            .font(AppFont.ibmPlexMono(size: 12, weight: .medium))
                            .tracking(1.6)
                            .foregroundColor(.textSecondary)
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.line2, lineWidth: 1),
                            )
                    }
                }
                .padding([.horizontal, .bottom], 16)
            }
            .background(Color.bg2)
            .cornerRadius(12)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color.line2, lineWidth: 1),
            )
        }
        // Keep the login form a comfortable width on large screens (so the
        // Connect / Offline buttons aren't stretched across half the display).
        .frame(maxWidth: isLarge ? 420 : .infinity, alignment: .leading)
    }

    private func savedColumn(showWhenEmpty: Bool) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            if serverGroups.isEmpty {
                if showWhenEmpty {
                    Text("SAVED CONNECTIONS")
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .tracking(1.6)
                        .foregroundColor(.textTertiary)
                        .padding(.top, isLarge ? 0 : 10)
                    Text("No saved connections yet.")
                        .font(AppFont.inter(size: 13, weight: .regular))
                        .foregroundColor(.textTertiary)
                }
            } else {
                Text("SAVED CONNECTIONS")
                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                    .tracking(1.6)
                    .foregroundColor(.textTertiary)
                    .padding(.top, isLarge ? 0 : 10)

                VStack(spacing: 8) {
                    ForEach(serverGroups, id: \.serverId) { group in
                        if group.profiles.count > 1 {
                            HStack {
                                Text("\(group.displayName)  (\(group.profiles.count))")
                                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                                    .tracking(1.2)
                                    .foregroundColor(.accentColor)
                                Spacer()
                            }
                            .padding(.top, 6)
                            ForEach(group.profiles, id: \.id) { server in
                                profileRow(server, indented: true, canSplit: true)
                            }
                        } else {
                            profileRow(group.profiles[0], indented: false, canSplit: false)
                        }
                    }
                }
            }
        }
    }

    private func profileRow(_ server: SavedServerEntity, indented: Bool, canSplit: Bool) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                Text(server.friendlyName ?? "JRiver Server")
                    .font(AppFont.inter(size: 16, weight: .medium))
                    .foregroundColor(.textPrimary)

                Text("\(server.host):\(server.useSsl ? server.sslPort : server.port)")
                    .font(AppFont.inter(size: 13, weight: .regular))
                    .foregroundColor(.textSecondary)
            }

            Spacer()

            Menu {
                if serverGroups.count > 1 {
                    Button {
                        mergeTarget = server
                    } label: {
                        Label("Same server as…", systemImage: "rectangle.3.group")
                    }
                }
                if canSplit {
                    Button { splitProfile(server) } label: {
                        Label("Make separate server", systemImage: "rectangle.badge.minus")
                    }
                }
                Button(role: .destructive) { deleteServer(server) } label: {
                    Label("Delete", systemImage: "trash")
                }
            } label: {
                Image(systemName: "ellipsis")
                    .foregroundColor(.textSecondary)
                    .frame(width: 44, height: 44)
                    .contentShape(Rectangle())
            }
            .buttonStyle(PlainButtonStyle())
        }
        .padding(12)
        .padding(.leading, indented ? 12 : 0)
        .background(Color.bg2)
        .cornerRadius(10)
        .overlay(
            RoundedRectangle(cornerRadius: 10)
                .stroke(Color.line, lineWidth: 1),
        )
        .contentShape(Rectangle())
        .onTapGesture {
            fillFormAndConnect(server: server)
        }
    }

    private func mergeProfile(_ server: SavedServerEntity, into targetServerId: String) {
        Task {
            try? await container.serverRepository.mergeProfileIntoServer(profileId: server.id, targetServerId: targetServerId)
            loadServers()
        }
    }

    private func splitProfile(_ server: SavedServerEntity) {
        Task {
            _ = try? await container.serverRepository.splitProfileToNewServer(profileId: server.id)
            loadServers()
        }
    }

    /// Custom active server card component
    private func activeServerCard(friendlyName: String) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("SERVER")
                    .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                    .tracking(2.5)
                    .foregroundColor(.accentColor)

                Spacer()

                HStack(spacing: 6) {
                    Circle()
                        .fill(Color.successColor)
                        .frame(width: 8, height: 8)
                    Text("ONLINE")
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .tracking(1.6)
                        .foregroundColor(.successColor)
                }
            }

            Text(friendlyName)
                .font(AppFont.inter(size: 17, weight: .bold))
                .foregroundColor(.textPrimary)

            if let host = container.facade.currentServerHost {
                let port = container.facade.currentServerUseSsl ? container.facade.currentServerSslPort : container.facade.currentServerPort
                Text("\(host):\(port)")
                    .font(AppFont.ibmPlexMono(size: 11.5, weight: .regular))
                    .foregroundColor(.textSecondary)
            }

            HStack(spacing: 20) {
                Button(action: {
                    // Fill input fields with current connection info
                    if let host = container.facade.currentServerHost {
                        self.host = host
                        port = String(container.facade.currentServerPort)
                        useSsl = container.facade.currentServerUseSsl
                        sslPort = String(container.facade.currentServerSslPort)
                        activeTab = 1
                    }
                }) {
                    Text("EDIT")
                        .font(AppFont.ibmPlexMono(size: 10, weight: .medium))
                        .tracking(1.6)
                        .foregroundColor(.accentColor)
                }

                Button(action: disconnectActiveServer) {
                    Text("LOGOUT")
                        .font(AppFont.ibmPlexMono(size: 10, weight: .medium))
                        .tracking(1.6)
                        .foregroundColor(.errorColor)
                }
            }
            .padding(.top, 4)
        }
        .padding(AppSpacing.cardPadding)
        .background(
            LinearGradient(
                colors: [Color.accentColor.opacity(0.08), Color.accentColor.opacity(0.02)],
                startPoint: .top,
                endPoint: .bottom,
            ),
        )
        .cornerRadius(AppSpacing.radiusList)
        .overlay(
            RoundedRectangle(cornerRadius: AppSpacing.radiusList)
                .stroke(Color.accentSoft, lineWidth: 1),
        )
    }

    private func tabButton(title: String, index: Int) -> some View {
        Button(action: {
            activeTab = index
            errorMessage = nil
        }) {
            Text(title)
                .font(AppFont.inter(size: 14, weight: .medium))
                .foregroundColor(activeTab == index ? .accentColor : .textSecondary)
                .frame(maxWidth: .infinity)
                .frame(height: 36)
                .background(activeTab == index ? Color.bg2 : Color.clear)
                .cornerRadius(6)
        }
    }

    private func customTextField(title: String, text: Binding<String>, placeholder: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(AppFont.inter(size: 12, weight: .regular))
                .foregroundColor(.textTertiary)

            TextField(placeholder, text: text)
                .font(AppFont.inter(size: 14, weight: .regular))
                .foregroundColor(.textPrimary)
                .padding(.horizontal, 12)
                .frame(height: 40)
                .background(Color.bg3)
                .cornerRadius(6)
                .overlay(
                    RoundedRectangle(cornerRadius: 6)
                        .stroke(Color.line2, lineWidth: 1),
                )
                .noAutocapitalization()
                .disableAutocorrection(true)
        }
    }

    private func customSecureField(title: String, text: Binding<String>, placeholder: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(AppFont.inter(size: 12, weight: .regular))
                .foregroundColor(.textTertiary)

            SecureField(placeholder, text: text)
                .font(AppFont.inter(size: 14, weight: .regular))
                .foregroundColor(.textPrimary)
                .padding(.horizontal, 12)
                .frame(height: 40)
                .background(Color.bg3)
                .cornerRadius(6)
                .overlay(
                    RoundedRectangle(cornerRadius: 6)
                        .stroke(Color.line2, lineWidth: 1),
                )
                .noAutocapitalization()
                .disableAutocorrection(true)
        }
    }

    private func loadServers() {
        Task {
            do {
                let servers = try await container.serverRepository.getAllServers()
                let sorted = servers.sorted { s1, s2 in
                    let t1 = s1.lastUsedAt
                    let t2 = s2.lastUsedAt
                    return t1 > t2
                }
                let groups = try await container.serverRepository.getServerGroups()
                await MainActor.run {
                    savedServers = sorted
                    serverGroups = groups
                }
            } catch {
                log.e("Failed to load servers: \(error)")
            }
        }
    }

    private func triggerConnect() {
        errorMessage = nil
        isConnecting = true

        Task {
            do {
                var resolvedHost = host
                var resolvedPort = Int(port) ?? 52199
                var resolvedSslPort = Int(sslPort) ?? 52200
                var resolvedUseSsl = useSsl
                var friendlyName = "JRiver Server"

                if activeTab == 0 {
                    // Access Key Lookup
                    guard accessKey.count == 6 else {
                        throw NSError(domain: "jrr", code: 1, userInfo: [NSLocalizedDescriptionKey: "Please enter a valid 6-digit access key"])
                    }

                    guard let result = try await container.serverRepository.lookupAccessKey(key: accessKey) else {
                        throw NSError(domain: "jrr", code: 2, userInfo: [NSLocalizedDescriptionKey: "Failed to look up Access Key"])
                    }

                    resolvedHost = (result.localIpList.first) ?? result.ip
                    resolvedPort = Int(result.port?.intValue ?? 52199)
                    resolvedSslPort = Int(result.httpsPort?.intValue ?? 52200)
                    resolvedUseSsl = useSsl // default user choice for SSL
                    friendlyName = "Server (\(accessKey))"
                } else {
                    guard !resolvedHost.isEmpty else {
                        throw NSError(domain: "jrr", code: 3, userInfo: [NSLocalizedDescriptionKey: "Please enter a host IP or address"])
                    }
                }

                // Authenticate
                guard let token = try await container.serverRepository.authenticate(
                    host: resolvedHost,
                    port: Int32(resolvedPort),
                    useSsl: resolvedUseSsl,
                    sslPort: Int32(resolvedSslPort),
                    username: username,
                    passwordVal: password,
                ) else {
                    throw NSError(domain: "jrr", code: 4, userInfo: [NSLocalizedDescriptionKey: "Authentication failed. Check credentials"])
                }

                // Check Alive
                if let name = try await container.serverRepository.checkAlive(
                    host: resolvedHost,
                    port: Int32(resolvedPort),
                    useSsl: resolvedUseSsl,
                    sslPort: Int32(resolvedSslPort),
                    token: token,
                ) {
                    friendlyName = name
                }

                // Save Server to Room Database
                let serverId = "\(resolvedHost):\(resolvedUseSsl ? resolvedSslPort : resolvedPort)"
                let servers = try await container.serverRepository.getAllServers()
                let existing = servers.first(where: { $0.host == resolvedHost && Int($0.port) == resolvedPort })

                let newServer = SavedServerEntity(
                    id: existing?.id ?? serverId,
                    host: resolvedHost,
                    port: Int32(resolvedPort),
                    username: username,
                    passwordKey: password,
                    friendlyName: friendlyName,
                    lastUsedAt: Int64(Date().timeIntervalSince1970 * 1000),
                    authToken: token,
                    useSsl: resolvedUseSsl,
                    sslPort: Int32(resolvedSslPort),
                    serverId: existing?.serverId ?? "",
                )

                // saveServer resolves/mints the real-server identity; scope
                // favorites to it.
                let resolvedServerId = try await container.serverRepository.saveServer(server: newServer)
                container.serverRepository.setActiveServerId(serverId: resolvedServerId)
                UserDefaults.standard.set(true, forKey: "has_saved_servers")

                // Configure handler
                container.facade.setServerConnection(
                    host: resolvedHost,
                    port: Int32(resolvedPort),
                    useSsl: resolvedUseSsl,
                    sslPort: Int32(resolvedSslPort),
                    authToken: token,
                )

                isConnecting = false
                loadServers()
                onConnectSuccess()
            } catch {
                isConnecting = false
                errorMessage = error.localizedDescription
            }
        }
    }

    private func fillFormAndConnect(server: SavedServerEntity) {
        host = server.host
        port = String(server.port)
        useSsl = server.useSsl
        sslPort = String(server.sslPort)
        username = server.username
        password = server.passwordKey
        activeTab = 1

        triggerConnect()
    }

    private func deleteServer(_ server: SavedServerEntity) {
        Task {
            do {
                try await container.serverRepository.deleteServer(server: server)
                loadServers()
                // Update local storage flag
                let servers = try await container.serverRepository.getAllServers()
                UserDefaults.standard.set(!servers.isEmpty, forKey: "has_saved_servers")
            } catch {
                log.e("Failed to delete server: \(error)")
            }
        }
    }

    private func enterOfflineMode() {
        container.facade.setServerConnection(host: "", port: 0, useSsl: false, sslPort: 0, authToken: nil)
        container.facade.setZone(zone: Zone.offline, skipLoadQueue: false)
        onConnectSuccess()
    }

    private func disconnectActiveServer() {
        container.facade.setServerConnection(host: "", port: 0, useSsl: false, sslPort: 0, authToken: nil)
        container.facade.setZone(zone: Zone.offline, skipLoadQueue: false)
    }
}
