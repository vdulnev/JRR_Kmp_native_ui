import Foundation
import Observation
import SharedLogic

/// Minimal application-scoped container for the tvOS app. Builds the Room DB,
/// the MCWS networking stack, and the library repository. No facade / audio
/// engine yet — this milestone is connect + browse only.
@Observable
final class TvContainer {
    let database: JrrDatabase
    let serverRepository: ServerRepository
    let mcwsClient: McwsClient
    let libraryRepository: LibraryRepository

    init() {
        let builder = DatabaseBuilder().createBuilder()
        let db = DatabaseBuilderKt.createDatabase(builder: builder)
        database = db

        let core = McwsCore.companion.create(database: db)
        serverRepository = core.serverRepository
        mcwsClient = core.mcwsClient

        libraryRepository = LibraryRepository(
            database: db,
            mcwsClient: core.mcwsClient,
            isOfflineProvider: AlwaysOnlineProvider()
        )
    }
}

/// tvOS is online-only, so the library always serves from MCWS, never a
/// downloaded-tracks cache.
private final class AlwaysOnlineProvider: OfflineModeProvider {
    func isOffline() -> Bool { false }
}
