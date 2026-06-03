import SharedLogic
import SwiftUI

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
                    ForEach(0 ..< fields.count, id: \.self) { idx in
                        let field = fields[idx]
                        Button(action: {
                            Clipboard.copy(field.value)
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
                    Clipboard.copy(allText)
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

                // Share uses UIKit's activity sheet (iOS only). On macOS the
                // "Copy All" action above covers the same need.
                #if os(iOS)
                    Button(action: {
                        let av = UIActivityViewController(activityItems: [allText], applicationActivities: nil)
                        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                           let rootVC = windowScene.windows.first?.rootViewController
                        {
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
                #endif
            }
            .padding(.horizontal, AppSpacing.screenHorizontalMargin)
            .padding(.vertical, 16)
        }
        .background(Color.bg2.ignoresSafeArea())
    }
}

extension Track: @retroactive Identifiable {
    public var id: String {
        fileKey
    }

    func toInfoFields() -> [(label: String, value: String)] {
        let secs = durationMs / 1000
        let timeStr = String(format: "%d:%02d", secs / 60, secs % 60)

        let list: [(label: String, value: String)] = [
            ("Title", name),
            ("Artist", artist),
            ("Album", album),
            ("Album Artist", albumArtist),
            ("Date", date),
            ("Genre", genre),
            ("Track Number", String(trackNumber)),
            ("Disc Number", String(discNumber)),
            ("Total Tracks", String(totalTracks)),
            ("Total Discs", String(totalDiscs)),
            ("Duration", timeStr),
            ("Bitrate", bitrate > 0 ? "\(bitrate) kbps" : ""),
            ("Bit Depth", bitDepth > 0 ? "\(bitDepth) bit" : ""),
            ("Sample Rate", sampleRate > 0 ? "\(sampleRate) Hz" : ""),
            ("Channels", channels > 0 ? String(channels) : ""),
            ("File Type", fileType),
            ("File Path", filePath),
            ("Folder Path", folderPath),
            ("Play Count", String(numberPlays)),
            ("File Key", fileKey),
        ]

        return list.filter { !$0.value.isEmpty && $0.value != "0" }
    }
}

extension Album: @retroactive Identifiable {
    public var id: String {
        name + folderPath
    }

    func toInfoFields() -> [(label: String, value: String)] {
        let list: [(label: String, value: String)] = [
            ("Name", name),
            ("Album Artist", albumArtist),
            ("Folder Path", folderPath),
            ("Parent Folder Path", parentFolderPath),
            ("Date", date),
            ("Artwork File Key", artworkFileKey),
            ("Total Discs", String(totalDiscs)),
            ("Disc Number", String(discNumber)),
        ]

        return list.filter { !$0.value.isEmpty && $0.value != "0" }
    }
}
