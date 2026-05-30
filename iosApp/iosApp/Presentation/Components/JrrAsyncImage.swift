import SwiftUI

private let log = SwiftLog("ui:iOS:JrrAsyncImage")

/// Global Image Cache for UI Images
class JrrImageCache {
    static let shared = NSCache<NSURL, UIImage>()
}

struct JrrAsyncImage<Content: View, Placeholder: View>: View {
    let url: URL?
    let content: (Image) -> Content
    let placeholder: () -> Placeholder

    @State private var image: UIImage? = nil
    @State private var isLoading = false
    @State private var loadError = false
    @State private var currentTask: Task<Void, Never>? = nil

    init(
        url: URL?,
        @ViewBuilder content: @escaping (Image) -> Content,
        @ViewBuilder placeholder: @escaping () -> Placeholder,
    ) {
        self.url = url
        self.content = content
        self.placeholder = placeholder
    }

    var body: some View {
        Group {
            if let image {
                content(Image(uiImage: image))
            } else {
                placeholder()
            }
        }
        .onAppear {
            loadImage()
        }
        .onDisappear {
            currentTask?.cancel()
        }
        .onChange(of: url) { _, _ in
            loadImage()
        }
    }

    private func loadImage() {
        currentTask?.cancel()

        guard let url else {
            image = nil
            return
        }

        var resolvedURL = url
        if let components = URLComponents(url: url, resolvingAgainstBaseURL: false),
           let fileQueryItem = components.queryItems?.first(where: { $0.name == "File" }),
           let fileKey = fileQueryItem.value
        {
            let fileManager = FileManager.default
            let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask)[0]
            let artURL = documentsURL.appendingPathComponent("downloads").appendingPathComponent("art_\(fileKey).jpg")
            if fileManager.fileExists(atPath: artURL.path) {
                resolvedURL = artURL
            }
        }

        if let cached = JrrImageCache.shared.object(forKey: resolvedURL as NSURL) {
            image = cached
            return
        }

        image = nil
        isLoading = true
        loadError = false

        currentTask = Task {
            do {
                let data: Data
                if resolvedURL.isFileURL {
                    data = try Data(contentsOf: resolvedURL)
                } else {
                    let (fetchedData, _) = try await URLSession.sslBypassingSession.data(from: resolvedURL)
                    data = fetchedData
                }
                try Task.checkCancellation()

                guard let uiImage = UIImage(data: data) else {
                    await MainActor.run {
                        isLoading = false
                        loadError = true
                    }
                    return
                }

                JrrImageCache.shared.setObject(uiImage, forKey: resolvedURL as NSURL)

                await MainActor.run {
                    image = uiImage
                    isLoading = false
                }
            } catch {
                if !(error is CancellationError) {
                    log.w("Failed to load image from \(resolvedURL): \(error)")
                    await MainActor.run {
                        isLoading = false
                        loadError = true
                    }
                }
            }
        }
    }
}
