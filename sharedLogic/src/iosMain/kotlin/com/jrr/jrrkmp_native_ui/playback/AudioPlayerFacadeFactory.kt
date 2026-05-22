package com.jrr.jrrkmp_native_ui.playback

import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase

object AudioPlayerFacadeFactory {
    fun create(
        database: JrrDatabase?,
        localPlayerEngine: LocalPlayerEngine,
        saveLastActiveZoneId: (String) -> Unit,
        loadLastActiveZoneId: () -> String?
    ): AudioPlayerFacade {
        return AudioPlayerFacade(
            database = database,
            localPlayerEngine = localPlayerEngine,
            saveLastActiveZoneId = saveLastActiveZoneId,
            loadLastActiveZoneId = loadLastActiveZoneId
        )
    }
}
