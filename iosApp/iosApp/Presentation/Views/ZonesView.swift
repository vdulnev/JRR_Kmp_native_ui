import SwiftUI
import SharedLogic

struct ZonesView: View {
    @ObservedObject var stateObserver = PlaybackStateObserver.shared
    
    let onBackClick: () -> Void
    
    @State private var serverZones: [Zone] = []
    @State private var isLoading = false
    
    var isOfflineMode: Bool {
        stateObserver.activeZone.isOffline || JrrDependencies.shared.facade.currentServerHost == nil || JrrDependencies.shared.facade.currentServerHost?.isEmpty == true
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
                
                Text("ZONES")
                    .styleSectionLabel()
                
                Spacer()
                
                // Placeholder to balance back button
                Spacer()
                    .frame(width: 60)
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .background(Color.bg1)
            
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    // Server Outputs Section
                    if !isOfflineMode {
                        Text("SERVER OUTPUTS")
                            .styleSectionHeading()
                            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                            .padding(.top, 16)
                        
                        if isLoading {
                            HStack {
                                Spacer()
                                ProgressView()
                                    .tint(.accentColor)
                                Spacer()
                            }
                            .padding(.vertical, 20)
                        } else if serverZones.isEmpty {
                            Text("No server zones found.")
                                .font(AppFont.inter(size: 13, weight: .regular))
                                .foregroundColor(.textTertiary)
                                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        } else {
                            VStack(spacing: 12) {
                                ForEach(serverZones) { zone in
                                    let isActive = zone.id == stateObserver.activeZone.id
                                    ZoneRow(
                                        zone: zone,
                                        isActive: isActive,
                                        volume: isActive ? (stateObserver.playerStatus?.volume ?? 0.5) : 0.5,
                                        onZoneClick: {
                                            JrrDependencies.shared.facade.setZone(zone: zone, skipLoadQueue: false)
                                        },
                                        onVolumeChange: { newVolume in
                                            JrrDependencies.shared.facade.setVolume(level: newVolume)
                                        }
                                    )
                                }
                            }
                            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        }
                    }
                    
                    // On Device Section
                    Text("ON DEVICE")
                        .styleSectionHeading()
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.top, 10)
                    
                    VStack(spacing: 12) {
                        let deviceZones = getDeviceZones()
                        ForEach(deviceZones) { zone in
                            let isActive = zone.id == stateObserver.activeZone.id
                            ZoneRow(
                                zone: zone,
                                isActive: isActive,
                                volume: isActive ? (stateObserver.playerStatus?.volume ?? 0.5) : 0.5,
                                onZoneClick: {
                                    JrrDependencies.shared.facade.setZone(zone: zone, skipLoadQueue: false)
                                },
                                onVolumeChange: { newVolume in
                                    JrrDependencies.shared.facade.setVolume(level: newVolume)
                                }
                            )
                        }
                    }
                    .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                }
                .padding(.bottom, 30)
            }
        }
        .background(Color.bg1.ignoresSafeArea())
        .task {
            await loadZones()
        }
    }
    
    private func loadZones() async {
        guard !isOfflineMode else { return }
        isLoading = true
        do {
            let list = try await JrrDependencies.shared.libraryRepository.getZones()
            await MainActor.run {
                self.serverZones = list
                self.isLoading = false
            }
        } catch {
            print("Failed to load zones: \(error)")
            await MainActor.run {
                self.isLoading = false
            }
        }
    }
    
    private func getDeviceZones() -> [Zone] {
        return [Zone.companion.Local, Zone.companion.Offline]
    }
}

struct ZoneRow: View {
    let zone: Zone
    let isActive: Bool
    let volume: Float
    let onZoneClick: () -> Void
    let onVolumeChange: (Float) -> Void
    
    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(zone.name)
                        .font(AppFont.inter(size: 16, weight: .medium))
                        .foregroundColor(isActive ? .accentColor : .textPrimary)
                    
                    Text(zoneSubtext)
                        .font(AppFont.inter(size: 12, weight: .regular))
                        .foregroundColor(.textSecondary)
                }
                
                Spacer()
                
                if isActive {
                    Text("ACTIVE")
                        .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                        .foregroundColor(.accentColor)
                }
            }
            .contentShape(Rectangle())
            .onTapGesture(perform: onZoneClick)
            
            if isActive {
                VStack(spacing: 0) {
                    Spacer()
                        .frame(height: 16)
                    
                    HStack(spacing: 12) {
                        Image(systemName: "speaker.wave.2.fill")
                            .font(.system(size: 14))
                            .foregroundColor(.accentColor)
                        
                        Slider(
                            value: Binding(
                                get: { volume },
                                set: { onVolumeChange($0) }
                            ),
                            in: 0.0...1.0
                        )
                        .tint(.accentColor)
                    }
                }
            }
        }
        .padding(16)
        .background(isActive ? Color.bg3 : Color.bg2)
        .cornerRadius(8)
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(isActive ? Color.accentColor : Color.line, lineWidth: 1)
        )
    }
    
    private var zoneSubtext: String {
        if zone.isLocal {
            return "Local Playback"
        } else if zone.isOffline {
            return "Offline Library"
        } else if zone.isAndroidAuto {
            return "Car System"
        } else if zone.isDLNA {
            return "DLNA Renderer"
        } else {
            return "Network Zone"
        }
    }
}

extension Zone: Identifiable {}
