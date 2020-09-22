package susteam.game

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant

class GameRepository @Inject constructor(private val database: JDBCClient) {

    suspend fun createGame(
        name: String,
        price: Int,
        publishDate: Instant,
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

}