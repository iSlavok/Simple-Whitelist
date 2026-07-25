package online.slavok.whitelist.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection

class MySqlManager(
    private val url: String,
) : DatabaseManager() {
    private val dataSource = createHikariDataSource()

    private fun createHikariDataSource(): HikariDataSource {
        val config = HikariConfig()
        config.jdbcUrl = url
        config.maximumPoolSize = 10
        config.connectionTimeout = 5000
        return HikariDataSource(config)
    }

    init {
        getConnection().use { connection ->
            // VARCHAR(16) = max Minecraft username length; TEXT can't be a key without a prefix length.
            // utf8mb4_bin is a binary collation, so lookups and the primary key are case-sensitive
            // ("Steve" and "steve" are distinct rows). MySQL's default collation is case-insensitive.
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS whitelist " +
                    "(nickname VARCHAR(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin PRIMARY KEY);"
            ).use { ps ->
                ps.execute()
            }
        }
    }

    private fun getConnection(): Connection = dataSource.connection

    override fun getAll(): List<String> {
        val list: MutableList<String> = ArrayList()
        getConnection().use { connection ->
            connection.prepareStatement("SELECT nickname FROM whitelist;").use { ps ->
                ps.executeQuery().use { rs ->
                    while (rs.next()) {
                        list.add(rs.getString("nickname"))
                    }
                }
            }
        }
        return list
    }

    override fun add(nickname: String): Boolean {
        if (contains(nickname)) {
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

    override fun remove(nickname: String): Boolean {
        getConnection().use { connection ->
            connection.prepareStatement("DELETE FROM whitelist WHERE nickname = ?;").use { ps ->
                ps.setString(1, nickname)
                return ps.executeUpdate() > 0
            }
        }
    }

    override fun contains(nickname: String): Boolean {
        getConnection().use { connection ->
            connection.prepareStatement("SELECT 1 FROM whitelist WHERE nickname = ?;").use { ps ->
                ps.setString(1, nickname)
                ps.executeQuery().use { rs ->
                    return rs.next()
                }
            }
        }
    }
}
