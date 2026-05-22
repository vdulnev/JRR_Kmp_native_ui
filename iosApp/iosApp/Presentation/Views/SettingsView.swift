import SwiftUI
import SharedLogic

struct SettingsView: View {
    @ObservedObject var stateObserver = PlaybackStateObserver.shared
    
    let onBackClick: () -> Void
    let onDisconnectClick: () -> Void
    
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
                        if isOfflineMode {
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
                            
                            Text("Host: \(JrrDependencies.shared.facade.currentServerHost ?? "")")
                                .font(AppFont.inter(size: 15, weight: .medium))
                                .foregroundColor(.textPrimary)
                            
                            let port = JrrDependencies.shared.facade.currentServerUseSsl ? JrrDependencies.shared.facade.currentServerSslPort : JrrDependencies.shared.facade.currentServerPort
                            let type = JrrDependencies.shared.facade.currentServerUseSsl ? "SSL" : "HTTP"
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
            }
            .listStyle(InsetGroupedListStyle())
            .scrollContentBackground(.hidden)
            .background(Color.bg1)
        }
        .background(Color.bg1.ignoresSafeArea())
    }
}
