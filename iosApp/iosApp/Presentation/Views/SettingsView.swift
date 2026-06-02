import SharedLogic
import SwiftUI

private let log = SwiftLog("ui:iOS:Settings")

@Observable
@MainActor
class SettingsObservable {
    private let viewModel: SettingsViewModel

    var isOfflineMode: Bool = true
    var serverHost: String?
    var useSsl: Bool = false
    var serverPort: Int32 = 52199
    var serverSslPort: Int32 = 52200
    var downloadedTracksCount: Int32 = 0
    var downloadJobs: [DownloadJobEntity] = []
    var isDebugBuild: Bool = false
    var logSeverity: Kermit_coreSeverity = .info
    var localAudioQuality: LocalAudioQuality = .lossless
    var transientError: String?

    init(viewModel: SettingsViewModel) {
        self.viewModel = viewModel
        // Cheap synchronous seed only. The live subscription runs from the owning
        // view's `.task` (see `observe()`), never from `init`. SwiftUI re-runs
        // `init` for the throwaway `@State(initialValue:)` copy each time an
        // ancestor body re-evaluates (e.g. ~1/sec during playback); subscribing
        // here made every throwaway subscribe-then-cancel a Kotlin flow and log
        // init/deinit once per second.
        sync(state: viewModel.state.value)
    }

    /// Drives the live state subscription. Call from the owning view's `.task`
    /// so it starts once per view identity and stops when the view disappears.
    func observe() async {
        log.d("observe: start")
        defer { log.d("observe: end") }
        for await state in viewModel.state {
            sync(state: state)
        }
    }

    private func sync(state: SettingsViewState) {
        isOfflineMode = state.isOfflineMode
        serverHost = state.serverHost
        useSsl = state.useSsl
        serverPort = state.serverPort
        serverSslPort = state.serverSslPort
        downloadedTracksCount = state.downloadedTracksCount
        downloadJobs = state.downloadJobs
        isDebugBuild = state.isDebugBuild
        logSeverity = state.logSeverity
        localAudioQuality = state.localAudioQuality
        transientError = state.transientError
    }

    func clearDownloads() {
        viewModel.clearDownloads()
    }

    func clearTransientError() {
        viewModel.clearTransientError()
    }

    func setLogSeverity(_ severity: Kermit_coreSeverity) {
        viewModel.setLogSeverity(severity: severity)
    }

    func setLocalAudioQuality(_ quality: LocalAudioQuality) {
        viewModel.setLocalAudioQuality(quality: quality)
    }

    func exportLogText() -> String {
        viewModel.exportLogText()
    }
}

struct SettingsView: View {
    let onBackClick: () -> Void
    let onDisconnectClick: () -> Void
    var isLarge: Bool = false

    @State private var observable: SettingsObservable

