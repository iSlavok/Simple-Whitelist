package online.slavok.whitelist.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ConfigManagerTest {

    @TempDir
    lateinit var dir: Path

    private fun configFile(name: String = "config.json") = File(dir.toFile(), name)

    @Test
    fun `writes default config when absent`() {
        val file = configFile()
        val cfg = ConfigManager(file).config
        assertTrue(file.exists())
        // defaults: whitelist on, json backend
        assertTrue(cfg.whitelist)
        assertEquals("json", cfg.databaseType)
    }

    @Test
    fun `setWhitelist persists across reloads`() {
        val file = configFile()
        ConfigManager(file).setWhitelist(false)
        // a fresh instance must read the persisted value, not the default
        assertFalse(ConfigManager(file).config.whitelist)
    }

    @Test
    fun `loads existing values instead of defaults`() {
        val file = configFile()
        file.writeText(
            """{"whitelist":false,"databaseType":"mysql","mysqlUrl":"jdbc:x","jsonFileName":"w.json"}"""
        )
        val cfg = ConfigManager(file).config
        assertFalse(cfg.whitelist)
        assertEquals("mysql", cfg.databaseType)
    }

    @Test
    fun `corrupt config falls back to defaults`() {
        val file = configFile()
        file.writeText("}{ not json")
        val cfg = ConfigManager(file).config
        // negative control: a corrupt file must NOT leave whitelist disabled/undefined
        assertTrue(cfg.whitelist)
        assertEquals("json", cfg.databaseType)
    }
}
