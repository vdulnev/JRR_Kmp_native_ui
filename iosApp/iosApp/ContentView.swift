import SwiftUI
import SharedLogic

struct ContentView: View {
    @ObservedObject var stateObserver = PlaybackStateObserver.shared
    
    @State private var activeTab: Int = 1 // Start on Server Manager tab (1)
    
    // Nested navigation states
    @State private var selectedAlbum: (name: String, artist: String)? = nil
    @State private var showQueue: Bool = false
    
    // Auto-connect states
    @State private var isAutoConnecting: Bool = false
    @State private var autoConnectServerName: String = ""
    @State private var autoConnectTask: Task<Void, Never>? = nil
    @State private var hasAttemptedAutoConnect: Bool = false
    @State private var toastMessage: String? = nil
    
    init() {
        let lastActiveZoneId = UserDefaults.standard.string(forKey: "last_active_zone_id")
        let hasSavedServers = UserDefaults.standard.bool(forKey: "has_saved_servers")
        
        let initialTab: Int
        if lastActiveZoneId == Zone.companion.Offline.id {
            initialTab = 2
        } else if hasSavedServers {
            initialTab = 2
        } else {
            initialTab = 1
        }
        _activeTab = State(initialValue: initialTab)
        
        // Configure standard tab bar appearance with custom premium dark system theme
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()
        
        let barColor = UIColor(red: 22/255.0, green: 22/255.0, blue: 24/255.0, alpha: 1.0) // bg2: 0x161618
        let lineColor = UIColor.white.withAlphaComponent(0.06) // line
        
        appearance.backgroundColor = barColor
        appearance.shadowColor = lineColor
        
        let goldAccent = UIColor(red: 200/255.0, green: 146/255.0, blue: 42/255.0, alpha: 1.0) // accentColor: 0xC8922A
        let dimText = UIColor.white.withAlphaComponent(0.3)
        
        appearance.stackedLayoutAppearance.normal.iconColor = dimText
        appearance.stackedLayoutAppearance.normal.titleTextAttributes = [.foregroundColor: dimText]
        appearance.stackedLayoutAppearance.selected.iconColor = goldAccent
        appearance.stackedLayoutAppearance.selected.titleTextAttributes = [.foregroundColor: goldAccent]
        
        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }
    
