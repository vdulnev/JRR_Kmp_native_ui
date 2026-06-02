import SharedLogic
import SwiftUI

private let log = SwiftLog("ui:iOS:Zones")

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
    var transientError: String?

    init(viewModel: ZonesViewModel) {
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

    private func sync(state: ZonesViewState) {
        serverZones = state.serverZones
        deviceZones = state.deviceZones
        activeZoneId = state.activeZoneId
        currentVolume = state.currentVolume
        isLoading = state.isLoading
        isOfflineMode = state.isOfflineMode
        transientError = state.transientError
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
    var isLarge: Bool = false

    init(viewModel: ZonesViewModel, onBackClick: @escaping () -> Void, isLarge: Bool = false) {
        _observable = State(initialValue: ZonesObservable(viewModel: viewModel))
        self.onBackClick = onBackClick
        self.isLarge = isLarge
    }

    private let gridColumns = [GridItem(.adaptive(minimum: 300), spacing: 16)]

    @ViewBuilder
    private func zoneCell(_ zone: Zone) -> some View {
        let isActive = zone.id == observable.activeZoneId
        ZoneRow(
            zone: zone,
            isActive: isActive,
            volume: isActive ? observable.currentVolume : 0.5,
            onZoneClick: { observable.selectZone(zone) },
            onVolumeChange: { observable.setVolume($0) },
        )
    }

    /// A group of zone cards: adaptive grid on large screens, stacked column on
    /// phones.
    @ViewBuilder
    private func zonesGroup(_ zones: [Zone]) -> some View {
        if isLarge {
            LazyVGrid(columns: gridColumns, alignment: .leading, spacing: 16) {
                ForEach(zones) { zoneCell($0) }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
        } else {
            VStack(spacing: 12) {
                ForEach(zones) { zoneCell($0) }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
        }
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
                            zonesGroup(observable.serverZones)
                        }
                    }

                    // On Device Section
                    Text("ON DEVICE")
                        .styleSectionHeading()
                        .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                        .padding(.top, 10)

                    zonesGroup(observable.deviceZones)
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
                set: { if !$0 { observable.clearTransientError() } },
            ),
            actions: {
                Button("OK", role: .cancel) {}
            },
            message: {
                Text(observable.transientError ?? "")
            },
        )
        .task { await observable.observe() }
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
                                set: { onVolumeChange($0) },
                            ),
                            in: 0.0 ... 1.0,
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
                .stroke(isActive ? Color.accentColor : Color.line, lineWidth: 1),
        )
    }

    private var zoneSubtext: String {
        if zone.isLocal {
            "Local Playback"
        } else if zone.isOffline {
            "Offline Library"
        } else if zone.isAndroidAuto {
            "Car System"
        } else if zone.isDLNA {
            "DLNA Renderer"
        } else {
            "Network Zone"
        }
    }
}

extension Zone: @retroactive Identifiable {}
