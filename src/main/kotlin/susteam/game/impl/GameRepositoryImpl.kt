package susteam.game.impl

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.game.*
import susteam.storage.getStorageFile
import susteam.storage.getStorageImage
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT

class GameRepositoryImpl @Inject constructor(private val database: JDBCClient) : GameRepository {

    override suspend fun createGame(
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

    override suspend fun updateDescription(
        gameId: Int,
        description: String?
    ): Boolean {
        return database.updateWithParamsAwait(
            """UPDATE game SET description = ? WHERE game_id = ?;""",
            jsonArrayOf(description, gameId)
        ).updated == 1
    }

    override suspend fun createVersion(
        gameId: Int,
        uploadTime: Instant,
        versionName: String,
        url: String
    ) {
        try {
            database.updateWithParamsAwait(
                """INSERT INTO game_version (game_id, upload_time, name, url) VALUES (?, ?, ?, ?);""",
                jsonArrayOf(gameId, uploadTime, versionName, url)
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

    override suspend fun getById(id: Int): Game? {
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

    override suspend fun getVersion(gameId: Int, versionName: String): GameVersion? {
        return database.querySingleWithParamsAwait(
            """SELECT game_id, upload_time, name, url FROM game_version WHERE name = ? AND game_id = ?;""",
            jsonArrayOf(versionName, gameId)
        )?.let {
            GameVersion(
                it.getInteger(0), it.getInstant(1), it.getString(2), it.getStorageFile(3)!!
            )
        }
    }

    override suspend fun getNewestVersion(gameId: Int): GameVersion? {
        return database.querySingleWithParamsAwait(
            """
                SELECT game_id, upload_time, name, url FROM game_version
                WHERE game_id = ?
                ORDER BY upload_time DESC
                LIMIT 1;
            """.trimIndent(),
            jsonArrayOf(gameId)
        )?.let {
            GameVersion(
                it.getInteger(0), it.getInstant(1), it.getString(2), it.getStorageFile(3)!!
            )
        }
    }

    override suspend fun getAllGameProfileOrderByPublishDate(): List<GameProfile> {
        return getAllGameProfile("publish_date" to "DESC")
    }

    override suspend fun getAllGameProfileOrderByName(): List<GameProfile> {
        return getAllGameProfile("name" to "ASC")
    }

    override suspend fun getAllGameProfile(vararg order: Pair<String, String>, limit: Int?): List<GameProfile> {
        val orderString = order.joinToString(", ") { "${it.first} ${it.second}" }
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
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                ${if (order.isEmpty()) "" else "ORDER BY $orderString"}
                ${if (limit == null) "" else "LIMIT $limit"};
            """.trimIndent()
        ).rows.map { it.toGameProfile() }
    }

    override suspend fun getRandomGameProfile(limit: Int): List<GameProfile> {
        return getAllGameProfile("rand()" to "", limit = limit)
    }

    override suspend fun getGameProfile(gameId: Int): GameProfile? {
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
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                WHERE game.game_id = ?;
            """.trimIndent(),
            jsonArrayOf(gameId)
        )?.let {
            GameProfile(
                it.getInteger(0), it.getString(1), it.getInteger(2),
                it.getInstant(3), it.getString(4), it.getString(5),
                it.getStorageImage(6), it.getStorageImage(7)
            )
        }
    }

    override suspend fun getGameDetail(gameId: Int): GameDetail? {
        val game = getById(gameId) ?: return null
        return GameDetail(
            game,
            database.queryWithParamsAwait(
                """SELECT game_id gameId, url, type FROM game_image where game_id = ?;""",
                jsonArrayOf(gameId)
            ).rows.map { it.toGameImage() },
            database.queryWithParamsAwait(
                """SELECT tag FROM game_tag WHERE game_id = ?;""",
                jsonArrayOf(gameId)
            ).results.map { it.getString(0) }
        )
    }

    override suspend fun createGameImage(gameId: Int, url: String, type: String): Boolean {
        return database.updateWithParamsAwait(
            """INSERT INTO game_image (game_id, url, type) VALUES (?, ?, ?);""",
            jsonArrayOf(gameId, url, type)
        ).updated == 1
    }

    override suspend fun updateGameImage(gameId: Int, url: String, type: String): Boolean {
        return database.updateWithParamsAwait(
            """UPDATE game_image SET url = ? WHERE game_id = ? AND type = ?;""",
            jsonArrayOf(url, gameId, type)
        ).updated == 1
    }

    override suspend fun getTag(gameId: Int): List<String> {
        return database.queryWithParamsAwait(
            """SELECT tag from game_tag where game_id = ?;""",
            jsonArrayOf(gameId)
        ).rows.map { it.getString("tag").toString() }
    }

    override suspend fun getAllTag(): List<String> {
        return database.queryAwait(
            """SELECT DISTINCT tag from game_tag;"""
        ).rows.map { it.getString("tag").toString() }
    }

    override suspend fun getGameProfileWithTags(tags: List<String>): List<GameProfile> {
        val tagSize: Int = tags.size
        if (tagSize == 0) return getAllGameProfile()

        val inputs = List(tagSize) { "?" }.joinToString(", ")
        val sql =
            """
                WITH sub AS (
                    SELECT game_id, COUNT(*) cnt
                    FROM game_tag
                    WHERE tag IN ($inputs)
                    GROUP BY game_id
                )
                SELECT game.game_id gameId,
                       name,
                       price,
                       publish_date publishDate,
                       author,
                       introduction,
                       gi1.url      imageFullSize,
                       gi2.url      imageCardSize,
                       sub.cnt
                FROM game
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                         JOIN sub ON game.game_id = sub.game_id
                WHERE cnt = $tagSize
            """.trimIndent()
        return database.queryWithParamsAwait(
            sql, JsonArray(tags)
        ).rows.map { it.toGameProfile() }
    }

    override suspend fun addTag(gameId: Int, tag: String) {
        try {
            database.updateWithParamsAwait(
                """INSERT INTO game_tag (game_id, tag) VALUES (?, ?);""",
                jsonArrayOf(gameId, tag)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            val message = e.message ?: throw e

            if (message.contains("FOREIGN KEY (`game_id`)")) {
                throw ServiceException("Cannot create tag '$tag' for game '$gameId', game '$gameId' do not exist")
            } else {
                throw e
            }
        }
    }

    override suspend fun getGameProfiles(games: List<Int>): List<GameProfile> {
        return database.queryWithParamsAwait(
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
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                WHERE game.game_id IN (?);
            """.trimIndent(),
            jsonArrayOf(
                games.joinToString(",")
            )
        ).rows.map { it.toGameProfile() }
    }

    override suspend fun getDevelopedGameProfile(author: String): List<GameProfile> {
        return database.queryWithParamsAwait(
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
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                WHERE author = ?;
            """.trimIndent(),
            jsonArrayOf(author)
        ).rows.map { it.toGameProfile() }
    }
}
