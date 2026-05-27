import SwiftUI
import SharedLogic
import KMPNativeCoroutinesAsync

class SwiftMainShellSettings: NSObject, MainShellSettings {
    func getLastActiveZoneId() -> String? {
        return UserDefaults.standard.string(forKey: "last_active_zone_id")
    }
    
    func setLastActiveZoneId(zoneId: String?) {
        UserDefaults.standard.set(zoneId, forKey: "last_active_zone_id")
    }
    
    func getHasSavedServers() -> Bool {
        return UserDefaults.standard.bool(forKey: "has_saved_servers")
    }
    
    func setHasSavedServers(hasSaved: Bool) {
        UserDefaults.standard.set(hasSaved, forKey: "has_saved_servers")
    }
}

@Observable
@MainActor
class MainShellObservable {
    let viewModel: MainShellViewModel
    
    var activeTab: Int = 1
    var selectedAlbum: Album? = nil
    var showQueue: Bool = false
    var isAutoConnecting: Bool = false
    var autoConnectServerName: String = ""
    var toastMessage: String? = nil
    
    nonisolated(unsafe) private var observeTask: Task<Void, Never>?

    init(viewModel: MainShellViewModel) {
        self.viewModel = viewModel

        sync(state: viewModel.state)

        observeTask = Task { @MainActor [weak self] in
            guard let stateFlow = self?.viewModel.stateFlow else { return }
            do {
                for try await state in asyncSequence(for: stateFlow) {
                    self?.sync(state: state)
                }
            } catch {
                // Flow cancelled
            }
        }
    }

    deinit {
        observeTask?.cancel()
    }
    
    private func sync(state: MainShellState) {
        self.activeTab = Int(state.activeTab)
        self.selectedAlbum = state.selectedAlbum
        self.showQueue = state.showQueue
        self.isAutoConnecting = state.isAutoConnecting
        self.autoConnectServerName = state.autoConnectServerName
        self.toastMessage = state.toastMessage
    }
    
    func performAutoConnect() {
        viewModel.performAutoConnect()
    }
    
    func cancelAutoConnect() {
        viewModel.cancelAutoConnect()
    }
    
    func selectTab(_ tab: Int) {
        viewModel.selectTab(tab: Int32(tab))
    }
    
    func selectAlbum(_ album: Album?) {
        viewModel.selectAlbum(album: album)
    }
    
    func setShowQueue(_ show: Bool) {
        viewModel.setShowQueue(show: show)
    }
    
    func clearToast() {
        viewModel.clearToast()
    }
    
    func disconnect() {
        viewModel.disconnect()
    }
}

struct ContentView: View {
    @Environment(AppContainer.self) private var container

    @State private var libraryViewModel: LibraryViewModel
    @State private var nowPlayingViewModel: NowPlayingViewModel
    @State private var queueViewModel: QueueViewModel
    @State private var zonesViewModel: ZonesViewModel
    @State private var nowPlayingObservable: NowPlayingObservable

    @State private var mainShellViewModel: MainShellViewModel
    @State private var mainShellObservable: MainShellObservable
    
