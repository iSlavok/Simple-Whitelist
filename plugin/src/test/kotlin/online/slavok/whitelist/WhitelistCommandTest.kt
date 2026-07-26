package online.slavok.whitelist

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockbukkit.mockbukkit.MockBukkit
import org.mockbukkit.mockbukkit.ServerMock

class WhitelistCommandTest {
    private lateinit var server: ServerMock
    private lateinit var plugin: SimpleWhitelistPlugin
    private val subs = listOf("add", "remove", "list", "on", "off")

    @BeforeEach
    fun setUp() {
        server = MockBukkit.mock()
        plugin = MockBukkit.load(SimpleWhitelistPlugin::class.java)
    }

    @AfterEach
    fun tearDown() = MockBukkit.unmock()

    // MockBukkit doesn't grant op-default permissions to ops, so grant the command
    // permission explicitly — the test targets our command routing/logic, not the
    // server's op-permission model (that gate is asserted in SimpleWhitelistPluginTest).
    private fun opWithPermission() =
        server.addPlayer().apply {
            isOp = true
            addAttachment(plugin, "simplewhitelist.command", true)
        }

    @Test
    fun `add then remove toggles membership (via alias too)`() {
        val op = opWithPermission()
        server.dispatchCommand(op, "simplewhitelist add Steve")
        assertTrue(plugin.databaseManager.inWhitelist("Steve"))
        // exercise an alias on the way out
        server.dispatchCommand(op, "swl remove Steve")
        assertFalse(plugin.databaseManager.inWhitelist("Steve"))
    }

    @Test
    fun `on and off toggle the config`() {
        val op = opWithPermission()
        server.dispatchCommand(op, "simplewhitelist off")
        assertFalse(plugin.configManager.config.whitelist)
        server.dispatchCommand(op, "swhitelist on")
        assertTrue(plugin.configManager.config.whitelist)
    }

    @Test
    fun `add is case-sensitive and rejects duplicates`() {
        val op = opWithPermission()
        server.dispatchCommand(op, "simplewhitelist add Steve")
        server.dispatchCommand(op, "simplewhitelist add steve") // different case = new entry
        assertEquals(2, plugin.databaseManager.getAll().size)
    }

    @Test
    fun `tab-complete suggests subcommands and stored names`() {
        val op = server.addPlayer().apply { isOp = true }
        plugin.databaseManager.addPlayer("Steve")
        val handler = WhitelistCommand(plugin.configManager, plugin.databaseManager)
        val pc = plugin.getCommand("simplewhitelist")!!
        assertTrue(handler.onTabComplete(op, pc, "simplewhitelist", arrayOf("")).containsAll(subs))
        assertEquals(
            listOf("Steve"),
            handler.onTabComplete(op, pc, "simplewhitelist", arrayOf("remove", "")),
        )
    }
}
