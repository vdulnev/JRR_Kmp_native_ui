import SharedLogic
import SwiftUI

private let log = SwiftLog("ui:iOS:MainShell")

class SwiftMainShellSettings: NSObject, MainShellSettings {
    func getLastActiveZoneId() -> String? {
        UserDefaults.standard.string(forKey: "last_active_zone_id")
    }

    func setLastActiveZoneId(zoneId: String?) {
        UserDefaults.standard.set(zoneId, forKey: "last_active_zone_id")
    }

    func getHasSavedServers() -> Bool {
        UserDefaults.standard.bool(forKey: "has_saved_servers")
    }

    func setHasSavedServers(hasSaved: Bool) {
        UserDefaults.standard.set(hasSaved, forKey: "has_saved_servers")
    }
}

/// Connection-flow state holder. Navigation now lives in the Decompose
/// `RootComponent`; this only mirrors the connect/toast fields the shell still
/// owns. `activeTab` is kept purely as a *bridge signal*: when the connect flow
/// flips it (auto-connect success → 2, failure/cancel/disconnect → 1) the host
/// forwards that to `root.selectTab`. Manual tab taps go straight to `root`.
@Observable
@MainActor
class MainShellObservable {
    let viewModel: MainShellViewModel

    var activeTab: Int = 1
    var isAutoConnecting: Bool = false
    var autoConnectServerName: String = ""
    var toastMessage: String?

    @ObservationIgnored private var observeTask: Task<Void, Never>?

    init(viewModel: MainShellViewModel) {
        log.d("init")
        self.viewModel = viewModel

        sync(state: viewModel.state.value)

        observeTask = Task { @MainActor [weak self] in
            guard let stateFlow = self?.viewModel.state else { return }
            for await state in stateFlow {
                self?.sync(state: state)
            }
        }
    }

    deinit {
        log.d("deinit")
        observeTask?.cancel()
    }

    private func sync(state: MainShellState) {
        activeTab = Int(state.activeTab)
        isAutoConnecting = state.isAutoConnecting
        autoConnectServerName = state.autoConnectServerName
        toastMessage = state.toastMessage
    }

    func performAutoConnect() {
        viewModel.performAutoConnect()
    }

    func cancelAutoConnect() {
        viewModel.cancelAutoConnect()
    }

    func clearToast() {
        viewModel.clearToast()
    }

    func disconnect() {
        viewModel.disconnect()
    }
}

// MARK: - Tab <-> RootConfig mapping

//
// The native TabView keeps its numeric `.tag`s (so UITabBarAppearance styling
// is untouched); these helpers translate between those tags and the typed
// `RootConfig` that `RootComponent` navigates with.

private func tabTag(for config: RootConfig) -> Int {
    switch onEnum(of: config) {
    case .library: 0
    case .server: 1
    case .player: 2
    case .zones: 3
    case .settings: 4
    }
}

private func config(forTag tag: Int) -> RootConfig {
    switch tag {
    case 0: RootConfigLibrary.shared
    case 2: RootConfigPlayer.shared
    case 3: RootConfigZones.shared
    case 4: RootConfigSettings.shared
    default: RootConfigServer.shared
    }
}

/// Drives the scroll-to-hide chrome (header, in-list filter, mini-player).
/// A scrolling list flips `collapsed`; the host hides the mini-player and the
/// Library screen collapses its header/filter, maximising the scroll area.
@Observable
@MainActor
final class ChromeVisibility {
    private(set) var collapsed = false

    func setCollapsed(_ value: Bool) {
        guard value != collapsed else { return }
        withAnimation(.easeInOut(duration: 0.22)) { collapsed = value }
    }
}

struct ContentView: View {
    @Environment(AppContainer.self) private var container
    @Environment(\.horizontalSizeClass) private var hSizeClass

    @State private var nowPlayingViewModel: NowPlayingViewModel
    @State private var nowPlayingObservable: NowPlayingObservable

    @State private var mainShellViewModel: MainShellViewModel
    @State private var mainShellObservable: MainShellObservable

    @State private var rootObservable: RootStackObservable
    @State private var chrome = ChromeVisibility()

