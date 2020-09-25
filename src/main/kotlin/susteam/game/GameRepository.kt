package susteam.game

import com.google.inject.Inject
import io.vertx.core.json.JsonObject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.ResultSet
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant
import java.time.LocalDateTime

class GameRepository @Inject constructor(private val database: JDBCClient) {

    suspend fun createGame(
            name: String,
            price: Int,
            publishDate: LocalDateTime,
            author: String,
            description: String?
    ): Int {
        try {
            return database.updateWithParamsAwait(
                    """INSERT INTO game (name, price, publish_date, author, description) VALUES (?, ?, ?, ?, ?);""",
                    jsonArrayOf(name, price, publishDate, author, description)
            ).keys.getInteger(0)
        } catch (e: SQLIntegrityConstraintViolationException) {
            if (e.message?.contains("game.name") == true) {
                throw ServiceException("Cannot create game '$name'", e)
            } else {
                throw e
            }
        }
    }

    suspend fun updateDescription(
            gameId: Int,
            description: String?
    ): Boolean {
        return database.updateWithParamsAwait(
                """UPDATE game SET description = ? WHERE game_id = ?;""",
                jsonArrayOf(description, gameId)
        ).updated == 1
    }

    suspend fun createVersion(
            gameId: Int,
            versionName: String,
            url: String
    ) {
        try {
            database.updateWithParamsAwait(
                    """INSERT INTO game_version (game_id, name, url) VALUES (?, ?, ?);""",
                    jsonArrayOf(gameId, versionName, url)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            val message = e.message ?: throw e

            if (message.contains("FOREIGN KEY (`game_id`)")) {
                throw ServiceException("Cannot create version '$versionName' for game '$gameId', game '$gameId' do not exist")
            } else if (message.contains("game_version.PRIMARY")) {
                throw ServiceException("Cannot create version '$versionName', version name already exist", e)
            } else if (message.contains("game_version.url")) {
                throw ServiceException("Cannot create version '$versionName', version url already exist", e)
            } else {
                throw e
            }
        }
    }

    suspend fun getById(id: Int): Game? {
        return database.querySingleWithParamsAwait(
                """SELECT game_id, name, price, publish_date, author, description FROM game WHERE game_id = ?;""",
                jsonArrayOf(id)
        )?.let {
            Game(
                    it.getInteger(0), it.getString(1), it.getInteger(2),
                    it.getInstant(3), it.getString(4), it.getString(5)
            )
        }
    }

    suspend fun getVersion(gameId: Int, versionName: String): GameVersion? {
        return database.querySingleWithParamsAwait(
                """SELECT game_id, name, url FROM game_version WHERE name = ? AND game_id = ?;""",
                jsonArrayOf(versionName, gameId)
        )?.let {
            GameVersion(
                    it.getInteger(0), it.getString(1), it.getString(2)
            )
        }
    }

    suspend fun getAllGamesOrderByPublishDate(): ArrayList<Game>? {
        val gameList: ArrayList<Game>? = ArrayList()
        val resultSet: ResultSet? = database.queryAwait(
                """SELECT game_id gameId, name, price, publish_date publishDate, author, description FROM game ORDER BY publish_date desc;"""
        )
        val allGames: ArrayList<JsonObject>? = resultSet?.getRows() as ArrayList<JsonObject>?
        if (allGames != null) {
            for (i in allGames)
                if (gameList != null) {
                    gameList.add(i.toGame())
                }
        }
        return gameList
    }

    suspend fun getAllGames(): ArrayList<Game>? {
        val gameList: ArrayList<Game>? = ArrayList()
        val resultSet: ResultSet? = database.queryAwait(
                """SELECT game_id gameId, name, price, publish_date publishDate, author, description FROM game;"""
        )
        val allGames: ArrayList<JsonObject>? = resultSet?.getRows() as ArrayList<JsonObject>?
        if (allGames != null) {
            for (i in allGames)
                if (gameList != null) {
                    gameList.add(i.toGame())
                }
        }
        return gameList
    }

    suspend fun getRandomGames(numberOfGames: Int): ArrayList<Game>? {
        val gameList: ArrayList<Game>? = ArrayList()
        val resultSet: ResultSet? = database.queryWithParamsAwait(
                """SELECT game_id gameId, name, price, publish_date publishDate, author, description FROM game ORDER BY rand() LIMIT ?;""", jsonArrayOf(numberOfGames)
        )
        val randomGames: ArrayList<JsonObject>? = resultSet?.getRows() as ArrayList<JsonObject>?
        if (randomGames != null) {
            for (i in randomGames)
                if (gameList != null) {
                    gameList.add(i.toGame())
                }
        }
        return gameList
    }

}