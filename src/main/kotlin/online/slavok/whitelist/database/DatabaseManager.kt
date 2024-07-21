package online.slavok.whitelist.database

abstract class DatabaseManager {
    abstract var players: MutableList<String>

    abstract fun addPlayer(nickname: String): Boolean
    abstract fun removePlayer(nickname: String): Boolean
    fun checkPlayer(nickname: String): Boolean {
        return nickname in players
    }
}