    init(container: AppContainer) {
        // The mini-player overlay reads playback status from its own lightweight
        // NowPlayingViewModel (facade-backed, read-only here). The Player tab's
        // NowPlayingView gets its VM from the Decompose component instead.
        let npVM = NowPlayingViewModel(facade: container.facade, mcwsClient: container.mcwsClient)
        _nowPlayingViewModel = State(initialValue: npVM)
        _nowPlayingObservable = State(initialValue: NowPlayingObservable(viewModel: npVM))

        // MainShellViewModel still owns auto-connect + toast (connection
        // business logic). Navigation comes from container.root.
        let settings = SwiftMainShellSettings()
        let shellVM = MainShellViewModel(facade: container.facade, serverRepository: container.serverRepository, settings: settings)
        _mainShellViewModel = State(initialValue: shellVM)
        _mainShellObservable = State(initialValue: MainShellObservable(viewModel: shellVM))

        _rootObservable = State(initialValue: RootStackObservable(container.root))

        // Configure standard tab bar appearance with custom premium dark system theme
        let appearance = UITabBarAppearance()
        appearance.configureWithOpaqueBackground()

        let barColor = UIColor(red: 22 / 255.0, green: 22 / 255.0, blue: 24 / 255.0, alpha: 1.0) // bg2: 0x161618
        let lineColor = UIColor.white.withAlphaComponent(0.06) // line

        appearance.backgroundColor = barColor
        appearance.shadowColor = lineColor

        let goldAccent = UIColor(red: 200 / 255.0, green: 146 / 255.0, blue: 42 / 255.0, alpha: 1.0) // accentColor: 0xC8922A
        let dimText = UIColor.white.withAlphaComponent(0.3)

        appearance.stackedLayoutAppearance.normal.iconColor = dimText
        appearance.stackedLayoutAppearance.normal.titleTextAttributes = [.foregroundColor: dimText]
        appearance.stackedLayoutAppearance.selected.iconColor = goldAccent
        appearance.stackedLayoutAppearance.selected.titleTextAttributes = [.foregroundColor: goldAccent]

        UITabBar.appearance().standardAppearance = appearance
        UITabBar.appearance().scrollEdgeAppearance = appearance
    }

    // MARK: Active-tab helpers

    private var activeTag: Int {
        tabTag(for: rootObservable.activeConfig)
    }

    private var isServerActive: Bool {
        activeTag == 1
    }

    private var isPlayerActive: Bool {
        activeTag == 2
    }

    /// Large-screen (iPad regular width) layout: persistent sidebar instead of
    /// the bottom tab bar. Compact widths (iPhone, narrow iPad multitasking)
    /// keep the existing TabView.
    private var isLarge: Bool {
        hSizeClass == .regular
    }

    /// The active destination's content, sans tab-bar chrome — used by the
    /// large-screen sidebar shell (the TabView builds these inline with
    /// `.tabItem`).
    @ViewBuilder
    private func routedContent() -> some View {
        switch activeTag {
        case 0:
            if let library = libraryComponent() {
                LibraryTabContainerView(component: library, isLarge: true)
            } else {
                Color.bg1
            }
        case 3:
            if let zones = zonesComponent() {
                ZonesView(
                    viewModel: zones.vm,
                    onBackClick: { container.root.selectTab(config: RootConfigPlayer.shared) },
                    isLarge: true,
                )
            } else {
                Color.bg1
            }
        case 4:
            if let settings = settingsComponent() {
                SettingsView(
                    viewModel: settings.vm,
                    onBackClick: { container.root.selectTab(config: RootConfigPlayer.shared) },
                    onDisconnectClick: {
                        mainShellObservable.disconnect()
                        container.root.selectTab(config: RootConfigServer.shared)
                    },
                    isLarge: true,
                )
            } else {
                Color.bg1
            }
        default:
            if let player = playerComponent() {
                PlayerTabContainerView(component: player, isLarge: true)
            } else {
                Color.bg1
            }
        }
    }

    // MARK: Per-tab component lookup (stable instances from the live stack)

    private func libraryComponent() -> LibraryComponent? {
        for child in rootObservable.children {
            if case let .library(c) = onEnum(of: child) { return c.component }
        }
        return nil
    }

    private func playerComponent() -> PlayerComponent? {
        for child in rootObservable.children {
            if case let .player(c) = onEnum(of: child) { return c.component }
        }
        return nil
    }

    private func zonesComponent() -> ZonesComponent? {
        for child in rootObservable.children {
            if case let .zones(c) = onEnum(of: child) { return c.component }
        }
        return nil
    }

    private func settingsComponent() -> SettingsComponent? {
        for child in rootObservable.children {
            if case let .settings(c) = onEnum(of: child) { return c.component }
        }
        return nil
    }

