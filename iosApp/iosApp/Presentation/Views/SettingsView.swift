import SwiftUI
import SharedLogic

private let log = SwiftLog("ui:iOS:Settings")

@Observable
@MainActor
class SettingsObservable {
    private let viewModel: SettingsViewModel
    
    var isOfflineMode: Bool = true
    var serverHost: String? = nil
    var useSsl: Bool = false
    var serverPort: Int32 = 52199
    var serverSslPort: Int32 = 52200
    var downloadedTracksCount: Int32 = 0
    var downloadJobs: [DownloadJobEntity] = []
    var transientError: String? = nil
    
    @ObservationIgnored private var observeTask: Task<Void, Never>?
    
    init(viewModel: SettingsViewModel) {
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
    
    private func sync(state: SettingsViewState) {
        self.isOfflineMode = state.isOfflineMode
        self.serverHost = state.serverHost
        self.useSsl = state.useSsl
        self.serverPort = state.serverPort
        self.serverSslPort = state.serverSslPort
        self.downloadedTracksCount = state.downloadedTracksCount
        self.downloadJobs = state.downloadJobs
        self.transientError = state.transientError
    }

    func clearDownloads() {
        viewModel.clearDownloads()
    }

    func clearTransientError() {
        viewModel.clearTransientError()
    }
}

struct SettingsView: View {
    let onBackClick: () -> Void
    let onDisconnectClick: () -> Void

    @State private var observable: SettingsObservable
    
    init(viewModel: SettingsViewModel, onBackClick: @escaping () -> Void, onDisconnectClick: @escaping () -> Void) {
        _observable = State(initialValue: SettingsObservable(viewModel: viewModel))
        self.onBackClick = onBackClick
        self.onDisconnectClick = onDisconnectClick
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: onBackClick) {
                    HStack(spacing: 4) {
                        Image(systemName: "chevron.left")
                            .font(.system(size: 16, weight: .bold))
                        Text("BACK")
                            .font(AppFont.ibmPlexMono(size: 11, weight: .medium))
                    }
                    .foregroundColor(.textPrimary)
                    .frame(height: 44)
                }
                
                Spacer()
                
                Text("SETTINGS")
                    .styleSectionLabel()
                
                Spacer()
                
                // Placeholder to balance back button
                Spacer()
                    .frame(width: 60)
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .background(Color.bg1)
            
            List {
                // Section 1: Connection
                Section {
                    VStack(alignment: .leading, spacing: 12) {
                        if observable.isOfflineMode {
                            Text("OFFLINE MODE")
                                .font(AppFont.ibmPlexMono(size: 12, weight: .bold))
                                .foregroundColor(.accentColor)
                            
                            Text("Playing cached and local files only")
                                .font(AppFont.inter(size: 13, weight: .regular))
                                .foregroundColor(.textSecondary)
                            
                            Button(action: onDisconnectClick) {
                                Text("CONNECT TO SERVER")
                                    .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                    .foregroundColor(.bg0)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 38)
                                    .background(Color.accentColor)
                                    .cornerRadius(6)
                            }
                        } else {
                            Text("CONNECTED SERVER")
                                .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                .foregroundColor(.textTertiary)
                            
                            Text("Host: \(observable.serverHost ?? "")")
                                .font(AppFont.inter(size: 15, weight: .medium))
                                .foregroundColor(.textPrimary)
                            
                            let port = observable.useSsl ? observable.serverSslPort : observable.serverPort
                            let type = observable.useSsl ? "SSL" : "HTTP"
                            Text("Port: \(port) (\(type))")
                                .font(AppFont.inter(size: 13, weight: .regular))
                                .foregroundColor(.textSecondary)
                            
                            Button(action: onDisconnectClick) {
                                Text("DISCONNECT / CHANGE SERVER")
                                    .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                    .foregroundColor(.errorColor)
                                    .frame(maxWidth: .infinity)
                                    .frame(height: 38)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 6)
                                            .stroke(Color.errorColor, lineWidth: 1)
                                    )
                            }
                            .padding(.top, 4)
                        }
                    }
                    .padding(.vertical, 8)
                } header: {
                    Text("Current Connection")
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .foregroundColor(.textTertiary)
                }
                .listRowBackground(Color.bg2)

                // Section 2: Storage & Downloads
                Section {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("DOWNLOADED TRACKS")
                            .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                            .foregroundColor(.textTertiary)
                        
                        Text("\(observable.downloadedTracksCount) Tracks cached")
                            .font(AppFont.inter(size: 15, weight: .medium))
                            .foregroundColor(.textPrimary)
                        
                        Text("Occupies space offline for lag-free playback")
                            .font(AppFont.inter(size: 13, weight: .regular))
                            .foregroundColor(.textSecondary)
                        
                        Button(action: {
                            observable.clearDownloads()
                        }) {
                            Text("CLEAR DOWNLOADS")
                                .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                .foregroundColor(observable.downloadedTracksCount == 0 ? .textTertiary : .errorColor)
                                .frame(maxWidth: .infinity)
                                .frame(height: 38)
                                .background(observable.downloadedTracksCount == 0 ? Color.bg3 : Color.clear)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 6)
                                        .stroke(observable.downloadedTracksCount == 0 ? Color.clear : Color.errorColor, lineWidth: 1)
                                )
                        }
                        .disabled(observable.downloadedTracksCount == 0)
                    }
                    .padding(.vertical, 8)
                } header: {
                    Text("Storage & Downloads")
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .foregroundColor(.textTertiary)
                }
                .listRowBackground(Color.bg2)

                // Section 3: Active Downloads
                if !observable.downloadJobs.isEmpty {
                    Section {
                        VStack(alignment: .leading, spacing: 12) {
                            ForEach(observable.downloadJobs, id: \.id) { job in
                                VStack(alignment: .leading, spacing: 4) {
                                    HStack {
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(job.name)
                                                .font(AppFont.inter(size: 15, weight: .medium))
                                                .foregroundColor(.textPrimary)
                                                .lineLimit(1)
                                            
                                            let stateText: String = {
                                                switch job.state {
                                                case "QUEUED": return "Queued"
                                                case "DOWNLOADING": return "Downloading"
                                                case "FAILED": return "Failed"
                                                default: return job.state
                                                }
                                            }()
                                            Text("\(job.artist) · \(stateText)")
                                                .font(AppFont.inter(size: 13, weight: .regular))
                                                .foregroundColor(.textSecondary)
                                        }
                                        Spacer()
                                    }
                                    
                                    if job.state == "DOWNLOADING" && job.bytesTotal > 0 {
                                        let pct = Double(job.bytesDownloaded) / Double(job.bytesTotal)
                                        ProgressView(value: pct)
                                            .tint(.accentColor)
                                            .padding(.top, 4)
                                    }
                                }
                                .padding(.vertical, 4)
                                
                                if job.id != observable.downloadJobs.last?.id {
                                    Divider()
                                        .background(Color.line2)
                                }
                            }
                        }
                        .padding(.vertical, 8)
                    } header: {
                        Text("Active Downloads")
                            .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                            .foregroundColor(.textTertiary)
                    }
                    .listRowBackground(Color.bg2)
                }
            }
            .listStyle(InsetGroupedListStyle())
            .scrollContentBackground(.hidden)
            .background(Color.bg1)
        }
        .background(Color.bg1.ignoresSafeArea())
    }
}
