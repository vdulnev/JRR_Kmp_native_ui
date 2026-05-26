package com.jrr.jrrkmp_native_ui.data.api

import com.jrr.jrrkmp_native_ui.data.db.JrrDatabase
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository

/**
 * Bundle of the MCWS networking stack: the shared [McwsClient] for server-bound
 * calls and the [ServerRepository] that owns the active-server state. Both
 * share the same underlying ktor [io.ktor.client.HttpClient], which is kept as
 * an implementation detail (not exposed across module boundaries so that
 * platform apps don't need ktor on their compile classpath).
 *
 * Construct with [create] from the platform DI / `AppContainer`.
 */
class McwsCore private constructor(
    val mcwsClient: McwsClient,
    val serverRepository: ServerRepository,
) {
    companion object {
        fun create(database: JrrDatabase): McwsCore {
            val httpClient = createMcwsHttpClient()
            val serverRepository = ServerRepository(
                database = database,
                httpClient = httpClient,
            )
            val mcwsClient = McwsClient(
                httpClient = httpClient,
                activeServerFlow = serverRepository.activeServer,
            )
            return McwsCore(mcwsClient = mcwsClient, serverRepository = serverRepository)
        }
    }
}