    var body: some View {
        let isPlaying = nowPlayingObservable.isPlaying
        let duration = nowPlayingObservable.durationMs
        let position = nowPlayingObservable.positionMs
        let progress = duration > 0 ? Double(position) / Double(duration) : 0.0

        ZStack {
            Color.bg1.ignoresSafeArea()

            if isServerActive {
                ServerManagerView(onConnectSuccess: {
                    withAnimation {
                        container.root.onConnectSuccess()
                    }
                })
            } else if isLarge {
                LargeScreenShell(
                    activeTag: activeTag,
                    onSelect: { tag in
                        withAnimation {
                            container.root.selectTab(config: config(forTag: tag))
                        }
                    },
                    nowPlaying: nowPlayingObservable,
                ) {
                    routedContent()
                }
            } else {
                TabView(selection: Binding(
                    get: { activeTag },
                    set: { newValue in
                        withAnimation {
                            container.root.selectTab(config: config(forTag: newValue))
                        }
                    },
                )) {
                    // Tab 2: Player (Now Playing → Queue)
                    Group {
                        if let player = playerComponent() {
                            PlayerTabContainerView(component: player)
                        } else {
                            Color.bg1
                        }
                    }
                    .tabItem {
                        Label("Player", systemImage: "play.circle.fill")
                    }
                    .tag(2)

                    // Tab 0: Library (List → Detail)
                    Group {
                        if let library = libraryComponent() {
                            LibraryTabContainerView(component: library)
                        } else {
                            Color.bg1
                        }
                    }
                    .tabItem {
                        Label("Library", systemImage: "music.note.house.fill")
                    }
                    .tag(0)

                    // Tab 3: Zones
                    Group {
                        if let zones = zonesComponent() {
                            ZonesView(
                                viewModel: zones.vm,
                                onBackClick: {
                                    withAnimation {
                                        container.root.selectTab(config: RootConfigPlayer.shared)
                                    }
                                },
                            )
                        } else {
                            Color.bg1
                        }
                    }
                    .tabItem {
                        Label("Zones", systemImage: "speaker.wave.3.fill")
                    }
                    .tag(3)

                    // Tab 4: Settings
                    Group {
                        if let settings = settingsComponent() {
                            SettingsView(
                                viewModel: settings.vm,
                                onBackClick: {
                                    withAnimation {
                                        container.root.selectTab(config: RootConfigPlayer.shared)
                                    }
                                },
                                onDisconnectClick: {
                                    // Disconnect (when online) or Connect (when
                                    // offline): tear down and go to the Server
                                    // screen to pick a server or stay offline.
                                    mainShellObservable.disconnect()
                                    container.root.selectTab(config: RootConfigServer.shared)
                                },
                            )
                        } else {
                            Color.bg1
                        }
                    }
                    .tabItem {
                        Label("Settings", systemImage: "gearshape.fill")
                    }
                    .tag(4)
                }
                .tint(.accentColor)
            }

            // Floating MiniPlayer overlay — shown on every tab except Player and
            // the full-screen Server screen, when a track is loaded. Hidden
            // while a list is scrolled to maximise the scroll area.
            if !isPlayerActive, !isServerActive, !isLarge, !chrome.collapsed,
               nowPlayingObservable.trackTitle != "Idle"
            {
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
                                container.root.selectTab(config: RootConfigPlayer.shared)
                            }
                        },
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
                                        .stroke(Color.line2, lineWidth: 1),
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
                                .stroke(Color.line2, lineWidth: 1),
                        )
                        .shadow(color: Color.black.opacity(0.4), radius: 8, x: 0, y: 4)
                        .padding(.bottom, !isPlayerActive && nowPlayingObservable.trackTitle != "Idle" ? 140 : 80) // Position above tab bar/miniplayer
                        .transition(.move(edge: .bottom).combined(with: .opacity))
                }
                .ignoresSafeArea(.keyboard)
            }
        }
        .environment(chrome)
        // Switching tabs always restores the chrome (the new tab starts at the
        // top, and the mini-player should be visible there).
        .onChange(of: activeTag) { _, _ in
            chrome.setCollapsed(false)
        }
        .task {
            mainShellObservable.performAutoConnect()
        }
        // Bridge connect-driven tab changes (auto-connect success → Player,
        // failure/cancel/disconnect → Server) into the component tree. Manual
        // tab taps don't touch mainShellObservable, so this only fires for
        // connection events.
        .onChange(of: mainShellObservable.activeTab) { _, newTab in
            container.root.selectTab(config: config(forTag: newTab))
        }
    }
}

