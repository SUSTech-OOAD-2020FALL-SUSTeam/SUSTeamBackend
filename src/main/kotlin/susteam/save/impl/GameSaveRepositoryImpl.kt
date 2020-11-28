package susteam.save.impl

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.save.GameSave
import susteam.save.GameSaveRepository
import susteam.save.toGameSave
import susteam.storage.getStorageFile
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT

class GameSaveRepositoryImpl @Inject constructor(private val database: JDBCClient) : GameSaveRepository {

    override suspend fun getAllGameSaveName(username: String, gameId: Int): List<GameSave> {
        return database.queryWithParamsAwait(
            """
                SELECT
                       username,
                       game_id gameId,
                       save_name saveName,
                       saved_time savedTime,
                       url
                FROM game_save 
                WHERE username = ? and game_id = ?
                ORDER BY saved_time DESC;
            """.trimIndent(),
            jsonArrayOf(username, gameId)
        ).rows.map { it.toGameSave() }
    }

    override suspend fun uploadGameSave(
        username: String,
        gameId: Int,
        saveName: String,
        savedTime: Instant,
        url: String
    ) {
        try {
            database.updateWithParamsAwait(
                """
                INSERT INTO game_save (username, game_id, save_name, saved_time, url) 
                    VALUE (?, ?, ?, ?, ?);
            """.trimIndent(),
                jsonArrayOf(username, gameId, saveName, ISO_INSTANT.format(savedTime), url)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            if (e.message?.contains("save_name") == true) {
                throw ServiceException("Cannot upload game save '$saveName'", e)
            } else {
                throw e
            }
        }
    }

    override suspend fun deleteGameSave(username: String, gameId: Int, saveName: String) {
        database.updateWithParamsAwait(
            """DELETE FROM game_save WHERE username = ? and game_id = ? and save_name = ?;""",
            jsonArrayOf(username, gameId, saveName)
        )
    }

    override suspend fun getGameSave(username: String, gameId: Int, saveName: String): GameSave? {
        return database.querySingleWithParamsAwait(
            """
                SELECT
                       username,
                       game_id gameId,
                       save_name saveName,
                       saved_time savedTime,
                       url
                FROM game_save 
                WHERE username = ? and game_id = ? and save_name = ?;
            """.trimIndent(),
            jsonArrayOf(username, gameId, saveName)
        )?.let {
            GameSave(
                it.getString(0), it.getInteger(1), it.getString(2),
                it.getInstant(3), it.getStorageFile(4)!!
            )
        }

    }

}
