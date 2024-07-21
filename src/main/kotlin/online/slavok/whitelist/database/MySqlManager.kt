package online.slavok.whitelist.database

import java.sql.Connection
import java.sql.DriverManager

class MySqlManager(
    private val url: String,
) : DatabaseManager() {
    override var players: MutableList<String> = mutableListOf()

    init {
        val createTableQuery = """
            CREATE TABLE IF NOT EXISTS whitelist (
                nickname TEXT unique
            )
        """
        val connection = getConnection()
        connection.createStatement().execute(createTableQuery)
        val preparedStatement = connection.createStatement()
        val resultSet = preparedStatement.executeQuery("SELECT nickname FROM whitelist")
        while (resultSet.next()) {
            players.add(resultSet.getString("nickname"))
        }
        connection.close()
    }

    private fun getConnection(): Connection {
        return DriverManager.getConnection(url)
    }

    override fun addPlayer(nickname: String): Boolean {
        if (checkPlayer(nickname)) {
            return false
        }
        val insertQuery = "INSERT INTO whitelist(nickname) VALUES (?)"
        val preparedStatement = getConnection().prepareStatement(insertQuery)
        preparedStatement.setString(1, nickname)
        preparedStatement.executeUpdate()
        players.add(nickname)
        return true
    }

    override fun removePlayer(nickname: String): Boolean {
        if (!checkPlayer(nickname)) {
            return false
        }
        val deleteQuery = "DELETE FROM whitelist WHERE nickname = ?"
        val preparedStatement = getConnection().prepareStatement(deleteQuery)
        preparedStatement.setString(1, nickname)
        preparedStatement.executeUpdate()
        players.remove(nickname)
        return true
    }
}