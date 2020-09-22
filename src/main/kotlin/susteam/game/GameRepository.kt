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
                throw e
        }
    }

    suspend fun updateDescription(
            gameId: Int,
            description: String?
    ) {
        try {
            database.updateWithParamsAwait(
                    """UPDATE game SET description=? WHERE id=?""",
                    jsonArrayOf(description, gameId)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            if (e.message?.contains("game.id") == true) {
                throw ServiceException("Game '$gameId' is not exist", e)
            } else {
                throw e
            }
        }
    }

    suspend fun createVersion(
            gameId: Int,
            versionName: String,
            url: String
    ) {
        try {
            val game: Game? = getById(gameId)
            if (game == null) {
                throw ServiceException("Cannot create version '$versionName' for game '$gameId' because game '$gameId' do not exist")
            }
            database.updateWithParamsAwait(
                    """INSERT INTO game_version (game_id, name, url) VALUES (?, ?, ?);""",
                    jsonArrayOf(game.id, versionName, url)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            if (e.message?.contains("game_version.name") == true || e.message?.contains("game_version.url") == true) {
                throw ServiceException("Cannot create game version '$versionName', already exist", e)
            } else {
                throw e
            }
        }
    }

    suspend fun getById(id: Int): Game? {
        return database.querySingleWithParamsAwait(
                """SELECT game_id, name, price, publish_date, author, description FROM game WHERE name = ?;""",
                jsonArrayOf(id)
        )?.let {
            Game(
                    it.getInteger(0), it.getString(1), it.getInteger(2),
                    it.getInstant(3), it.getString(4), it.getString(5)
            )
        }
    }

    suspend fun getVersion(gameId: Int, versionName: String): GameVersion? {
        val game: Game? = getById(gameId)
        if (game != null) {
            return database.querySingleWithParamsAwait(
                    """SELECT game_id, name, url FROM game_version WHERE name = ? and game_id = ?;""",
                    jsonArrayOf(versionName, gameId)
            )?.let {
                GameVersion(
                        it.getInteger(0), it.getString(1), it.getString(2)
                )
            }
        } else {
            throw ServiceException("Game not found")
        }
    }

}