package com.jrr.jrrkmp_native_ui.playback

import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase

import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository

object AudioPlayerFacadeFactory {
    fun create(
        database: JrrDatabase?,
        localPlayerEngine: LocalPlayerEngine,
        serverRepository: ServerRepository?,
        saveLastActiveZoneId: (String) -> Unit,
        loadLastActiveZoneId: () -> String?
    ): AudioPlayerFacade {
        return AudioPlayerFacade(
            database = database,
            localPlayerEngine = localPlayerEngine,
            serverRepository = serverRepository,
            saveLastActiveZoneId = saveLastActiveZoneId,
            loadLastActiveZoneId = loadLastActiveZoneId
        )
    }
}
