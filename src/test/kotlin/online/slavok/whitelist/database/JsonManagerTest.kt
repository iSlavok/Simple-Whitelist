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
    fun `lookup is case-sensitive`() {
        val db = manager()
        db.addPlayer("Steve")
        // positive control: exact case is found
        assertTrue(db.inWhitelist("Steve"))
        // a different case is a different player and is not on the list
        assertFalse(db.inWhitelist("steve"))
        assertFalse(db.inWhitelist("STEVE"))
    }

    @Test
    fun `different case is a distinct player`() {
        val db = manager()
        assertTrue(db.addPlayer("Steve"))
        // adding a different case is a new, separate entry (not a no-op)
        assertTrue(db.addPlayer("steve"))
        assertEquals(2, db.getAll().size)
        assertTrue(db.inWhitelist("Steve"))
        assertTrue(db.inWhitelist("steve"))
    }

    @Test
    fun `remove only matches the exact case`() {
        val db = manager()
        db.addPlayer("Steve")
        // negative control: removing a different case removes nothing
        assertFalse(db.removePlayer("STEVE"))
        assertTrue(db.inWhitelist("Steve"))
        // the exact case removes it
        assertTrue(db.removePlayer("Steve"))
        assertFalse(db.inWhitelist("Steve"))
    }

    @Test
    fun `data persists across reloads`() {
        val file = File(dir.toFile(), "persist.json")
        JsonManager(file).addPlayer("Steve")
        // a fresh instance backed by the same file must see the stored name (exact case)
        assertTrue(JsonManager(file).inWhitelist("Steve"))
        assertFalse(JsonManager(file).inWhitelist("steve"))
    }

    @Test
    fun `names are trimmed but keep their case`() {
        val db = manager()
        db.addPlayer("  SteVe  ")
        assertEquals(listOf("SteVe"), db.getAll())
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
