package online.slavok.whitelist.database

/**
 * Base for whitelist storage backends.
 *
 * The whitelist is case-sensitive: names that differ only in case are treated as
 * distinct players and stored separately. Every public entry point trims
 * surrounding whitespace (Minecraft names can't contain spaces) but preserves the
 * exact case before delegating to the backend.
 */
abstract class DatabaseManager {

    protected fun normalize(nickname: String): String = nickname.trim()

    fun addPlayer(nickname: String): Boolean = add(normalize(nickname))
    fun removePlayer(nickname: String): Boolean = remove(normalize(nickname))
    fun inWhitelist(nickname: String): Boolean = contains(normalize(nickname))

    /** All whitelisted names, exactly as stored (case preserved). */
    abstract fun getAll(): List<String>

    protected abstract fun add(nickname: String): Boolean
    protected abstract fun remove(nickname: String): Boolean
    protected abstract fun contains(nickname: String): Boolean
}
