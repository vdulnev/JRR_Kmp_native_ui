import SharedLogic
import SwiftUI

/// Settings: server info, local playback quality, and disconnect. Mirrors the
/// other platforms' Settings (downloads/log-export omitted for tvOS).
struct TvSettingsView: View {
    @Environment(TvContainer.self) private var container
    let onDisconnect: () -> Void

    @State private var settingsVM: SettingsViewModel?
    @State private var currentQuality: LocalAudioQuality = .lossless

    private let qualities: [LocalAudioQuality] = [.lossless, .lossyHigh, .lossyNormal, .lossyLow]

    var body: some View {
        NavigationStack {
            List {
                Section("Server") {
                    LabeledContent("Host", value: container.facade.currentServerHost ?? "Not connected")
                    LabeledContent("Port", value: "\(container.facade.currentServerPort)")
                }

                Section("Local Playback Quality") {
                    ForEach(Array(qualities.enumerated()), id: \.offset) { _, q in
                        Button {
                            settingsVM?.setLocalAudioQuality(quality: q)
                            currentQuality = q
                        } label: {
                            HStack {
                                Text(q.label)
                                Spacer()
                                if q.label == currentQuality.label {
                                    Image(systemName: "checkmark").foregroundStyle(.tint)
                                }
                            }
                        }
                    }
                }

                Section {
                    Button("Disconnect", role: .destructive) {
                        Task {
                            await container.disconnect()
                            onDisconnect()
                        }
                    }
                }
            }
            .navigationTitle("Settings")
        }
        .onAppear {
            if settingsVM == nil {
                settingsVM = container.makeSettingsViewModel()
            }
            currentQuality = container.facade.currentLocalAudioQuality
        }
    }
}