/// Player tab: NowPlaying → Queue, driven by [PlayerComponent.stack].
struct PlayerTabContainerView: View {
    let component: PlayerComponent
    var isLarge: Bool = false
    @State private var stack: PlayerStackObservable

    init(component: PlayerComponent, isLarge: Bool = false) {
        self.component = component
        self.isLarge = isLarge
        _stack = State(initialValue: PlayerStackObservable(component))
    }

    var body: some View {
        if isLarge {
            // Large screen: Now Playing hero + persistent queue rail side by
            // side. The queue is never pushed (hero's queue button is inert),
            // so the rail reads the component-level queue VM. The rail takes at
            // most ~36% of the width (capped at 380pt) so the hero isn't
            // squeezed on narrower large widths (e.g. iPad portrait).
            GeometryReader { geo in
                HStack(spacing: 0) {
                    Group {
                        if case let .nowPlaying(child) = onEnum(of: stack.activeChild) {
                            NowPlayingView(viewModel: child.vm, onQueueClick: {})
                        } else {
                            Color.bg1
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

                    Rectangle().fill(Color.line).frame(width: 1)

                    QueueView(viewModel: component.queueViewModel, onBackClick: {}, isRail: true)
                        .frame(width: min(380, geo.size.width * 0.36))
                }
            }
        } else {
            switch onEnum(of: stack.activeChild) {
            case let .nowPlaying(child):
                NowPlayingView(viewModel: child.vm, onQueueClick: { component.openQueue() })
            case let .queue(child):
                QueueView(viewModel: child.vm, onBackClick: { component.closeQueue() })
            }
        }
    }
}

/// Library tab: List → Detail, driven by [LibraryComponent.stack].
///
/// Backed by a `NavigationStack` whose path mirrors the Decompose stack. The
/// component stays the single source of truth — album taps call `openAlbum`,
/// and both the custom back button and the native swipe-back gesture resolve to
/// `component.back()`. SwiftUI keeps `LibraryView` alive underneath the pushed
/// `AlbumDetailView`, so the list's scroll position and drill-down state are
/// restored on pop automatically (no opacity / keep-mounted hack).
struct LibraryTabContainerView: View {
    let component: LibraryComponent
    var isLarge: Bool = false
    @State private var stack: LibraryStackObservable

    init(component: LibraryComponent, isLarge: Bool = false) {
        self.component = component
        self.isLarge = isLarge
        _stack = State(initialValue: LibraryStackObservable(component))
    }

    private var listVM: LibraryViewModel? {
        for child in stack.children {
            if case let .list(c) = onEnum(of: child) { return c.vm }
        }
        return nil
    }

    /// Detail entries above the list root, as a navigation path keyed by album
    /// group id (Hashable, and stable across re-renders since the same album
    /// keeps the same id).
    private var detailPath: [String] {
        stack.children.compactMap { child in
            if case let .detail(c) = onEnum(of: child) { return c.album.albumGroupId }
            return nil
        }
    }

    private func detailChild(forGroupId groupId: String) -> LibraryComponentChildDetail? {
        for child in stack.children {
            if case let .detail(c) = onEnum(of: child), c.album.albumGroupId == groupId {
                return c
            }
        }
        return nil
    }

    var body: some View {
        NavigationStack(path: Binding(
            get: { detailPath },
            set: { newPath in
                // SwiftUI trims from the tail on swipe-back / system back.
                // Forward the delta to Decompose (the source of truth).
                let popCount = detailPath.count - newPath.count
                if popCount > 0 {
                    for _ in 0 ..< popCount {
                        component.back()
                    }
                }
            },
        )) {
            Group {
                if let listVM {
                    LibraryView(
                        viewModel: listVM,
                        onAlbumClick: { album in
                            component.openAlbum(album: album)
                        },
                        isLarge: isLarge,
                    )
                } else {
                    Color.bg1
                }
            }
            // Hide the system bar (each screen draws its own header) while
            // keeping the interactive pop gesture — NavigationStack preserves it
            // under `.toolbar(.hidden,...)`, unlike `navigationBarHidden`.
            .toolbar(.hidden, for: .navigationBar)
            .navigationDestination(for: String.self) { groupId in
                Group {
                    if let detail = detailChild(forGroupId: groupId) {
                        AlbumDetailView(
                            viewModel: detail.vm,
                            onBackClick: { component.back() },
                            isLarge: isLarge,
                        )
                        .background(Color.bg1)
                    } else {
                        Color.bg1
                    }
                }
                .toolbar(.hidden, for: .navigationBar)
            }
        }
    }
}
