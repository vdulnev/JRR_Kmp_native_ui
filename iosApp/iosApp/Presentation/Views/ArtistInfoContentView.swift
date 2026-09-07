import SharedLogic
import SwiftUI

/// Renders a loaded `ArtistInfo`: the header strip, the multi-paragraph
/// biography, and the full discography. Shared by the artist card on the
/// library screen and by the artist-info sheets on library and album detail.
struct ArtistInfoContentView: View {
    let info: ArtistInfo

    @State private var didCopy = false

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            // Long-press any passage to select and copy it; the COPY button
            // below takes the whole profile in one go. Selection is scoped to
            // the prose so it can't swallow a tap on the button.
            profileText
                .textSelection(.enabled)

            Button(action: copyProfile) {
                HStack(spacing: 6) {
                    Image(systemName: didCopy ? "checkmark" : "doc.on.doc")
                        .font(.system(size: 11))
                    Text(didCopy ? "COPIED" : "COPY")
                        .font(AppFont.ibmPlexMono(size: 11, weight: .bold))
                }
                .foregroundColor(.accentColor)
            }
            .padding(.top, 6)
        }
    }

    private var profileText: some View {
        VStack(alignment: .leading, spacing: 10) {
            if !info.summaryLine.isEmpty {
                Text(info.summaryLine)
                    .styleMonoLabel()
                    .foregroundColor(.textTertiary)
            }

            // The model writes the biography as blank-line-separated paragraphs;
            // keep that structure instead of one dense wall of text.
            ForEach(Array(biographyParagraphs.enumerated()), id: \.offset) { _, paragraph in
                Text(paragraph)
                    .styleItemSubtitle()
                    .fixedSize(horizontal: false, vertical: true)
            }

            if !info.discography.isEmpty {
                Text("DISCOGRAPHY · \(info.discography.count) RELEASES")
                    .styleSectionLabel()
                    .foregroundColor(.textTertiary)
                    .padding(.top, 6)

                ForEach(Array(info.discography.enumerated()), id: \.offset) { index, album in
                    if index > 0 {
                        Divider().background(Color.line2)
                    }
                    discographyRow(album)
                }
            }
        }
    }

    private func copyProfile() {
        Clipboard.copy(info.plainText())
        didCopy = true
        Task {
            try? await Task.sleep(for: .seconds(2))
            didCopy = false
        }
    }

    private var biographyParagraphs: [String] {
        info.biography
            .components(separatedBy: "\n\n")
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty }
    }

    @ViewBuilder
    private func discographyRow(_ album: DiscographyAlbum) -> some View {
        HStack(alignment: .top, spacing: 10) {
            Text(album.year.isEmpty ? "—" : album.year)
                .styleMonoLabel()
                .foregroundColor(.accentColor)
                .frame(width: 44, alignment: .leading)

            VStack(alignment: .leading, spacing: 4) {
                Text(album.title)
                    .styleItemTitle()
                    .fixedSize(horizontal: false, vertical: true)
                if !album.kind.isEmpty {
                    Text(album.kind.uppercased())
                        .styleMonoLabel()
                        .foregroundColor(.textTertiary)
                }
                if !album.history.isEmpty {
                    Text(album.history)
                        .styleItemSubtitle()
                        .fixedSize(horizontal: false, vertical: true)
                }
                if !album.insight.isEmpty {
                    Text(album.insight)
                        .styleItemSubtitle()
                        .italic()
                        .foregroundColor(.textTertiary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
        }
        .padding(.top, 2)
    }
}
