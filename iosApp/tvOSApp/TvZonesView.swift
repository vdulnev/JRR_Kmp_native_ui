import SharedLogic
import SwiftUI

/// Picks the JRiver output zone that playback commands target.
struct TvZonesView: View {
    @Environment(TvContainer.self) private var container
    @State private var zones: ZonesObservable?

    var body: some View {
        Group {
            if let zones {
                List {
                    if zones.isLoading && zones.serverZones.isEmpty {
                        ProgressView()
                    }
                    Section("This Device") {
                        ForEach(zones.deviceZones, id: \.id) { zone in
                            zoneRow(zone, model: zones)
                        }
                    }
                    Section("Server Zones") {
                        ForEach(zones.serverZones, id: \.id) { zone in
                            zoneRow(zone, model: zones)
                        }
                    }
                }
                .task { await zones.observe() }
            } else {
                ProgressView()
            }
        }
        .navigationTitle("Output Zone")
        .onAppear {
            if zones == nil {
                let vm = ZonesObservable(viewModel: container.makeZonesViewModel())
                zones = vm
                vm.refresh()
            }
        }
    }

    private func zoneRow(_ zone: Zone, model: ZonesObservable) -> some View {
        Button {
            model.select(zone)
        } label: {
            HStack {
                Text(zone.name)
                Spacer()
                if zone.id == model.activeZoneId {
                    Image(systemName: "checkmark").foregroundStyle(.tint)
                }
            }
        }
    }
}
