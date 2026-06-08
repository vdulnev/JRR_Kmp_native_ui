import SharedLogic
import SwiftUI

/// Picks the JRiver output zone that playback commands target.
struct TvZonesView: View {
    let zones: ZonesObservable

    var body: some View {
        List {
            if zones.isLoading, zones.serverZones.isEmpty {
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
        .navigationTitle("Output Zone")
        .task { await zones.observe() }
        .onAppear {
            zones.refresh()
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
