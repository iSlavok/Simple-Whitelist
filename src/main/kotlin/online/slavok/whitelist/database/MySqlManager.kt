package online.slavok.whitelist.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection
import java.sql.DriverManager

class MySqlManager(
    private val url: String,
) : DatabaseManager() {
    private val dataSource = createHikariDataSource()

    private fun createHikariDataSource(): HikariDataSource {
        val config = HikariConfig()
        config.jdbcUrl = url
        config.maximumPoolSize = 10
        config.idleTimeout = 300000
        config.connectionTimeout = 5000
        return HikariDataSource(config)
    }
    init {
        getConnection().use { connection ->
            connection.prepareStatement("CREATE TABLE IF NOT EXISTS whitelist (nickname TEXT PRIMARY KEY);").use { ps ->
                ps.execute()
            }
        }
    }

    private fun getConnection(): Connection {
        return dataSource.connection
    }

    override fun getAll(): List<String> {
        val list: MutableList<String> = ArrayList()
        getConnection().use { connection ->
            connection.prepareStatement ("SELECT * FROM whitelist;").use { ps ->
                val rs = ps.executeQuery()
                while (rs.next()) {
                    list.add(rs.getString("nickname"))
                }
            }
        }
        return list
    }

    override fun addPlayer(nickname: String): Boolean {
        if (inWhitelist(nickname)) {
            return false
        }
        getConnection().use { connection ->
            connection.prepareStatement("INSERT INTO whitelist (nickname) VALUES (?);").use { ps ->
                ps.setString(1, nickname)
                ps.execute()
            }
        }
        return true
    }

    override fun removePlayer(nickname: String): Boolean {
        if (!inWhitelist(nickname)) {
            return false
        }
        getConnection().use { connection ->
            connection.prepareStatement("DELETE FROM whitelist WHERE nickname = ?;").use { ps ->
                ps.setString(1, nickname)
                ps.execute()
            }
        }
        return true
    }

    override fun inWhitelist(nickname: String): Boolean {
        getConnection().use { connection ->
            connection.prepareStatement("SELECT * FROM whitelist WHERE nickname = ?;").use { ps ->
                ps.setString(1, nickname)
                ps.executeQuery().use { rs ->
                    return rs.next()
                }
            }
        }
    }
}