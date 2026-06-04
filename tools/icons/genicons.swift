import AppKit
import CoreText

// Brand palette
let gold = NSColor(red: 0xC8/255.0, green: 0x92/255.0, blue: 0x2A/255.0, alpha: 1)
let darkBg = NSColor(red: 0x0E/255.0, green: 0x0E/255.0, blue: 0x10/255.0, alpha: 1)
let white = NSColor.white

let outDir = "/tmp/iconout"
let fm = FileManager.default
try? fm.createDirectory(atPath: outDir, withIntermediateDirectories: true)

func png(size: Int, _ draw: (CGContext, CGFloat) -> Void) -> Data {
    let cs = CGColorSpaceCreateDeviceRGB()
    let ctx = CGContext(data: nil, width: size, height: size, bitsPerComponent: 8,
                        bytesPerRow: 0, space: cs,
                        bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)!
    ctx.setAllowsAntialiasing(true)
    ctx.setShouldAntialias(true)
    ctx.interpolationQuality = .high
    draw(ctx, CGFloat(size))
    let cg = ctx.makeImage()!
    return NSBitmapImageRep(cgImage: cg).representation(using: .png, properties: [:])!
}

// Draw the "JRR" monogram centered, scaled so its glyph-path width ≈ widthFraction*size.
func monogram(_ ctx: CGContext, _ s: CGFloat, color: NSColor, widthFraction: CGFloat) {
    let text = "JRR"
    func attr(_ fs: CGFloat) -> NSAttributedString {
        let f = NSFont(name: "Avenir-Black", size: fs)
            ?? NSFont(name: "HelveticaNeue-Bold", size: fs)
            ?? NSFont.systemFont(ofSize: fs, weight: .heavy)
        return NSAttributedString(string: text, attributes: [
            .font: f, .foregroundColor: color, .kern: -fs * 0.04,
        ])
    }
    var fs = s * 0.5
    let line0 = CTLineCreateWithAttributedString(attr(fs))
    let w0 = CTLineGetBoundsWithOptions(line0, .useGlyphPathBounds).width
    fs *= (s * widthFraction) / w0
    let line = CTLineCreateWithAttributedString(attr(fs))
    let b = CTLineGetBoundsWithOptions(line, .useGlyphPathBounds)
    ctx.textPosition = CGPoint(x: (s - b.width) / 2 - b.minX,
                               y: (s - b.height) / 2 - b.minY)
    CTLineDraw(line, ctx)
}

func roundedRectPath(_ ctx: CGContext, rect: CGRect, radius: CGFloat) {
    let p = CGMutablePath()
    p.addRoundedRect(in: rect, cornerWidth: radius, cornerHeight: radius)
    ctx.addPath(p)
}

// Variants ------------------------------------------------------------------

// Opaque full-bleed square: dark bg, gold JRR. (iOS/macOS, Android base, ico, linux)
func square(_ size: Int) -> Data {
    png(size: size) { ctx, s in
        ctx.setFillColor(darkBg.cgColor); ctx.fill(CGRect(x: 0, y: 0, width: s, height: s))
        monogram(ctx, s, color: gold, widthFraction: 0.60)
    }
}

// Round (Android round legacy): dark disc on transparent, gold JRR.
func round(_ size: Int) -> Data {
    png(size: size) { ctx, s in
        ctx.setFillColor(darkBg.cgColor); ctx.fillEllipse(in: CGRect(x: 0, y: 0, width: s, height: s))
        monogram(ctx, s, color: gold, widthFraction: 0.58)
    }
}

// Adaptive foreground: gold JRR on transparent, sized within the 66% safe zone.
func foreground(_ size: Int) -> Data {
    png(size: size) { ctx, s in monogram(ctx, s, color: gold, widthFraction: 0.50) }
}

// Adaptive monochrome (themed icons): white JRR silhouette on transparent.
func monochrome(_ size: Int) -> Data {
    png(size: size) { ctx, s in monogram(ctx, s, color: white, widthFraction: 0.50) }
}

// Rounded + padded tile for the macOS .icns dock icon.
func rounded(_ size: Int) -> Data {
    png(size: size) { ctx, s in
        let pad = s * 0.10
        let r = CGRect(x: pad, y: pad, width: s - 2 * pad, height: s - 2 * pad)
        ctx.saveGState()
        roundedRectPath(ctx, rect: r, radius: (s - 2 * pad) * 0.22)
        ctx.clip()
        ctx.setFillColor(darkBg.cgColor); ctx.fill(r)
        ctx.restoreGState()
        monogram(ctx, s, color: gold, widthFraction: 0.52)
    }
}

func write(_ data: Data, _ name: String) {
    try! data.write(to: URL(fileURLWithPath: "\(outDir)/\(name)"))
}

// iOS / macOS app icon + general square master
write(square(1024), "appicon_1024.png")
write(square(512), "square_512.png")

// Android legacy square + round
let androidDensities: [(String, Int)] = [("mdpi", 48), ("hdpi", 72), ("xhdpi", 96), ("xxhdpi", 144), ("xxxhdpi", 192)]
for (d, px) in androidDensities {
    write(square(px), "android_\(d)_ic_launcher.png")
    write(round(px), "android_\(d)_ic_launcher_round.png")
}
// Android adaptive fg/monochrome (108dp base scaled per density)
let fgDensities: [(String, Int)] = [("mdpi", 108), ("hdpi", 162), ("xhdpi", 216), ("xxhdpi", 324), ("xxxhdpi", 432)]
for (d, px) in fgDensities {
    write(foreground(px), "android_\(d)_ic_launcher_foreground.png")
    write(monochrome(px), "android_\(d)_ic_launcher_monochrome.png")
}

// Desktop: Windows .ico source sizes (square), Linux png
for px in [16, 32, 48, 64, 128, 256] { write(square(px), "ico_\(px).png") }
write(square(512), "linux_512.png")

// Desktop macOS .icns iconset (rounded tile)
let iconset = "\(outDir)/jrr.iconset"
try? fm.createDirectory(atPath: iconset, withIntermediateDirectories: true)
let icnsSizes: [(String, Int)] = [
    ("icon_16x16.png", 16), ("icon_16x16@2x.png", 32),
    ("icon_32x32.png", 32), ("icon_32x32@2x.png", 64),
    ("icon_128x128.png", 128), ("icon_128x128@2x.png", 256),
    ("icon_256x256.png", 256), ("icon_256x256@2x.png", 512),
    ("icon_512x512.png", 512), ("icon_512x512@2x.png", 1024),
]
for (name, px) in icnsSizes {
    try! rounded(px).write(to: URL(fileURLWithPath: "\(iconset)/\(name)"))
}

print("done -> \(outDir)")
