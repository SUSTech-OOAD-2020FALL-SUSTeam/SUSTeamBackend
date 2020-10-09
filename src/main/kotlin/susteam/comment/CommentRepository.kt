package susteam.comment

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant

class CommentRepository @Inject constructor(private val database: JDBCClient) {

    suspend fun create(
        username: String,
        gameId: Int,
        commentTime: Instant,
        content: String,
        score: Int
    ) {
        try {
            database.updateWithParamsAwait(
                """
                    INSERT INTO `comment` (username, game_id, comment_time, content, score) 
                    VALUES (?, ?, ?, ?, ?);
                """.trimIndent(),
                jsonArrayOf(username, gameId, commentTime.toString(), content, score)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            throw ServiceException("Comment failed", e)
        }
    }

    suspend fun getExists(username: String, gameId: Int): Int? {
        return database.querySingleWithParamsAwait(
            """
                SELECT 1
                FROM `comment`
                WHERE username = ? and game_id = ?;
            """.trimIndent(),
            jsonArrayOf(username, gameId)
        )?.getInteger(0)
    }

    suspend fun modify(
        username: String,
        gameId: Int,
        commentTime: Instant,
        newContent: String,
        newScore: Int
    ) {
        try {
            database.updateWithParamsAwait(
                """
                UPDATE `comment`
                SET comment_time = ?,
                    content      = ?,
                    score        = ?
                WHERE username = ?
                  and game_id = ?;
            """.trimIndent(),
                jsonArrayOf(commentTime.toString(), newContent, newScore, username, gameId)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            throw ServiceException("Cannot update comment", e)
        }
    }

    suspend fun getByUser(username: String): List<Comment> {
        return database.queryWithParamsAwait(
            """
                SELECT username, game_id gameId, comment_time commentTime, content, score
                FROM `comment`
                WHERE username = ?;
            """.trimIndent(),
            jsonArrayOf(username)
        ).rows.map { it.toComment() }
    }


    suspend fun getByGame(gameId: Int): List<Comment> {
        return database.queryWithParamsAwait(
            """
                SELECT username, game_id gameId, comment_time commentTime, content, score
                FROM `comment`
                WHERE game_id = ?;
            """.trimIndent(),
            jsonArrayOf(gameId)
        ).rows.map { it.toComment() }
    }
}
