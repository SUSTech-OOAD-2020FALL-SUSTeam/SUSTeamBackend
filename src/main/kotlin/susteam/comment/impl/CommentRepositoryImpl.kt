package susteam.comment.impl

import susteam.comment.Comment
import susteam.comment.toComment

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.comment.CommentRepository
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant

class CommentRepositoryImpl @Inject constructor(private val database: JDBCClient) : CommentRepository {

    override suspend fun create(
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

    override suspend fun getExists(username: String, gameId: Int): Boolean {
        return database.querySingleWithParamsAwait(
            """
                SELECT 1
                FROM `comment`
                WHERE username = ? and game_id = ?;
            """.trimIndent(),
            jsonArrayOf(username, gameId)
        )?.let { true } ?: false
    }

    override suspend fun modify(
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

    override suspend fun getByUser(username: String): List<Comment> {
        return database.queryWithParamsAwait(
            """
                SELECT username, game_id gameId, comment_time commentTime, content, score
                FROM `comment`
                WHERE username = ?;
            """.trimIndent(),
            jsonArrayOf(username)
        ).rows.map { it.toComment() }
    }


    override suspend fun getByGame(gameId: Int): List<Comment> {
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
