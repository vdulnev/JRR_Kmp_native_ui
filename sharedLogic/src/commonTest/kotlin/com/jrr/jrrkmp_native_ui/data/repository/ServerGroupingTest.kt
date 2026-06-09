package com.jrr.jrrkmp_native_ui.data.repository

import com.jrr.jrrkmp_native_ui.data.db.entity.SavedServerEntity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ServerGroupingTest {

    private fun srv(
        id: String,
        host: String,
        lastUsedAt: Long,
        group: String? = null,
    ) = SavedServerEntity(
        id = id,
        host = host,
        port = 52199,
        username = "u",
        passwordKey = "p",
        friendlyName = host,
        lastUsedAt = lastUsedAt,
        groupName = group,
    )

    @Test
    fun groups_profilesSharingAName() {
        val servers = listOf(
            srv("1", "192.168.1.10", 100, group = "Home"),
            srv("2", "wan.example.com", 200, group = "Home"),
            srv("3", "10.0.0.5", 150, group = "Office"),
        )
        val groups = groupServers(servers)
        assertEquals(2, groups.size)
        val home = groups.first { it.name == "Home" }
        assertEquals(listOf("2", "1"), home.profiles.map { it.id }) // most-recent first
    }

    @Test
    fun ungrouped_profilesBecomeSingletonGroups() {
        val servers = listOf(
            srv("1", "a", 100, group = "Home"),
            srv("2", "b", 200, group = null),
            srv("3", "c", 50, group = null),
        )
        val groups = groupServers(servers)
        assertEquals(3, groups.size)
        // Ordered by most-recently-used profile: b(200) > Home(100) > c(50).
        assertNull(groups[0].name)
        assertEquals("b", groups[0].profiles.single().host)
        assertEquals("Home", groups[1].name)
        assertNull(groups[2].name)
        assertEquals("c", groups[2].profiles.single().host)
    }

    @Test
    fun emptyInput_yieldsNoGroups() {
        assertEquals(emptyList(), groupServers(emptyList()))
    }
}
