package online.slavok.whitelist.database

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import online.slavok.whitelist.SimpleWhitelist
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * JSON-file backed whitelist.
 *
 * Login checks run on the network thread while commands run on the server thread,
 * so all access is guarded by [lock] and writes are atomic (temp file + move) to
 * avoid corrupting the file on a partial write.
 */
class JsonManager(
    private val file: File,
) : DatabaseManager() {
    private val lock = Any()
    private val players: MutableSet<String>

    init {
        players = synchronized(lock) {
            if (file.exists()) {
                try {
                    Json.decodeFromString<List<String>>(file.readText())
                        .mapTo(LinkedHashSet()) { normalize(it) }
                } catch (e: Exception) {
                    SimpleWhitelist.logger.warn("Error loading whitelist file, starting empty: ${e.message}")
                    LinkedHashSet()
                }
            } else {
                LinkedHashSet()
            }
        }
    }

    override fun getAll(): List<String> = synchronized(lock) { players.toList() }

    override fun add(nickname: String): Boolean = synchronized(lock) {
        if (!players.add(nickname)) return false
        savePlayers()
        true
    }

    override fun remove(nickname: String): Boolean = synchronized(lock) {
        if (!players.remove(nickname)) return false
        savePlayers()
        true
    }

    override fun contains(nickname: String): Boolean = synchronized(lock) { players.contains(nickname) }

    private fun savePlayers() {
        val json = Json.encodeToString(players.toList())
        val tmp = File(file.parentFile, "${file.name}.tmp")
        tmp.writeText(json)
        Files.move(
            tmp.toPath(), file.toPath(),
            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE,
        )
    }
}
