package online.slavok.whitelist.database

abstract class DatabaseManager {
    abstract fun getAll(): List<String>
    abstract fun addPlayer(nickname: String): Boolean
    abstract fun removePlayer(nickname: String): Boolean
    abstract fun inWhitelist(nickname: String): Boolean
}