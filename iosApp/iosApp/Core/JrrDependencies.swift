import Foundation
import SharedLogic

class JrrDependencies {
    static let shared = JrrDependencies()
    
    private(set) var database: JrrDatabase
    private(set) var localPlayerEngine: IosLocalPlayerEngine
    private(set) var corePlayer: CorePlayer
    private(set) var facade: AudioPlayerFacade
    private(set) var serverRepository: ServerRepository
    private(set) var libraryRepository: LibraryRepository
    
    private init() {
        let builder = DatabaseBuilder().createBuilder()
        let db = DatabaseBuilderKt.createDatabase(builder: builder)
        self.database = db
        
        let engine = IosLocalPlayerEngine()
        self.localPlayerEngine = engine
        
        let facadeInstance = AudioPlayerFacadeFactory.shared.create(
            database: db,
            localPlayerEngine: engine,
            saveLastActiveZoneId: { zoneId in
                UserDefaults.standard.set(zoneId, forKey: "last_active_zone_id")
            },
            loadLastActiveZoneId: {
                UserDefaults.standard.string(forKey: "last_active_zone_id")
            }
        )
        self.facade = facadeInstance
        
        self.corePlayer = CorePlayer(
            engine: engine,
            database: db
        )
        
        self.serverRepository = ServerRepository(database: db)
        
        self.libraryRepository = LibraryRepository(
            database: db,
            isOfflineProvider: {
                let zone = facadeInstance.activeZone.value as? Zone
                return KotlinBoolean(value: zone == Zone.companion.Offline)
            }
        )
    }
}
