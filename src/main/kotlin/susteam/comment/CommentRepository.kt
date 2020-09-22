package susteam.comment

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
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
    ): Int {
        try {
            return database.updateWithParamsAwait(
                """
                    INSERT INTO `comment` (username, game_id, comment_time, content, score) 
                    VALUES (?, ?, ?, ?, ?);
                """.trimIndent(),
                jsonArrayOf(username, gameId, commentTime, content, score)
            ).keys.getInteger(0)
        } catch (e: SQLIntegrityConstraintViolationException) {
            // XXX: Shall we consider all cases of comment failures?
            throw ServiceException("Comment failed", e)
        }
    }

    suspend fun getByUser(username: String): List<Comment> {
        return database.queryWithParamsAwait(
            """
                SELECT username, game_id, comment_time, content, score
                FROM `comment`
                WHERE username = ?;
            """.trimIndent(),
            jsonArrayOf(username)
        ).rows.map { it.toComment() }
    }


    suspend fun getByGame(gameId: Int): List<Comment> {
        return database.queryWithParamsAwait(
            """
                SELECT username, game_id, comment_time, content, score
                FROM `comment`
                WHERE game_id = ?;
            """.trimIndent(),
            jsonArrayOf(gameId)
        ).rows.map { it.toComment() }
    }
}
