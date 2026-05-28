import Foundation
import SharedLogic

/// Lightweight Swift façade over Kermit's SKIE-exposed Logger. Use at file or
/// class scope:
///
/// ```swift
/// private let log = SwiftLog("ui:iOS:Library")
///
/// log.d("init")
/// log.e("loadTracks failed: \(error)")
/// ```
///
/// Logs land in the same pipeline as the Kotlin side (Logcat / OSLog +
/// in-memory ring buffer), so the "Share debug log" action surfaces them.
///
/// The message argument is a Swift `@autoclosure`, so below the severity
/// threshold the message is never evaluated — matching the lazy-message
/// guarantee of the Kotlin `log.d { … }` form.
///
/// Swift `Error` values are stringified into the message rather than bridged
/// to `KotlinThrowable` — Swift stack traces aren't useful in the Kotlin
/// throwable model, and we already preserve `error.localizedDescription`
/// in the log line.
struct SwiftLog {

    private let tag: String

    init(_ tag: String) {
        self.tag = tag
    }

    func v(_ msg: @autoclosure @escaping () -> String) {
        Logger.companion.v(throwable: nil, tag: tag, message: msg)
    }

    func d(_ msg: @autoclosure @escaping () -> String) {
        Logger.companion.d(throwable: nil, tag: tag, message: msg)
    }

    func i(_ msg: @autoclosure @escaping () -> String) {
        Logger.companion.i(throwable: nil, tag: tag, message: msg)
    }

    func w(_ msg: @autoclosure @escaping () -> String) {
        Logger.companion.w(throwable: nil, tag: tag, message: msg)
    }

    func e(_ msg: @autoclosure @escaping () -> String) {
        Logger.companion.e(throwable: nil, tag: tag, message: msg)
    }
}