    init(viewModel: SettingsViewModel, onBackClick: @escaping () -> Void, onDisconnectClick: @escaping () -> Void, isLarge: Bool = false) {
        _observable = State(initialValue: SettingsObservable(viewModel: viewModel))
        self.onBackClick = onBackClick
        self.onDisconnectClick = onDisconnectClick
        self.isLarge = isLarge
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
                                    .frame(maxWidth: isLarge ? nil : .infinity)
                                    .padding(.horizontal, isLarge ? 20 : 0)
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
                                    .frame(maxWidth: isLarge ? nil : .infinity)
                                    .padding(.horizontal, isLarge ? 20 : 0)
                                    .frame(height: 38)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 6)
                                            .stroke(Color.errorColor, lineWidth: 1),
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
                                .frame(maxWidth: isLarge ? nil : .infinity)
                                .padding(.horizontal, isLarge ? 20 : 0)
                                .frame(height: 38)
                                .background(observable.downloadedTracksCount == 0 ? Color.bg3 : Color.clear)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 6)
                                        .stroke(observable.downloadedTracksCount == 0 ? Color.clear : Color.errorColor, lineWidth: 1),
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

                // Section 3: Audio Quality — server-side transcode level for
                // local-zone streaming and downloads.
                Section {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("STREAMING & DOWNLOADS")
                            .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                            .foregroundColor(.textTertiary)

                        Text("Server transcodes to this format on the fly. Lossless preserves fidelity; lossy saves bandwidth.")
                            .font(AppFont.inter(size: 13, weight: .regular))
                            .foregroundColor(.textSecondary)

                        ForEach(LocalAudioQuality.allCases, id: \.self) { quality in
                            let selected = observable.localAudioQuality == quality
                            Button(action: { observable.setLocalAudioQuality(quality) }) {
                                HStack(spacing: 10) {
                                    Image(systemName: selected ? "largecircle.fill.circle" : "circle")
                                        .font(.system(size: 18))
                                        .foregroundColor(selected ? .accentColor : .textTertiary)
                                    Text(quality.label)
                                        .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                        .foregroundColor(selected ? .accentColor : .textPrimary)
                                    Spacer()
                                }
                                .frame(maxWidth: .infinity)
                                .frame(height: 38)
                                .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.vertical, 8)
                } header: {
                    Text("Audio Quality")
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .foregroundColor(.textTertiary)
                }
                .listRowBackground(Color.bg2)

                // Section 4: Logging — share log + (debug-only) severity selector
                Section {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("DEBUG LOG")
                            .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                            .foregroundColor(.textTertiary)

                        Text("Recent activity from the in-memory ring buffer.")
                            .font(AppFont.inter(size: 13, weight: .regular))
                            .foregroundColor(.textSecondary)

                        ShareLink(item: observable.exportLogText(),
                                  subject: Text("JRR debug log"),
                                  preview: SharePreview("JRR debug log"))
                        {
                            Text("SHARE LOG")
                                .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                .foregroundColor(.accentColor)
                                .frame(maxWidth: isLarge ? nil : .infinity)
                                .padding(.horizontal, isLarge ? 20 : 0)
                                .frame(height: 38)
                                .overlay(
                                    RoundedRectangle(cornerRadius: 6)
                                        .stroke(Color.accentColor, lineWidth: 1),
                                )
                        }

                        if observable.isDebugBuild {
                            Text("MIN SEVERITY")
                                .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                .foregroundColor(.textTertiary)
                                .padding(.top, 8)

                            Text("Filter level for all log writers. Dev builds only.")
                                .font(AppFont.inter(size: 13, weight: .regular))
                                .foregroundColor(.textSecondary)

                            HStack(spacing: 4) {
                                ForEach([
                                    (Kermit_coreSeverity.verbose, "V"),
                                    (Kermit_coreSeverity.debug, "D"),
                                    (Kermit_coreSeverity.info, "I"),
                                    (Kermit_coreSeverity.warn, "W"),
                                    (Kermit_coreSeverity.error, "E"),
                                ], id: \.0) { sev, label in
                                    let selected = observable.logSeverity == sev
                                    Button(action: { observable.setLogSeverity(sev) }) {
                                        Text(label)
                                            .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                                            .foregroundColor(selected ? .bg0 : .textPrimary)
                                            .frame(maxWidth: .infinity)
                                            .frame(height: 38)
                                            .background(selected ? Color.accentColor : Color.clear)
                                            .overlay(
                                                RoundedRectangle(cornerRadius: 6)
                                                    .stroke(selected ? Color.accentColor : Color.line2, lineWidth: 1),
                                            )
                                    }
                                }
                            }
                        }
                    }
                    .padding(.vertical, 8)
                } header: {
                    Text("Logging")
                        .font(AppFont.ibmPlexMono(size: 11, weight: .regular))
                        .foregroundColor(.textTertiary)
                }
                .listRowBackground(Color.bg2)

                // Section 4: Active Downloads
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

                                            let stateText: String = switch job.state {
                                            case "QUEUED": "Queued"
                                            case "DOWNLOADING": "Downloading"
                                            case "FAILED": "Failed"
                                            default: job.state
                                            }
                                            Text("\(job.artist) · \(stateText)")
                                                .font(AppFont.inter(size: 13, weight: .regular))
                                                .foregroundColor(.textSecondary)
                                        }
                                        Spacer()
                                    }

                                    if job.state == "DOWNLOADING", job.bytesTotal > 0 {
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
            // Large screens: cap the card column to a comfortable reading width
            // (centered by the enclosing VStack) instead of full-bleed.
            .frame(maxWidth: isLarge ? 760 : .infinity)
        }
        .frame(maxWidth: .infinity)
        .background(Color.bg1.ignoresSafeArea())
        .task { await observable.observe() }
    }
}
