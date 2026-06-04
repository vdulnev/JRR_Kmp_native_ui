import AppKit
import CoreText

let gold = NSColor(red: 0xC8/255.0, green: 0x92/255.0, blue: 0x2A/255.0, alpha: 1)
let darkBg = NSColor(red: 0x0E/255.0, green: 0x0E/255.0, blue: 0x10/255.0, alpha: 1)

func render(w: Int, h: Int, solidBackground: Bool, drawText: Bool) -> Data {
    let cs = CGColorSpaceCreateDeviceRGB()
    let ctx = CGContext(data: nil, width: w, height: h, bitsPerComponent: 8,
                        bytesPerRow: 0, space: cs,
                        bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)!
    ctx.setShouldAntialias(true)
    ctx.interpolationQuality = .high
    if solidBackground {
        ctx.setFillColor(darkBg.cgColor)
        ctx.fill(CGRect(x: 0, y: 0, width: w, height: h))
    }
    if drawText {
        let text = "JRR"
        func attr(_ fs: CGFloat) -> NSAttributedString {
            let f = NSFont(name: "Avenir-Black", size: fs)
                ?? NSFont.systemFont(ofSize: fs, weight: .heavy)
            return NSAttributedString(string: text, attributes: [.font: f, .foregroundColor: gold, .kern: -fs * 0.04])
        }
        var fs = CGFloat(h) * 0.5
        let b0 = CTLineGetBoundsWithOptions(CTLineCreateWithAttributedString(attr(fs)), .useGlyphPathBounds)
        fs *= (CGFloat(h) * 0.42) / b0.height
        let line = CTLineCreateWithAttributedString(attr(fs))
        let b = CTLineGetBoundsWithOptions(line, .useGlyphPathBounds)
        ctx.textPosition = CGPoint(x: (CGFloat(w) - b.width) / 2 - b.minX,
                                   y: (CGFloat(h) - b.height) / 2 - b.minY)
        CTLineDraw(line, ctx)
    }
    return NSBitmapImageRep(cgImage: ctx.makeImage()!).representation(using: .png, properties: [:])!
}

let root = CommandLine.arguments[1]
func write(_ data: Data, _ path: String) { try! data.write(to: URL(fileURLWithPath: "\(root)/\(path)")) }
func back(_ w: Int, _ h: Int, _ p: String) { write(render(w: w, h: h, solidBackground: true, drawText: false), p) }
func front(_ w: Int, _ h: Int, _ p: String) { write(render(w: w, h: h, solidBackground: false, drawText: true), p) }

// App Icon small 400x240 (@1x/@2x) — back (dark) + front (gold JRR, transparent)
back(400, 240, "AppIconSmall.imagestack/Back.imagestacklayer/Content.imageset/back_400x240.png")
back(800, 480, "AppIconSmall.imagestack/Back.imagestacklayer/Content.imageset/back_800x480.png")
front(400, 240, "AppIconSmall.imagestack/Front.imagestacklayer/Content.imageset/front_400x240.png")
front(800, 480, "AppIconSmall.imagestack/Front.imagestacklayer/Content.imageset/front_800x480.png")
// App Icon - App Store 1280x768 (@1x)
back(1280, 768, "AppIconLarge.imagestack/Back.imagestacklayer/Content.imageset/back_1280x768.png")
front(1280, 768, "AppIconLarge.imagestack/Front.imagestacklayer/Content.imageset/front_1280x768.png")
// Top Shelf 1920x720 (@1x/@2x) — flat (single image)
write(render(w: 1920, h: 720, solidBackground: true, drawText: true), "TopShelf.imageset/top_1920x720.png")
write(render(w: 3840, h: 1440, solidBackground: true, drawText: true), "TopShelf.imageset/top_3840x1440.png")
// Top Shelf Wide 2320x720 (@1x/@2x)
write(render(w: 2320, h: 720, solidBackground: true, drawText: true), "TopShelfWide.imageset/wide_2320x720.png")
write(render(w: 4640, h: 1440, solidBackground: true, drawText: true), "TopShelfWide.imageset/wide_4640x1440.png")
print("tvOS icon images (2-layer) written to \(root)")
