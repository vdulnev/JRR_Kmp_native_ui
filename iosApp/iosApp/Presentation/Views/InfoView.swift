import SwiftUI
import SharedLogic

struct InfoView: View {
    let title: String
    let fields: [(label: String, value: String)]
    @Environment(\.dismiss) private var dismiss

    var allText: String {
        fields.map { "\($0.label): \($0.value)" }.joined(separator: "\n")
    }

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text(title.uppercased())
                    .font(AppFont.ibmPlexMono(size: 13, weight: .bold))
                    .foregroundColor(.accentColor)
                    .lineLimit(1)
                
                Spacer()
                
                Button(action: { dismiss() }) {
                    Image(systemName: "xmark")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.textPrimary)
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.top, 20)
            .padding(.bottom, 10)
            
            Divider()
                .background(Color.line)
            
            // Fields List
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    ForEach(0..<fields.count, id: \.self) { idx in
                        let field = fields[idx]
                        Button(action: {
                            UIPasteboard.general.string = field.value
                        }) {
                            VStack(alignment: .leading, spacing: 4) {
                                Text(field.label.uppercased())
                                    .font(AppFont.ibmPlexMono(size: 8, weight: .medium))
                                    .foregroundColor(.textTertiary)
                                
                                HStack {
                                    Text(field.value)
                                        .font(AppFont.inter(size: 14, weight: .medium))
                                        .foregroundColor(.textPrimary)
                                        .multilineTextAlignment(.leading)
                                    
                                    Spacer()
                                    
                                    Image(systemName: "doc.on.doc")
                                        .font(.system(size: 12))
                                        .foregroundColor(.textTertiary.opacity(0.5))
                                }
                            }
                        }
                        .buttonStyle(PlainButtonStyle())
                        
                        Divider()
                            .background(Color.line.opacity(0.5))
                    }
                }
                .padding(.horizontal, AppSpacing.screenHorizontalMargin)
                .padding(.vertical, 16)
            }
            
            Divider()
                .background(Color.line)
            
            // Actions
            HStack(spacing: 12) {
                Button(action: {
                    UIPasteboard.general.string = allText
                }) {
                    HStack {
                        Image(systemName: "doc.on.doc")
                        Text("Copy All")
                            .font(AppFont.inter(size: 13, weight: .semibold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.bg3)
                    .foregroundColor(.textPrimary)
                    .cornerRadius(8)
                }
                
                Button(action: {
                    let av = UIActivityViewController(activityItems: [allText], applicationActivities: nil)
                    if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                       let rootVC = windowScene.windows.first?.rootViewController {
                        rootVC.present(av, animated: true)
                    }
                }) {
                    HStack {
                        Image(systemName: "square.and.arrow.up")
                        Text("Share")
                            .font(AppFont.inter(size: 13, weight: .semibold))
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(Color.bg3)
                    .foregroundColor(.textPrimary)
                    .cornerRadius(8)
                }
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.vertical, 16)
        }
        .background(Color.bg2.ignoresSafeArea())
    }
}

extension Track: @retroactive Identifiable {
    public var id: String { fileKey }

    func toInfoFields() -> [(label: String, value: String)] {
        let secs = self.durationMs / 1000
        let timeStr = String(format: "%d:%02d", secs / 60, secs % 60)
        
        let list: [(label: String, value: String)] = [
            ("Title", self.name),
            ("Artist", self.artist),
            ("Album", self.album),
            ("Album Artist", self.albumArtist),
            ("Date", self.date),
            ("Genre", self.genre),
            ("Track Number", String(self.trackNumber)),
            ("Disc Number", String(self.discNumber)),
            ("Total Tracks", String(self.totalTracks)),
            ("Total Discs", String(self.totalDiscs)),
            ("Duration", timeStr),
            ("Bitrate", self.bitrate > 0 ? "\(self.bitrate) kbps" : ""),
            ("Bit Depth", self.bitDepth > 0 ? "\(self.bitDepth) bit" : ""),
            ("Sample Rate", self.sampleRate > 0 ? "\(self.sampleRate) Hz" : ""),
            ("Channels", self.channels > 0 ? String(self.channels) : ""),
            ("File Type", self.fileType),
            ("File Path", self.filePath),
            ("Folder Path", self.folderPath),
            ("Play Count", String(self.numberPlays)),
            ("File Key", self.fileKey)
        ]
        
        return list.filter { !$0.value.isEmpty && $0.value != "0" }
    }
}

extension Album: @retroactive Identifiable {
    public var id: String { name + folderPath }

    func toInfoFields() -> [(label: String, value: String)] {
        let list: [(label: String, value: String)] = [
            ("Name", self.name),
            ("Album Artist", self.albumArtist),
            ("Folder Path", self.folderPath),
            ("Parent Folder Path", self.parentFolderPath),
            ("Date", self.date),
            ("Artwork File Key", self.artworkFileKey),
            ("Total Discs", String(self.totalDiscs)),
            ("Disc Number", String(self.discNumber))
        ]
        
        return list.filter { !$0.value.isEmpty && $0.value != "0" }
    }
}
