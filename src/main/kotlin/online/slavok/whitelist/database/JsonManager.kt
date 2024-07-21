package online.slavok.whitelist.database

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader
import java.io.FileWriter


class JsonManager(
    private val file: File
) : DatabaseManager() {
    override var players: MutableList<String>

    init {
        if (file.exists()) {
            val reader = FileReader(file)
            val content = reader.readText()
            reader.close()
            players = try {
                Json.decodeFromString(content)
            } catch (e: Exception) {
                mutableListOf()
            }
        }
        players = mutableListOf()
    }

    private fun savePlayers() {
        val writer = FileWriter(file)
        val json = Json.encodeToString(players)
        writer.write(json)
        writer.flush()
        writer.close()
    }

    override fun addPlayer(nickname: String): Boolean {
        if (checkPlayer(nickname)) {
            return false
        }
        players.add(nickname)
        savePlayers()
        return true
    }

    override fun removePlayer(nickname: String): Boolean {
        if (!checkPlayer(nickname)) {
            return false
        }
        players.remove(nickname)
        savePlayers()
        return true
    }
}