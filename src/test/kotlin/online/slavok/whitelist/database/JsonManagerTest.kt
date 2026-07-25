package online.slavok.whitelist.database

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class JsonManagerTest {

    @TempDir
    lateinit var dir: Path

    private fun manager(name: String = "whitelist.json") = JsonManager(File(dir.toFile(), name))

    @Test
    fun `add then contains`() {
        val db = manager()
        assertTrue(db.addPlayer("Steve"))
        assertTrue(db.inWhitelist("Steve"))
        // negative control: a name that was never added must not be present
        assertFalse(db.inWhitelist("Alex"))
    }

    @Test
    fun `lookup is case-insensitive`() {
        val db = manager()
        db.addPlayer("Steve")
        assertTrue(db.inWhitelist("steve"))
        assertTrue(db.inWhitelist("STEVE"))
        // negative control: a different name in any case is still absent
        assertFalse(db.inWhitelist("steves"))
    }

    @Test
    fun `add is idempotent regardless of case`() {
        val db = manager()
        assertTrue(db.addPlayer("Steve"))
        // adding the same name in different case is a no-op, reported as false
        assertFalse(db.addPlayer("steve"))
        assertEquals(1, db.getAll().size)
    }

    @Test
    fun `remove only succeeds for present names`() {
        val db = manager()
        db.addPlayer("Steve")
        assertTrue(db.removePlayer("STEVE"))
        assertFalse(db.inWhitelist("Steve"))
        // negative control: removing an absent name reports false
        assertFalse(db.removePlayer("Steve"))
    }

    @Test
    fun `data persists across reloads`() {
        val file = File(dir.toFile(), "persist.json")
        JsonManager(file).addPlayer("Steve")
        // a fresh instance backed by the same file must see the stored name
        assertTrue(JsonManager(file).inWhitelist("steve"))
    }

    @Test
    fun `names are stored normalized`() {
        val db = manager()
        db.addPlayer("  SteVe  ")
        assertEquals(listOf("steve"), db.getAll())
    }

    @Test
    fun `corrupt file recovers to empty whitelist`() {
        val file = File(dir.toFile(), "corrupt.json")
        file.writeText("}{ not json")
        val db = JsonManager(file)
        assertTrue(db.getAll().isEmpty())
        // and it is still usable after recovery
        assertTrue(db.addPlayer("Steve"))
        assertTrue(db.inWhitelist("Steve"))
    }
}
