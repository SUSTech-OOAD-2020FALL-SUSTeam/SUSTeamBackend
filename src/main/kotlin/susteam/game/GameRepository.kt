package susteam.game

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.user.toUser
import java.lang.Exception
import java.sql.SQLIntegrityConstraintViolationException

class GameRepository @Inject constructor(private val database: JDBCClient) {

    suspend fun getGameID( game_name: String ): Int? {
        try {
            val game_id:Int? = database.querySingleWithParamsAwait(
                """SELECT game_id FROM game WHERE game_name = ?;"""
                    .trimIndent(),
                jsonArrayOf(game_name)
            )?.getInteger(0)
            return game_id
        }
        catch (e: SQLIntegrityConstraintViolationException) {
            throw e
        }
    }

    suspend fun create(game_name: String, version_name: String, price: Double, author_id: Int, publish_date: String, url: String ) {
        try {
            database.updateWithParamsAwait(
                """INSERT INTO game (game_name, price, author_id) VALUES (?, ?, ?);""",
                jsonArrayOf(game_name, price, author_id)
            )
            val game_id = getGameID(game_name)!!
            database.updateWithParamsAwait(
                """INSERT INTO gameVersion (game_id, version_name, url, publish_date) VALUES(?, ?, ?, ?);""",
                jsonArrayOf(game_id,version_name,url,publish_date)
            )
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun update(game_name:String, version_name: String, url: String, publish_date: String ) {
        try {
            val game_id = getGameID(game_name)!!
            database.updateWithParamsAwait(
                """INSERT INTO gameVersion (game_id, version_name, url, publish_date) VALUES(?, ?, ?, ?);""",
                jsonArrayOf(game_id,version_name,url, publish_date)
            )
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun get(game_name: String): Game? {
        return database.querySingleWithParamsAwait(
            """SELECT game_id, game_name, price, author_id, Description FROM game WHERE game_name = ?;""",
            jsonArrayOf(game_name)
        )?.let { Game(it.getInteger(0), it.getString(1), it.getDouble(2),
                    it.getInteger(4),it.getString(5)) }
    }

    suspend fun geturl( game_name: String, version_name: String ): String? {
        try {
            val game_id = getGameID(game_name)!!
            return database.querySingleWithParamsAwait(
                """SELECT url FROM gameVersion WHERE game_id == ? and version_name = ?;"""
                    .trimIndent(),
                jsonArrayOf(game_id,version_name)
            )?.getString(0)
        } catch (e: Exception) {
            throw e
        }
    }
}