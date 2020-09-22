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

    suspend fun create(
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
                throw ServiceException("Cannot create game '$name', already exist", e)
            } else {
                throw e
            }
        }
    }

    suspend fun writeDescription(
            name: String,
            description: String?
    ) {
        try {
            database.updateWithParamsAwait(
                    """UPDATE game SET description=? WHERE name=?""",
                    jsonArrayOf(description, name)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            if (e.message?.contains("game.name") == true) {
                throw ServiceException("Game '$name' is not exist", e)
            } else {
                throw e
            }
        }
    }

    suspend fun createVersion(
            gameName: String,
            versionName: String,
            url: String
    ) {
        try {
            val game: Game? = get(gameName)
            if (game == null) {
                throw ServiceException("Cannot create version '$versionName' for game '$gameName' because game '$gameName' do not exist")
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

    suspend fun get(name: String): Game? {
        return database.querySingleWithParamsAwait(
                """SELECT game_id, name, price, publish_date, author, description FROM game WHERE name = ?;""",
                jsonArrayOf(name)
        )?.let {
            Game(
                    it.getInteger(0), it.getString(1), it.getInteger(2),
                    it.getInstant(3), it.getString(4), it.getString(5)
            )
        }
    }

    suspend fun getVersion(name: String): GameVersion? {
        return database.querySingleWithParamsAwait(
                """SELECT game_id, name, url FROM game_version WHERE name = ?;""",
                jsonArrayOf(name)
        )?.let {
            GameVersion(
                    it.getInteger(0), it.getString(1), it.getString(2)
            )
        }
    }

}