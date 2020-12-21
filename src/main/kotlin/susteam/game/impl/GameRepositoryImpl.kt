package susteam.game.impl

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.discount.Discount
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

    override suspend fun addKeyMap(gameId: Int, gameKey: String) {
        database.updateWithParamsAwait(
            """INSERT INTO game_map (game_id, game_key) VALUES (?,?);""",
            jsonArrayOf(gameId, gameKey)
        )
    }

    override suspend fun getGameByGameKey(gameKey: String): Game? {
        return database.querySingleWithParamsAwait(
            """
                SELECT game.game_id, name, price, publish_date, author, introduction, description
                FROM game
                JOIN game_map gm on game.game_id = gm.game_id
                WHERE gm.game_key = ?;""".trimIndent(),
            jsonArrayOf(gameKey)
        )?.let {
            Game(
                it.getInteger(0), it.getString(1), it.getInteger(2),
                it.getInstant(3), it.getString(4), it.getString(5),
                it.getString(6)
            )
        }
    }

    override suspend fun getGameKey(gameId: Int): String? {
        return database.querySingleWithParamsAwait(
            """SELECT game_key FROM game_map WHERE game_id = ?;""",
            jsonArrayOf(gameId)
        )?.getString(0)
    }

    override suspend fun updateGame(
        gameId: Int,
        game: Game
    ): Boolean {
        return database.updateWithParamsAwait(
            """
                UPDATE game 
                SET price = ?,
                    introduction = ?,
                    description = ? 
                WHERE game_id = ?;
            """.trimIndent(),
            jsonArrayOf(game.price, game.introduction, game.description, gameId)
        ).updated == 1
    }

    override suspend fun createVersion(
        gameId: Int,
        branch: String,
        uploadTime: Instant,
        versionName: String,
        url: String,
        updateUrl: String?,
    ) {
        try {
            database.updateWithParamsAwait(
                """INSERT INTO game_version (game_id, branch, upload_time, name, url, update_url) VALUES (?, ?, ?, ?, ?, ?);""",
                jsonArrayOf(gameId, branch, uploadTime, versionName, url, updateUrl)
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
            """
            SELECT game_id, branch, upload_time, name, url, update_url FROM game_version 
            WHERE name = ? AND game_id = ?;
            """.trimIndent(),
            jsonArrayOf(versionName, gameId)
        )?.let {
            GameVersion(
                it.getInteger(0),
                it.getString(1),
                it.getInstant(2),
                it.getString(3),
                it.getStorageFile(4)!!,
                it.getStorageFile(5)
            )
        }
    }

    override suspend fun getNewestVersion(gameId: Int, branchName: String): GameVersion? {
        return database.querySingleWithParamsAwait(
            """
                SELECT game_id, branch, upload_time, name, url, update_url FROM game_version 
                WHERE game_id = ? AND branch = ?
                ORDER BY upload_time DESC
                LIMIT 1;
            """.trimIndent(),
            jsonArrayOf(gameId, branchName)
        )?.let {
            GameVersion(
                it.getInteger(0),
                it.getString(1),
                it.getInstant(2),
                it.getString(3),
                it.getStorageFile(4)!!,
                it.getStorageFile(5)
            )
        }
    }

    override suspend fun getVersionOfBranch(gameId: Int, branchName: String): List<GameVersion> {
        return database.queryWithParamsAwait(
            """
                SELECT game_id, branch, upload_time, name, url, update_url FROM game_version 
                WHERE game_id = ? AND branch = ?
                ORDER BY upload_time DESC;
            """.trimIndent(),
            jsonArrayOf(gameId, branchName)
        ).results.map {
            GameVersion(
                it.getInteger(0),
                it.getString(1),
                it.getInstant(2),
                it.getString(3),
                it.getStorageFile(4)!!,
                it.getStorageFile(5)
            )
        }
    }

    override suspend fun getAllBranch(gameId: Int): List<String> {
        return database.queryWithParamsAwait(
            """
            SELECT DISTINCT branch FROM game_version WHERE game_id = ?
            """.trimIndent(), jsonArrayOf(gameId)
        ).results.map { it.getString(0) }
    }

    override suspend fun getAllGameProfileOrderByPublishDate(): List<GameProfile> {
        return getAllGameProfile("publish_date" to "DESC")
    }

    override suspend fun getAllGameProfileOrderByName(): List<GameProfile> {
        return getAllGameProfile("name" to "ASC")
    }

    override suspend fun getAllGameProfile(vararg order: Pair<String, String>, limit: Int?): List<GameProfile> {
        val orderString = order.joinToString(", ") { "${it.first} ${it.second}" }
        return database.queryWithParamsAwait(
            """
                WITH sub AS (SELECT game_id, percentage, start_time, end_time
                             FROM discount
                             WHERE start_time <= ?
                               AND end_time >= ?
                             ORDER BY percentage
                             LIMIT 1)
                SELECT game.game_id   gameId,
                       name,
                       price,
                       publish_date   publishDate,
                       author,
                       introduction,
                       gi1.url        imageFullSize,
                       gi2.url        imageCardSize,
                       sub.percentage,
                       sub.start_time startTime,
                       sub.end_time   endTime
                FROM game
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                         LEFT JOIN sub ON sub.game_id = game.game_id
                ${if (order.isEmpty()) "" else "ORDER BY $orderString"}
                ${if (limit == null) "" else "LIMIT $limit"};
            """.trimIndent(),
            jsonArrayOf(ISO_INSTANT.format(Instant.now()), ISO_INSTANT.format(Instant.now()))
        ).rows.map { it.toGameProfile() }
    }

    override suspend fun getRandomGameProfile(limit: Int): List<GameProfile> {
        return getAllGameProfile("rand()" to "", limit = limit)
    }

    override suspend fun getGameProfile(gameId: Int): GameProfile? {
        return database.querySingleWithParamsAwait(
            """
                WITH sub AS (SELECT game_id, percentage, start_time, end_time
                             FROM discount
                             WHERE start_time <= ?
                               AND end_time >= ?
                             ORDER BY percentage
                             LIMIT 1)
                SELECT game.game_id   gameId,
                       name,
                       price,
                       publish_date   publishDate,
                       author,
                       introduction,
                       gi1.url        imageFullSize,
                       gi2.url        imageCardSize,
                       sub.percentage,
                       sub.start_time startTime,
                       sub.end_time   endTime
                FROM game
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                         LEFT JOIN sub ON sub.game_id = game.game_id
                WHERE game.game_id = ?;
            """.trimIndent(),
            jsonArrayOf(ISO_INSTANT.format(Instant.now()), ISO_INSTANT.format(Instant.now()), gameId)
        )?.let {
            GameProfile(
                it.getInteger(0),
                it.getString(1),
                it.getInteger(2),
                it.getInstant(3),
                it.getString(4),
                it.getString(5),
                it.getStorageImage(6),
                it.getStorageImage(7),
                if (it.getDouble(8) != null)
                    Discount(
                        it.getInteger(0),
                        it.getDouble(8),
                        it.getInstant(9),
                        it.getInstant(10)
                    )
                else null
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
            ).results.map { it.getString(0) },
            database.querySingleWithParamsAwait(
                """
                    SELECT game_id    gameId,
                           percentage,
                           start_time startTime,
                           end_time   endTime
                    FROM discount
                    WHERE game_id = ?
                      AND start_time <= ?
                      AND end_time >= ?
                    ORDER BY percentage
                    LIMIT 1;
                """.trimIndent(),
                jsonArrayOf(gameId, ISO_INSTANT.format(Instant.now()), ISO_INSTANT.format(Instant.now()))
            ).let {
                if (it != null)
                    Discount(
                        it.getInteger(0),
                        it.getDouble(1),
                        it.getInstant(2),
                        it.getInstant(3)
                    )
                else null
            }
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
                WITH sub2 AS (
                    SELECT game_id, COUNT(*) cnt
                    FROM game_tag
                    WHERE tag IN ($inputs)
                    GROUP BY game_id
                ),
                     sub AS (
                         SELECT game_id, percentage, start_time, end_time
                         FROM discount
                         WHERE start_time <= ?
                           AND end_time >= ?
                         ORDER BY percentage
                         LIMIT 1
                     )
                SELECT game.game_id   gameId,
                       name,
                       price,
                       publish_date   publishDate,
                       author,
                       introduction,
                       gi1.url        imageFullSize,
                       gi2.url        imageCardSize,
                       sub.percentage,
                       sub.start_time startTime,
                       sub.end_time   endTime,
                       sub2.cnt
                FROM game
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                         LEFT JOIN sub ON sub.game_id = game.game_id
                         JOIN sub2 ON game.game_id = sub2.game_id
                WHERE sub2.cnt = $tagSize;
            """.trimIndent()
        return database.queryWithParamsAwait(
            sql, JsonArray(tags).add(ISO_INSTANT.format(Instant.now())).add(ISO_INSTANT.format(Instant.now()))
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
        if (games.isEmpty()) {
            return emptyList()
        }
        return database.queryWithParamsAwait(
            """
                WITH sub AS (SELECT game_id, percentage, start_time, end_time
                             FROM discount
                             WHERE start_time <= ?
                               AND end_time >= ?
                             ORDER BY percentage
                             LIMIT 1)
                SELECT game.game_id   gameId,
                       name,
                       price,
                       publish_date   publishDate,
                       author,
                       introduction,
                       gi1.url        imageFullSize,
                       gi2.url        imageCardSize,
                       sub.percentage,
                       sub.start_time startTime,
                       sub.end_time   endTime
                FROM game
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                         LEFT JOIN sub ON sub.game_id = game.game_id
                WHERE game.game_id IN (${games.joinToString(",")});
            """.trimIndent(),
            jsonArrayOf(ISO_INSTANT.format(Instant.now()), ISO_INSTANT.format(Instant.now()))
        ).rows.map { it.toGameProfile() }
    }

    override suspend fun getDevelopedGameProfile(author: String): List<GameProfile> {
        return database.queryWithParamsAwait(
            """
                WITH sub AS (SELECT game_id, percentage, start_time, end_time
                             FROM discount
                             WHERE start_time <= ?
                               AND end_time >= ?
                             ORDER BY percentage
                             LIMIT 1)
                SELECT game.game_id   gameId,
                       name,
                       price,
                       publish_date   publishDate,
                       author,
                       introduction,
                       gi1.url        imageFullSize,
                       gi2.url        imageCardSize,
                       sub.percentage,
                       sub.start_time startTime,
                       sub.end_time   endTime
                FROM game
                         LEFT JOIN game_image gi1 ON game.game_id = gi1.game_id AND gi1.type = 'F'
                         LEFT JOIN game_image gi2 ON game.game_id = gi2.game_id AND gi2.type = 'C'
                         LEFT JOIN sub ON sub.game_id = game.game_id
               WHERE author = ?;
            """.trimIndent(),
            jsonArrayOf(ISO_INSTANT.format(Instant.now()), ISO_INSTANT.format(Instant.now()), author)
        ).rows.map { it.toGameProfile() }
    }
}