    init(container: AppContainer) {
        let libVM = LibraryViewModel(libraryRepository: container.libraryRepository, facade: container.facade)
        let npVM = NowPlayingViewModel(facade: container.facade, mcwsClient: container.mcwsClient)
        let qVM = QueueViewModel(facade: container.facade, libraryRepository: container.libraryRepository)
        let zVM = ZonesViewModel(facade: container.facade, libraryRepository: container.libraryRepository)

        self._libraryViewModel = State(initialValue: libVM)
        self._nowPlayingViewModel = State(initialValue: npVM)
        self._queueViewModel = State(initialValue: qVM)
        self._zonesViewModel = State(initialValue: zVM)
        self._nowPlayingObservable = State(initialValue: NowPlayingObservable(viewModel: npVM))

        let settings = SwiftMainShellSettings()
        let shellVM = MainShellViewModel(facade: container.facade, serverRepository: container.serverRepository, settings: settings)
        self._mainShellViewModel = State(initialValue: shellVM)
        self._mainShellObservable = State(initialValue: MainShellObservable(viewModel: shellVM))
        
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
        let isPlaying = nowPlayingObservable.isPlaying
        let duration = nowPlayingObservable.durationMs
        let position = nowPlayingObservable.positionMs
        let progress = duration > 0 ? Double(position) / Double(duration) : 0.0
        
        ZStack {
            Color.bg1.ignoresSafeArea()
            
            if mainShellObservable.activeTab == 1 {
                ServerManagerView(onConnectSuccess: {
                    withAnimation {
                        mainShellObservable.selectTab(2) // Switch to Player
                    }
                })
            } else {
                TabView(selection: Binding(
                    get: { mainShellObservable.activeTab },
                    set: { newValue in
                        if newValue == 0 && mainShellObservable.activeTab == 0 {
                            // Tapping the active tab resets details
                            mainShellObservable.selectAlbum(nil)
                        }
                        mainShellObservable.selectTab(newValue)
                    }
                )) {
                    // Tab 2: Player (Now Playing)
                    PlayerTabContainerView(
                        showQueue: Binding(
                            get: { mainShellObservable.showQueue },
                            set: { mainShellObservable.setShowQueue($0) }
                        ),
                        queueViewModel: queueViewModel,
                        nowPlayingViewModel: nowPlayingViewModel
                    )
                    .tabItem {
                        Label("Player", systemImage: "play.circle.fill")
                    }
                    .tag(2)
                    
                    // Tab 0: Library
                    LibraryTabContainerView(
                        selectedAlbum: Binding(
                            get: { mainShellObservable.selectedAlbum },
                            set: { mainShellObservable.selectAlbum($0) }
                        ),
                        libraryViewModel: libraryViewModel
                    )
                    .tabItem {
                        Label("Library", systemImage: "music.note.house.fill")
                    }
                    .tag(0)
                    
                    // Tab 3: Zones
                    ZonesView(
                        viewModel: zonesViewModel,
                        onBackClick: {
                            withAnimation {
                                mainShellObservable.selectTab(2)
                            }
                        }
                    )
                    .tabItem {
                        Label("Zones", systemImage: "speaker.wave.3.fill")
                    }
                    .tag(3)
                    
                    // Tab 4: Settings
                    SettingsView(
                        isOfflineMode: zonesViewModel.state.isOfflineMode,
                        serverHost: container.facade.currentServerHost,
                        useSsl: container.facade.currentServerUseSsl,
                        serverPort: container.facade.currentServerPort,
                        serverSslPort: container.facade.currentServerSslPort,
                        onBackClick: {
                            withAnimation {
                                mainShellObservable.selectTab(2)
                            }
                        },
                        onDisconnectClick: {
                            mainShellObservable.disconnect()
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
            if mainShellObservable.activeTab != 2 && mainShellObservable.activeTab != 1 && nowPlayingObservable.trackTitle != "Idle" {
                VStack {
                    Spacer()
                    
                    MiniPlayer(
                        title: nowPlayingObservable.trackTitle,
                        artist: nowPlayingObservable.artistName,
                        imageUrl: nowPlayingObservable.imageUrl.isEmpty ? nil : nowPlayingObservable.imageUrl,
                        isPlaying: isPlaying,
                        progress: progress,
                        onPlayPauseClick: {
                            if isPlaying {
                                nowPlayingObservable.pause()
                            } else {
                                nowPlayingObservable.play()
                            }
                        },
                        onNextClick: {
                            nowPlayingObservable.next()
                        },
                        onPrevClick: {
                            nowPlayingObservable.previous()
                        },
                        onBodyClick: {
                            withAnimation {
                                mainShellObservable.selectTab(2) // Open Full Player
                            }
                        }
                    )
                    .padding(.horizontal, 16)
                    .padding(.bottom, 56) // Float above tab bar
                }
                .transition(.move(edge: .bottom).combined(with: .opacity))
            }
            
            // Auto-connecting Overlay
            if mainShellObservable.isAutoConnecting {
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
                        
                        Text("Connecting to \(mainShellObservable.autoConnectServerName)...")
                            .font(AppFont.inter(size: 14, weight: .regular))
                            .foregroundColor(.textSecondary)
                        
                        ProgressView()
                            .tint(.accentColor)
                            .scaleEffect(1.5)
                            .padding(.vertical, 16)
                        
                        Button(action: {
                            mainShellObservable.cancelAutoConnect()
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
            if let message = mainShellObservable.toastMessage {
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
                        .padding(.bottom, mainShellObservable.activeTab != 2 && nowPlayingObservable.trackTitle != "Idle" ? 140 : 80) // Position above tab bar/miniplayer
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                .ignoresSafeArea(.keyboard)
            }
        }
        .task {
            mainShellObservable.performAutoConnect()
        }
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        let container = AppContainer()
        ContentView(container: container)
            .environment(container)
    }
}

struct PlayerTabContainerView: View {
    @Binding var showQueue: Bool
    let queueViewModel: QueueViewModel
    let nowPlayingViewModel: NowPlayingViewModel
    
    var body: some View {
        if showQueue {
            QueueView(viewModel: queueViewModel, onBackClick: { showQueue = false })
        } else {
            NowPlayingView(viewModel: nowPlayingViewModel, onQueueClick: { showQueue = true })
        }
    }
}

struct LibraryTabContainerView: View {
    @Environment(AppContainer.self) private var container
    @Binding var selectedAlbum: Album?
    let libraryViewModel: LibraryViewModel

    var body: some View {
        if let album = selectedAlbum {
            let kmpAlbum = album
            let albumDetailViewModel = AlbumDetailViewModel(
                album: kmpAlbum,
                libraryRepository: container.libraryRepository,
                facade: container.facade,
                database: container.database
            )
            AlbumDetailView(
                viewModel: albumDetailViewModel,
                onBackClick: { selectedAlbum = nil }
            )
        } else {
            LibraryView(
                viewModel: libraryViewModel,
                onAlbumClick: { album in
                    selectedAlbum = album
                }
            )
        }
    }
}
