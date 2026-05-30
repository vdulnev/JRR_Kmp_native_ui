import Foundation
import SharedLogic

// MARK: - Zone constants

//
// Kotlin exposes the Zone constants on the data class's `companion object`,
// which Swift sees as `Zone.companion.Offline` — verbose at every call site.
// Re-export them as plain static properties on Zone so Swift can write
// `Zone.offline` directly, matching Apple framework idioms.
//
// SKIE 0.10.x does not auto-flatten companion-object members onto the type,
// so this extension is still required despite the migration.

public extension Zone {
    static let offline: Zone = .companion.Offline
    static let local: Zone = .companion.Local
    static let androidAuto: Zone = .companion.AndroidAuto
}
