# App icon generation

The JRR app icon is a gold (`#C8922A`) "JRR" monogram on the brand-dark
background (`#0E0E10`), rendered for every platform from a single source.

## Regenerate

```sh
swift tools/icons/genicons.swift     # writes PNGs + jrr.iconset to /tmp/iconout
```

Then rebuild the platform artefacts and copy them into place:

- **macOS .icns**: `iconutil -c icns /tmp/iconout/jrr.iconset -o desktopApp/packaging/jrr.icns`
- **Windows .ico**: pack `/tmp/iconout/ico_{16,32,48,64,128,256}.png` (see commit history for the stdlib packer) → `desktopApp/packaging/jrr.ico`
- **Linux**: `/tmp/iconout/linux_512.png` → `desktopApp/packaging/jrr.png`
- **iOS/macOS app icon**: `/tmp/iconout/appicon_1024.png` → `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/AppIcon.png`
- **Desktop window icon**: `/tmp/iconout/appicon_1024.png` → `desktopApp/src/jvmMain/resources/jrr_icon.png`
- **Android**: copy the `android_<density>_*.png` files into `androidApp/src/main/res/mipmap-<density>/`

The generator uses only macOS system frameworks (AppKit/CoreText) — no
ImageMagick or third-party rasteriser required.
