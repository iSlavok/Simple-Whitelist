package online.slavok.whitelist.database

import java.util.Locale

/**
 * Base for whitelist storage backends.
 *
 * Minecraft usernames are case-insensitively unique, so every public entry point
 * normalizes the nickname before delegating to the backend. This keeps behavior
 * consistent between storage types and fixes case-sensitive lookups (e.g. adding
 * "Steve" and logging in as "steve").
 */
abstract class DatabaseManager {

    protected fun normalize(nickname: String): String = nickname.trim().lowercase(Locale.ROOT)

    fun addPlayer(nickname: String): Boolean = add(normalize(nickname))
    fun removePlayer(nickname: String): Boolean = remove(normalize(nickname))
    fun inWhitelist(nickname: String): Boolean = contains(normalize(nickname))

    /** All whitelisted names (already normalized). */
    abstract fun getAll(): List<String>

    protected abstract fun add(nickname: String): Boolean
    protected abstract fun remove(nickname: String): Boolean
    protected abstract fun contains(nickname: String): Boolean
}
