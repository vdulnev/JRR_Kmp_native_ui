import Foundation
import Observation
import SharedLogic

/// Bridges a Decompose `Value<T>` into a SwiftUI-observable property, mirroring
/// the `for await state in stateFlow` pattern used by the existing
/// `…Observable` wrappers (e.g. `MainShellObservable`, `NowPlayingObservable`).
///
/// Holds the current value and refreshes on every emission. Decompose's
/// `Value.subscribe` returns a `Cancellation`; mutating `value` triggers the
/// `@Observable` invalidation that re-renders any reading SwiftUI view.
///
/// Used in Phase 4 to drive `ContentView` from `RootComponent.stack` and the
/// inner `LibraryComponent.stack` / `PlayerComponent.stack`.
@Observable
@MainActor
final class ValueObservable<T: AnyObject> {
    private(set) var value: T
    @ObservationIgnored private let source: Value<T>
    @ObservationIgnored private var cancellable: Cancellation?

    init(_ source: Value<T>) {
        self.source = source
        self.value = source.value
        self.cancellable = source.subscribe { [weak self] newValue in
            self?.value = newValue
        }
    }

    deinit {
        cancellable?.cancel()
    }
}
