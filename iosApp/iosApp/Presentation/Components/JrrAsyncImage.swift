import SwiftUI

// Global Image Cache for UI Images
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
        @ViewBuilder placeholder: @escaping () -> Placeholder
    ) {
        self.url = url
        self.content = content
        self.placeholder = placeholder
    }
    
    var body: some View {
        Group {
            if let image = image {
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
        .onChange(of: url) { oldVal, newVal in
            loadImage()
        }
    }
    
    private func loadImage() {
        currentTask?.cancel()
        
        guard let url = url else {
            self.image = nil
            return
        }
        
        if let cached = JrrImageCache.shared.object(forKey: url as NSURL) {
            self.image = cached
            return
        }
        
        self.image = nil
        self.isLoading = true
        self.loadError = false
        
        currentTask = Task {
            do {
                let (data, _) = try await URLSession.sslBypassingSession.data(from: url)
                try Task.checkCancellation()
                
                guard let uiImage = UIImage(data: data) else {
                    await MainActor.run {
                        self.isLoading = false
                        self.loadError = true
                    }
                    return
                }
                
                JrrImageCache.shared.setObject(uiImage, forKey: url as NSURL)
                
                await MainActor.run {
                    self.image = uiImage
                    self.isLoading = false
                }
            } catch {
                if !(error is CancellationError) {
                    print("Failed to load image from \(url): \(error)")
                    await MainActor.run {
                        self.isLoading = false
                        self.loadError = true
                    }
                }
            }
        }
    }
}
