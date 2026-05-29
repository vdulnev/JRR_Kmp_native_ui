import Foundation
import Observation
import SharedLogic

private let log = SwiftLog("ui:iOS:RootNav")

/// Bridges Decompose `Value<ChildStack<…>>` into SwiftUI-observable state.
///
/// SKIE surfaces `ChildStack<C, T>` as a two-parameter generic whose erased
/// config/child are existential protocols — awkward to name in a stored
/// property. These purpose-built observers sidestep that by only ever touching
/// the generic *inside* the `subscribe` closure (where Swift infers it) and
/// exposing the active config/child (and the live tab set) as plain
/// existentials. This is the "concrete accessor" escape hatch the migration
/// plan calls for (§6).
///
/// Decompose's `Value.subscribe` returns a `Cancellation`; mutating an
/// `@Observable` property invalidates any SwiftUI view reading it.

@Observable
@MainActor
final class RootStackObservable {
    private(set) var activeConfig: RootConfig
    private(set) var activeChild: RootComponentRootChild
    /// Every live tab/leaf instance (active + back stack). Lets the host pull a
    /// specific tab's component so its SwiftUI subtree keeps a stable identity
    /// across tab switches instead of being torn down and rebuilt.
    private(set) var children: [RootComponentRootChild]

    @ObservationIgnored private var cancellable: Cancellation?

    init(_ root: RootComponent) {
        let stack = root.stack.value
        self.activeConfig = stack.active.configuration
        self.activeChild = stack.active.instance
        self.children = stack.items.map { $0.instance }
        self.cancellable = root.stack.subscribe { [weak self] newStack in
            self?.activeConfig = newStack.active.configuration
            self?.activeChild = newStack.active.instance
            self?.children = newStack.items.map { $0.instance }
            log.d("stack -> active=\(newStack.active.configuration) depth=\(newStack.items.count)")
        }
    }

    deinit { cancellable?.cancel() }
}

@Observable
@MainActor
final class LibraryStackObservable {
    private(set) var activeChild: LibraryComponentChild
    /// List + (optional) Detail entries. Exposing the whole set lets the host
    /// keep `LibraryView` mounted under an overlaid `AlbumDetailView` — matching
    /// the pre-Decompose opacity-crossfade so back navigation doesn't rebuild
    /// the list (and lose its scroll position).
    private(set) var children: [LibraryComponentChild]
    @ObservationIgnored private var cancellable: Cancellation?

    init(_ component: LibraryComponent) {
        self.activeChild = component.stack.value.active.instance
        self.children = component.stack.value.items.map { $0.instance }
        self.cancellable = component.stack.subscribe { [weak self] newStack in
            self?.activeChild = newStack.active.instance
            self?.children = newStack.items.map { $0.instance }
        }
    }

    deinit { cancellable?.cancel() }
}

@Observable
@MainActor
final class PlayerStackObservable {
    private(set) var activeChild: PlayerComponentChild
    @ObservationIgnored private var cancellable: Cancellation?

    init(_ component: PlayerComponent) {
        self.activeChild = component.stack.value.active.instance
        self.cancellable = component.stack.subscribe { [weak self] newStack in
            self?.activeChild = newStack.active.instance
        }
    }

    deinit { cancellable?.cancel() }
}
