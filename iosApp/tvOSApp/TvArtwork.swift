import SwiftUI

/// Square album/track artwork loaded from an MCWS image URL string. Falls back
/// to a music-note placeholder while loading or when the URL is empty.
struct TvArtwork: View {
    let urlString: String
    var size: CGFloat = 80

    var body: some View {
        ZStack {
            Color.gray.opacity(0.2)
            if let url = URL(string: urlString), !urlString.isEmpty {
                AsyncImage(url: url) { image in
                    image.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    Image(systemName: "music.note")
                        .foregroundStyle(.secondary)
                }
            } else {
                Image(systemName: "music.note")
                    .foregroundStyle(.secondary)
            }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}
