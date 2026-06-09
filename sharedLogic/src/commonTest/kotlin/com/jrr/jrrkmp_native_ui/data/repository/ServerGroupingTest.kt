package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.data.db.entity.SavedServerEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class ServerGroupingTest {

    private fun srv(
        id: String,
        host: String,
        lastUsedAt: Long,
        serverId: String,
        friendlyName: String? = null,
    ) = SavedServerEntity(
        id = id,
        host = host,
        port = 52199,
        username = "u",
        passwordKey = "p",
        friendlyName = friendlyName,
        lastUsedAt = lastUsedAt,
        serverId = serverId,
    )

    @Test
    fun profilesSharingServerId_collapseIntoOneServer() {
        val servers = listOf(
            srv("1", "192.168.1.10", 100, serverId = "A", friendlyName = "Home"),
            srv("2", "wan.example.com", 200, serverId = "A", friendlyName = "Home"),
            srv("3", "10.0.0.5", 150, serverId = "B", friendlyName = "Office"),
        )
        val groups = groupServers(servers)
        assertEquals(2, groups.size)
        val home = groups.first { it.serverId == "A" }
        assertEquals(listOf("2", "1"), home.profiles.map { it.id }) // most-recent first
        assertEquals("Home", home.displayName)
    }

    @Test
    fun distinctServerIds_areDistinctServers() {
        val servers = listOf(
            srv("1", "a", 100, serverId = "X"),
            srv("2", "b", 200, serverId = "Y"),
            srv("3", "c", 50, serverId = "Z"),
        )
        val groups = groupServers(servers)
        assertEquals(3, groups.size)
        // Ordered by most-recently-used profile: Y(200) > X(100) > Z(50).
        assertEquals(listOf("Y", "X", "Z"), groups.map { it.serverId })
    }

    @Test
    fun displayName_fallsBackToHostWhenNoFriendlyName() {
        val groups = groupServers(listOf(srv("1", "myhost", 1, serverId = "A")))
        assertEquals("myhost", groups.single().displayName)
    }

    @Test
    fun emptyInput_yieldsNoGroups() {
        assertEquals(emptyList(), groupServers(emptyList()))
    }
}
