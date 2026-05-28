import SwiftUI
import SharedLogic

@Observable
@MainActor
class ZonesObservable {
    let viewModel: ZonesViewModel

    var serverZones: [Zone] = []
    var deviceZones: [Zone] = []
    var activeZoneId: String = ""
    var currentVolume: Float = 0.5
    var isLoading: Bool = false
    var isOfflineMode: Bool = true
    var transientError: String? = nil

    @ObservationIgnored private var observeTask: Task<Void, Never>?

    init(viewModel: ZonesViewModel) {
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
        observeTask?.cancel()
    }
    
    private func sync(state: ZonesViewState) {
        self.serverZones = state.serverZones
        self.deviceZones = state.deviceZones
        self.activeZoneId = state.activeZoneId
        self.currentVolume = state.currentVolume
        self.isLoading = state.isLoading
        self.isOfflineMode = state.isOfflineMode
        self.transientError = state.transientError
    }
    
    func refreshZones() {
        viewModel.refreshZones()
    }
    
    func selectZone(_ zone: Zone) {
        viewModel.selectZone(zone: zone)
    }
    
    func setVolume(_ level: Float) {
        viewModel.setVolume(level: level)
    }
    
    func clearTransientError() {
        viewModel.clearTransientError()
    }
}

struct ZonesView: View {
    @State private var observable: ZonesObservable
    
    let onBackClick: () -> Void
    
    init(viewModel: ZonesViewModel, onBackClick: @escaping () -> Void) {
        self._observable = State(initialValue: ZonesObservable(viewModel: viewModel))
        self.onBackClick = onBackClick
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
                    if !observable.isOfflineMode {
                        Text("SERVER OUTPUTS")
                            .styleSectionHeading()
                            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                            .padding(.top, 16)
                        
                        if observable.isLoading {
                            HStack {
                                Spacer()
                                ProgressView()
                                    .tint(.accentColor)
                                Spacer()
                            }
                            .padding(.vertical, 20)
                        } else if observable.serverZones.isEmpty {
                            Text("No server zones found.")
                                .font(AppFont.inter(size: 13, weight: .regular))
                                .foregroundColor(.textTertiary)
                                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        } else {
                            VStack(spacing: 12) {
                                ForEach(observable.serverZones) { zone in
                                    let isActive = zone.id == observable.activeZoneId
                                    ZoneRow(
                                        zone: zone,
                                        isActive: isActive,
                                        volume: isActive ? observable.currentVolume : 0.5,
                                        onZoneClick: {
                                            observable.selectZone(zone)
                                        },
                                        onVolumeChange: { newVolume in
                                            observable.setVolume(newVolume)
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
                        ForEach(observable.deviceZones) { zone in
                            let isActive = zone.id == observable.activeZoneId
                            ZoneRow(
                                zone: zone,
                                isActive: isActive,
                                volume: isActive ? observable.currentVolume : 0.5,
                                onZoneClick: {
                                    observable.selectZone(zone)
                                },
                                onVolumeChange: { newVolume in
                                    observable.setVolume(newVolume)
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
            observable.refreshZones()
        }
        .alert(
            "Error",
            isPresented: Binding(
                get: { observable.transientError != nil },
                set: { if !$0 { observable.clearTransientError() } }
            ),
            actions: {
                Button("OK", role: .cancel) {}
            },
            message: {
                Text(observable.transientError ?? "")
            }
        )
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

extension Zone: @retroactive Identifiable {}

