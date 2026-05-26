import SwiftUI
import SharedLogic

struct ServerManagerView: View {
    @ObservedObject var stateObserver = PlaybackStateObserver.shared
    
    let onConnectSuccess: () -> Void
    
    @State private var savedServers: [SavedServerEntity] = []
    
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
        ScrollView {
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
                .padding(.top, 20)
                
                // Connection Status Card if connected
                if let status = stateObserver.playerStatus, !stateObserver.activeZone.isOffline {
                    activeServerCard(friendlyName: status.zoneName)
                } else if !stateObserver.activeZone.isOffline && JrrDependencies.shared.facade.currentServerHost != nil {
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
                            customTextField(title: "6-Digit Access Key", text: $accessKey, placeholder: "e.g. A1B2C3")
                                .onChange(of: accessKey) { oldVal, newVal in
                                    if newVal.count > 6 {
                                        accessKey = String(newVal.prefix(6))
                                    }
                                    accessKey = accessKey.uppercased()
                                }
                        } else {
                            customTextField(title: "Host Address / IP", text: $host, placeholder: "e.g. 192.168.1.100")
                            
                            HStack(spacing: 12) {
                                customTextField(title: "Port", text: useSsl ? $sslPort : $port, placeholder: useSsl ? "52200" : "52199")
                                    .keyboardType(.numberPad)
                                
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
                                        .stroke(Color.line2, lineWidth: 1)
                                )
                        }
                    }
                    .padding([.horizontal, .bottom], 16)
                }
                .background(Color.bg2)
                .cornerRadius(12)
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(Color.line2, lineWidth: 1)
                )
                
                // Saved Connections List
                if !savedServers.isEmpty {
                    Text("SAVED CONNECTIONS")
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .tracking(1.6)
                        .foregroundColor(.textTertiary)
                        .padding(.top, 10)
                    
                    VStack(spacing: 8) {
                        ForEach(savedServers, id: \.id) { server in
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
                                
                                Button(action: {
                                    deleteServer(server)
                                }) {
                                    Image(systemName: "trash")
                                        .foregroundColor(.errorColor)
                                        .frame(width: 44, height: 44)
                                }
                            }
                            .padding(12)
                            .background(Color.bg2)
                            .cornerRadius(10)
                            .overlay(
                                RoundedRectangle(cornerRadius: 10)
                                    .stroke(Color.line, lineWidth: 1)
                            )
                            .onTapGesture {
                                fillFormAndConnect(server: server)
                            }
                        }
                    }
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.bottom, 30)
        }
        .background(Color.bg1.ignoresSafeArea())
        .onAppear {
            loadServers()
        }
    }
    
    // Custom active server card component
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
            
            if let host = JrrDependencies.shared.facade.currentServerHost {
                let port = JrrDependencies.shared.facade.currentServerUseSsl ? JrrDependencies.shared.facade.currentServerSslPort : JrrDependencies.shared.facade.currentServerPort
                Text("\(host):\(port)")
                    .font(AppFont.ibmPlexMono(size: 11.5, weight: .regular))
                    .foregroundColor(.textSecondary)
            }
            
            HStack(spacing: 20) {
                Button(action: {
                    // Fill input fields with current connection info
                    if let host = JrrDependencies.shared.facade.currentServerHost {
                        self.host = host
                        self.port = String(JrrDependencies.shared.facade.currentServerPort)
                        self.useSsl = JrrDependencies.shared.facade.currentServerUseSsl
                        self.sslPort = String(JrrDependencies.shared.facade.currentServerSslPort)
                        self.activeTab = 1
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
                endPoint: .bottom
            )
        )
        .cornerRadius(AppSpacing.radiusList)
        .overlay(
            RoundedRectangle(cornerRadius: AppSpacing.radiusList)
                .stroke(Color.accentSoft, lineWidth: 1)
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
                        .stroke(Color.line2, lineWidth: 1)
                )
                .textInputAutocapitalization(.never)
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
                        .stroke(Color.line2, lineWidth: 1)
                )
                .textInputAutocapitalization(.never)
                .disableAutocorrection(true)
        }
    }
    
    private func loadServers() {
        Task {
            do {
                let servers = try await JrrDependencies.shared.serverRepository.getAllServers()
                let sorted = servers.sorted { s1, s2 in
                    let t1 = s1.lastUsedAt?.int64Value ?? 0
                    let t2 = s2.lastUsedAt?.int64Value ?? 0
                    return t1 > t2
                }
                await MainActor.run {
                    self.savedServers = sorted
                }
            } catch {
                print("Failed to load servers: \(error)")
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
                    
                    guard let result = try await JrrDependencies.shared.serverRepository.lookupAccessKey(key: accessKey) else {
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
                guard let token = try await JrrDependencies.shared.serverRepository.authenticate(
                    host: resolvedHost,
                    port: Int32(resolvedPort),
                    useSsl: resolvedUseSsl,
                    sslPort: Int32(resolvedSslPort),
                    username: username,
                    passwordVal: password
                ) else {
                    throw NSError(domain: "jrr", code: 4, userInfo: [NSLocalizedDescriptionKey: "Authentication failed. Check credentials"])
                }
                
                // Check Alive
                if let name = try await JrrDependencies.shared.serverRepository.checkAlive(
                    host: resolvedHost,
                    port: Int32(resolvedPort),
                    useSsl: resolvedUseSsl,
                    sslPort: Int32(resolvedSslPort),
                    token: token
                ) {
                    friendlyName = name
                }
                
                // Save Server to Room Database
                let serverId = "\(resolvedHost):\(resolvedUseSsl ? resolvedSslPort : resolvedPort)"
                let servers = try await JrrDependencies.shared.serverRepository.getAllServers()
                let existing = servers.first(where: { $0.host == resolvedHost && Int($0.port) == resolvedPort })

                let newServer = SavedServerEntity(
                    id: existing?.id ?? serverId,
                    host: resolvedHost,
                    port: Int32(resolvedPort),
                    username: username,
                    passwordKey: password,
                    friendlyName: friendlyName,
                    lastUsedAt: KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000)),
                    authToken: token,
                    useSsl: resolvedUseSsl,
                    sslPort: Int32(resolvedSslPort)
                )
                
                try await JrrDependencies.shared.serverRepository.saveServer(server: newServer)
                UserDefaults.standard.set(true, forKey: "has_saved_servers")
                
                // Configure handler
                JrrDependencies.shared.facade.setServerConnection(
                    host: resolvedHost,
                    port: Int32(resolvedPort),
                    useSsl: resolvedUseSsl,
                    sslPort: Int32(resolvedSslPort),
                    authToken: token
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
        self.host = server.host
        self.port = String(server.port)
        self.useSsl = server.useSsl
        self.sslPort = String(server.sslPort)
        self.username = server.username
        self.password = server.passwordKey
        self.activeTab = 1
        
        triggerConnect()
    }
    
    private func deleteServer(_ server: SavedServerEntity) {
        Task {
            do {
                try await JrrDependencies.shared.serverRepository.deleteServer(server: server)
                loadServers()
                // Update local storage flag
                let servers = try await JrrDependencies.shared.serverRepository.getAllServers()
                UserDefaults.standard.set(!servers.isEmpty, forKey: "has_saved_servers")
            } catch {
                print("Failed to delete server: \(error)")
            }
        }
    }
    
    private func enterOfflineMode() {
        JrrDependencies.shared.facade.setServerConnection(host: "", port: 0, useSsl: false, sslPort: 0, authToken: nil)
        JrrDependencies.shared.facade.setZone(zone: Zone.companion.Offline, skipLoadQueue: false)
        onConnectSuccess()
    }
    
    private func disconnectActiveServer() {
        JrrDependencies.shared.facade.setServerConnection(host: "", port: 0, useSsl: false, sslPort: 0, authToken: nil)
        JrrDependencies.shared.facade.setZone(zone: Zone.companion.Offline, skipLoadQueue: false)
    }
}