    var body: some View {
        let status = stateObserver.playerStatus
        let isPlaying = status?.state == .playing
        let duration = status?.durationMs ?? 0
        let position = status?.positionMs ?? 0
        let progress = duration > 0 ? Double(position) / Double(duration) : 0.0
        
        ZStack {
            Color.bg1.ignoresSafeArea()
            
            if activeTab == 1 {
                ServerManagerView(onConnectSuccess: {
                    withAnimation {
                        activeTab = 2 // Switch to Player
                    }
                })
            } else {
                TabView(selection: Binding(
                    get: { activeTab },
                    set: { newValue in
                        if newValue == 0 && activeTab == 0 {
                            // Tapping the active tab resets details
                            selectedAlbum = nil
                        }
                        activeTab = newValue
                    }
                )) {
                    // Tab 2: Player (Now Playing)
                    Group {
                        if showQueue {
                            QueueView(onBackClick: { showQueue = false })
                        } else {
                            NowPlayingView(onQueueClick: { showQueue = true })
                        }
                    }
                    .tabItem {
                        Label("Player", systemImage: "play.circle.fill")
                    }
                    .tag(2)
                    
                    // Tab 0: Library
                    Group {
                        if let album = selectedAlbum {
                            AlbumDetailView(
                                albumName: album.name,
                                artistName: album.artist,
                                onBackClick: { selectedAlbum = nil }
                            )
                        } else {
                            LibraryView(onAlbumClick: { albumName, artistName in
                                selectedAlbum = (name: albumName, artist: artistName)
                            })
                        }
                    }
                    .tabItem {
                        Label("Library", systemImage: "music.note.house.fill")
                    }
                    .tag(0)
                    
                    // Tab 3: Zones
                    ZonesView(onBackClick: {
                        withAnimation {
                            activeTab = 2
                        }
                    })
                    .tabItem {
                        Label("Zones", systemImage: "speaker.wave.3.fill")
                    }
                    .tag(3)
                    
                    // Tab 4: Settings
                    SettingsView(
                        onBackClick: {
                            withAnimation {
                                activeTab = 2
                            }
                        },
                        onDisconnectClick: {
                            JrrDependencies.shared.facade.setServerConnection(
                                host: "",
                                port: Int32(0),
                                useSsl: false,
                                sslPort: Int32(0),
                                authToken: nil
                            )
                            JrrDependencies.shared.facade.setZone(zone: Zone.companion.Offline, skipLoadQueue: false)
                            withAnimation {
                                activeTab = 1 // Switch to Server Manager
                            }
                        }
                    )
                    .tabItem {
                        Label("Settings", systemImage: "gearshape.fill")
                    }
                    .tag(4)
                }
                .tint(.accentColor)
            }
            
            // Floating MiniPlayer overlay
            if activeTab != 2 && activeTab != 1 && status != nil {
                VStack {
                    Spacer()
                    
                    MiniPlayer(
                        title: status?.trackName ?? "",
                        artist: status?.trackArtist ?? "",
                        //TODO: implement later
                        imageUrl: nil,
                        isPlaying: isPlaying,
                        progress: progress,
                        onPlayPauseClick: {
                            if isPlaying {
                                JrrDependencies.shared.facade.pause()
                            } else {
                                JrrDependencies.shared.facade.play()
                            }
                        },
                        onNextClick: {
                            JrrDependencies.shared.facade.next()
                        },
                        onPrevClick: {
                            JrrDependencies.shared.facade.previous()
                        },
                        onBodyClick: {
                            withAnimation {
                                activeTab = 2 // Open Full Player
                            }
                        }
                    )
                    .padding(.horizontal, 16)
                    .padding(.bottom, 56) // Float above tab bar
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
            
            // Auto-connecting Overlay
            if isAutoConnecting {
                ZStack {
                    Color.bg0
                        .opacity(0.95)
                        .ignoresSafeArea()
                        // Scrim intercept: prevents tapping behind
                        .contentShape(Rectangle())
                        .onTapGesture {}
                    
                    VStack(spacing: 24) {
                        Text("JRiver Remote".uppercased())
                            .font(AppFont.ibmPlexMono(size: 24, weight: .bold))
                            .tracking(2.5)
                            .foregroundColor(.accentColor)
                        
                        Text("Connecting to \(autoConnectServerName)...")
                            .font(AppFont.inter(size: 14, weight: .regular))
                            .foregroundColor(.textSecondary)
                        
                        ProgressView()
                            .tint(.accentColor)
                            .scaleEffect(1.5)
                            .padding(.vertical, 16)
                        
                        Button(action: {
                            autoConnectTask?.cancel()
                            isAutoConnecting = false
                            autoConnectTask = nil
                            showToast(message: "Connection cancelled")
                            withAnimation {
                                activeTab = 1
                            }
                        }) {
                            Text("CANCEL")
                                .font(AppFont.ibmPlexMono(size: 11, weight: .medium))
                                .tracking(1.6)
                                .foregroundColor(.textSecondary)
                                .padding(.horizontal, 20)
                                .padding(.vertical, 10)
                                .background(Color.clear)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 8)
                                        .stroke(Color.line2, lineWidth: 1)
                                )
                        }
                    }
                    .padding(24)
                }
                .transition(.opacity)
            }
            
            // Premium Toast Message
            if let message = toastMessage {
                VStack {
                    Spacer()
                    Text(message)
                        .font(AppFont.inter(size: 13, weight: .medium))
                        .foregroundColor(.textPrimary)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                        .background(Color.bg3)
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color.line2, lineWidth: 1)
                        )
                        .shadow(color: Color.black.opacity(0.4), radius: 8, x: 0, y: 4)
                        .padding(.bottom, activeTab != 2 && status != nil ? 140 : 80) // Position above tab bar/miniplayer
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                .ignoresSafeArea(.keyboard)
            }
        }
        .task {
            performAutoConnect()
        }
    }
    
    private func performAutoConnect() {
        guard !hasAttemptedAutoConnect else { return }
        hasAttemptedAutoConnect = true
        
        let lastActiveZoneId = UserDefaults.standard.string(forKey: "last_active_zone_id")
        if lastActiveZoneId == Zone.companion.Offline.id {
            // Boot directly into offline mode (Player tab, zone offline)
            JrrDependencies.shared.facade.setZone(zone: Zone.companion.Offline, skipLoadQueue: false)
            withAnimation {
                activeTab = 2
            }
            return
        }
        
        autoConnectTask = Task {
            do {
                guard let lastServer = try await JrrDependencies.shared.serverRepository.getLastUsedServer() else {
                    // No saved servers, stay on Server Manager tab (1)
                    await MainActor.run {
                        UserDefaults.standard.set(false, forKey: "has_saved_servers")
                        withAnimation {
                            activeTab = 1
                        }
                    }
                    return
                }
                
                await MainActor.run {
                    UserDefaults.standard.set(true, forKey: "has_saved_servers")
                    autoConnectServerName = lastServer.friendlyName ?? "JRiver Server"
                    isAutoConnecting = true
                }
                
                // Perform authentication using the stored credentials
                guard let token = try await JrrDependencies.shared.serverRepository.authenticate(
                    host: lastServer.host,
                    port: lastServer.port,
                    useSsl: lastServer.useSsl,
                    sslPort: lastServer.sslPort,
                    username: lastServer.username,
                    passwordVal: lastServer.passwordKey
                ) else {
                    throw NSError(domain: "jrr", code: 4, userInfo: [NSLocalizedDescriptionKey: "Authentication failed. Check credentials"])
                }
                
                // Perform checkAlive to verify connection and get friendly name
                let finalName = try await JrrDependencies.shared.serverRepository.checkAlive(
                    host: lastServer.host,
                    port: lastServer.port,
                    useSsl: lastServer.useSsl,
                    sslPort: lastServer.sslPort,
                    token: token
                ) ?? lastServer.friendlyName ?? "JRiver Server"
                
                // Check if task is cancelled before modifying states
                try Task.checkCancellation()
                
                // Update server info
                let updatedServer = SavedServerEntity(
                    id: lastServer.id,
                    host: lastServer.host,
                    port: lastServer.port,
                    username: lastServer.username,
                    passwordKey: lastServer.passwordKey,
                    friendlyName: finalName,
                    lastUsedAt: KotlinLong(value: Int64(Date().timeIntervalSince1970 * 1000)),
                    authToken: token,
                    useSsl: lastServer.useSsl,
                    sslPort: lastServer.sslPort
                )
                try await JrrDependencies.shared.serverRepository.saveServer(server: updatedServer)
                
                // Configure handler
                JrrDependencies.shared.facade.setServerConnection(
                    host: lastServer.host,
                    port: lastServer.port,
                    useSsl: lastServer.useSsl,
                    sslPort: lastServer.sslPort,
                    authToken: token
                )
                JrrDependencies.shared.facade.setZone(zone: Zone.companion.Local, skipLoadQueue: false)
                
                await MainActor.run {
                    showToast(message: "Connected to \(finalName)")
                    withAnimation {
                        activeTab = 2
                    }
                }
            } catch is CancellationError {
                await MainActor.run {
                    showToast(message: "Connection cancelled")
                    withAnimation {
                        activeTab = 1
                    }
                }
            } catch {
                await MainActor.run {
                    showToast(message: "Auto-connect failed: \(error.localizedDescription)")
                    withAnimation {
                        activeTab = 1
                    }
                }
            }
            
            await MainActor.run {
                withAnimation {
                    isAutoConnecting = false
                }
                autoConnectTask = nil
            }
        }
    }
    
    private func showToast(message: String) {
        withAnimation {
            toastMessage = message
        }
        // Auto dismiss after 2.5 seconds
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.5) {
            if toastMessage == message {
                withAnimation {
                    toastMessage = nil
                }
            }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
    }
}
