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
                    Section("Server Zones") {
                        ForEach(zones.serverZones, id: \.id) { zone in
                            Button {
                                zones.select(zone)
                            } label: {
                                HStack {
                                    Text(zone.name)
                                    Spacer()
                                    if zone.id == zones.activeZoneId {
                                        Image(systemName: "checkmark")
                                            .foregroundStyle(.tint)
                                    }
                                }
                            }
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
}
