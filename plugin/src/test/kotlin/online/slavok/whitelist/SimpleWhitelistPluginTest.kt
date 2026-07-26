package online.slavok.whitelist

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.bukkit.permissions.PermissionDefault
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class SimpleWhitelistPluginTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: SimpleWhitelistPlugin

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(SimpleWhitelistPlugin::class.java)
    }

    @AfterEach
    fun tearDown() = MockBukkit.unmock()

    @Test
    fun `plugin enables and initializes managers`() {
        assertTrue(plugin.isEnabled)
        assertNotNull(plugin.databaseManager)
        assertNotNull(plugin.configManager)
        // default backend is JSON, whitelist on
        assertTrue(plugin.configManager.config.whitelist)
        assertEquals("json", plugin.configManager.config.databaseType)
    }

    @Test
    fun `command and aliases are registered with an op-default permission`() {
        val cmd = plugin.getCommand("simplewhitelist")
        assertNotNull(cmd)
        assertEquals("simplewhitelist.command", cmd!!.permission)
        assertTrue(cmd.aliases.containsAll(listOf("swl", "swh", "swhitelist")))

        // Assert the gate we actually declare (plugin.yml default: op); a real server
        // grants this to ops, which is not what MockBukkit's permission model simulates.
        val perm = server.pluginManager.getPermission("simplewhitelist.command")
        assertNotNull(perm)
        assertEquals(PermissionDefault.OP, perm!!.default)
    }
}
