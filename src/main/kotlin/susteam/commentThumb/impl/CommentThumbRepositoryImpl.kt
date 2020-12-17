package susteam.commentThumb.impl

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.commentThumb.CommentThumb
import susteam.commentThumb.CommentThumbRepository
import susteam.commentThumb.toCommentThumb
import susteam.record.Record
import java.sql.SQLIntegrityConstraintViolationException

class CommentThumbRepositoryImpl @Inject constructor(private val database: JDBCClient) : CommentThumbRepository {

    override suspend fun insertCommentThumb(
        username: String,
        gameId: Int,
        commenter: String,
        voteNum: Int
    ): Boolean {
        try {
            database.updateWithParamsAwait(
                """INSERT INTO comment_thumb (commenter, game_id, username, vote) VALUES (?,?,?,?);""",
                jsonArrayOf(commenter, gameId, username, voteNum)
            )
            return true
        } catch (e: SQLIntegrityConstraintViolationException) {
            return false
        }
    }

    override suspend fun changeCommentThumb(
        username: String,
        gameId: Int,
        commenter: String,
        voteNum: Int
    ): Boolean {
        return database.updateWithParamsAwait(
            """UPDATE comment_thumb SET vote = ? WHERE commenter = ? and game_id = ? and username = ?;""",
            jsonArrayOf(voteNum, commenter, gameId, username)
        ).updated == 1
    }

    override suspend fun calCommentThumbSum(
        gameId: Int,
        commenter: String
    ): Int? {
        return database.querySingleWithParamsAwait(
            """SELECT sum(vote) voteSum FROM
                        comment_thumb
                        WHERE game_id = ? and commenter = ?
                        """,
            jsonArrayOf(gameId, commenter)
        )?.getInteger(0)
    }

    override suspend fun getCommentThumbByGame(
        gameId: Int,
        username: String
    ): List<CommentThumb> {
        return database.queryWithParamsAwait(
            """
                SELECT commenter, game_id, username, vote
                FROM comment_thumb
                WHERE game_id = ?
                  AND username = ?;
            """.trimIndent(),
            jsonArrayOf(gameId, username)
        ).rows.map { it.toCommentThumb() }
    }
}
