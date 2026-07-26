package online.slavok.whitelist

import online.slavok.whitelist.config.ConfigManager
import online.slavok.whitelist.database.JsonManager
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.nio.file.Path

class WhitelistListenerTest {

    private fun listener(dir: Path, whitelistOn: Boolean, stored: List<String>): WhitelistListener {
        val cfg = ConfigManager(File(dir.toFile(), "config.json"))
        cfg.setWhitelist(whitelistOn)
        val db = JsonManager(File(dir.toFile(), "whitelist.json"))
        stored.forEach { db.addPlayer(it) }
        return WhitelistListener(cfg, db)
    }

    private fun event(name: String): AsyncPlayerPreLoginEvent {
        val e = mock<AsyncPlayerPreLoginEvent>()
        whenever(e.name).thenReturn(name)
        return e
    }

    @Test
    fun `disallows a name that is not on the whitelist`(@TempDir dir: Path) {
        val e = event("Alex")
        listener(dir, whitelistOn = true, stored = listOf("Steve")).onPreLogin(e)
        verify(e).disallow(eq(Result.KICK_WHITELIST), any<String>())
    }

    @Test
    fun `allows a whitelisted name (exact case)`(@TempDir dir: Path) {
        val e = event("Steve")
        listener(dir, whitelistOn = true, stored = listOf("Steve")).onPreLogin(e)
        verify(e, never()).disallow(any<Result>(), any<String>())
    }

    @Test
    fun `case mismatch is disallowed (case-sensitive)`(@TempDir dir: Path) {
        val e = event("steve")
        listener(dir, whitelistOn = true, stored = listOf("Steve")).onPreLogin(e)
        verify(e).disallow(eq(Result.KICK_WHITELIST), any<String>())
    }

    @Test
    fun `whitelist off allows everyone`(@TempDir dir: Path) {
        val e = event("Alex")
        listener(dir, whitelistOn = false, stored = emptyList()).onPreLogin(e)
        verify(e, never()).disallow(any<Result>(), any<String>())
    }
}
