import Foundation
import SharedLogic

// MARK: - Zone constants
//
// Kotlin exposes the Zone constants on the data class's `companion object`,
// which Swift sees as `Zone.companion.Offline` — verbose at every call site.
// Re-export them as plain static properties on Zone so Swift can write
// `Zone.offline` directly, matching Apple framework idioms.

extension Zone {
    public static let offline: Zone = Zone.companion.Offline
    public static let local: Zone = Zone.companion.Local
    public static let androidAuto: Zone = Zone.companion.AndroidAuto
}
