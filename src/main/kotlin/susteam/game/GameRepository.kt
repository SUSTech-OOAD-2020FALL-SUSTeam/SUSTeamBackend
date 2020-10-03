package susteam.game

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT

class GameRepository @Inject constructor(private val database: JDBCClient) {

    suspend fun createGame(
        name: String,
        price: Int,
        publishDate: Instant,
        author: String,
        introduction: String?,
        description: String?
    ): Int {
        try {
            return database.updateWithParamsAwait(
                """INSERT INTO game (name, price, publish_date, author, introduction, description) VALUES (?, ?, ?, ?, ?, ?);""",
                jsonArrayOf(name, price, ISO_INSTANT.format(publishDate), author, introduction, description)
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
            """
                    SELECT game_id, name, price, publish_date, author, introduction, description 
                    FROM game 
                    WHERE game_id = ?;""".trimIndent(),
            jsonArrayOf(id)
        )?.let {
            Game(
                it.getInteger(0), it.getString(1), it.getInteger(2),
                it.getInstant(3), it.getString(4), it.getString(5),
                it.getString(6)
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

    suspend fun getAllGameProfileOrderByPublishDate(): List<GameProfile> {
        return getAllGameProfile("publish_date" to "DESC")
    }

    suspend fun getAllGameProfileOrderByName(): List<GameProfile> {
        return getAllGameProfile("name" to "ASC")
    }

    suspend fun getAllGameProfile(vararg order: Pair<String, String>): List<GameProfile> {
        val orderString = order.joinToString(", ") { "${it.first} ${it.second}"}
        return database.queryAwait(
            """
                SELECT game.game_id gameId,
                       name,
                       price,
                       publish_date publishDate,
                       author,
                       introduction,
                       gi1.url      imageFullSize,
                       gi2.url      imageCardSize
                FROM game
                         JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                ${ if (order.isEmpty()) "" else "ORDER BY $orderString"};
            """.trimIndent()
        ).rows.map { it.toGameProfile() }
    }

    suspend fun getRandomGames(numberOfGames: Int): List<Game> {
        return database.queryWithParamsAwait(
            """
                SELECT game_id gameId, name, price, publish_date publishDate, author, introduction, description 
                FROM game
                ORDER BY rand() 
                LIMIT ?;
            """.trimIndent(), jsonArrayOf(numberOfGames)
        ).rows.map { it.toGame() }
    }

    suspend fun getGameProfile(gameId: Int): GameProfile? {
        return database.querySingleWithParamsAwait(
            """
                SELECT game.game_id gameId,
                       name,
                       price,
                       publish_date publishDate,
                       author,
                       introduction,
                       gi1.url      imageFullSize,
                       gi2.url      imageCardSize
                FROM game
                         JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C';
            """.trimIndent(),
            jsonArrayOf(gameId)
        )?.let {
            GameProfile(
                it.getInteger(0), it.getString(1), it.getInteger(2),
                it.getInstant(3), it.getString(4), it.getString(5),
                it.getString(6), it.getString(7)
            )
        }
    }

    suspend fun getGameDetail(gameId: Int): GameDetail? {
        val game = getById(gameId) ?: return null
        return GameDetail(
            game,
            database.queryWithParamsAwait(
                """SELECT game_id gameId, url, type FROM game_image where game_id = ?;""",
                jsonArrayOf(gameId)
            ).rows.map { it.toGameImage() }
        )
    }
}