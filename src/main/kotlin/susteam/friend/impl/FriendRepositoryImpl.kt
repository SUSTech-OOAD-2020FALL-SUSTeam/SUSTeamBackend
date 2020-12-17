package susteam.friend.impl

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.friend.*
import java.sql.SQLIntegrityConstraintViolationException

class FriendRepositoryImpl @Inject constructor(private val database: JDBCClient) : FriendRepository {
    override suspend fun getFriendsUsername(username: String): List<String> {
        return database.queryWithParamsAwait(
            """
               (SELECT user1 friend_name
                 FROM relationship
                 WHERE user2 = ?
                   AND status = 'accept')
                UNION
                (SELECT user2 friend_name
                 FROM relationship
                 WHERE user1 = ?
                   AND status = 'accept');
                """.trimIndent(),
            jsonArrayOf(username, username)
        ).rows.map { it.getString("friend_name").toString() }
    }

    override suspend fun getFriendsUsernameHaveGame(username: String, gameId: Int): List<String> {
        return database.queryWithParamsAwait(
            """
                (SELECT user1 friend_name
                 FROM relationship r
                          JOIN `order` o ON user1 = o.username
                 WHERE user2 = ?
                   AND o.game_id = ?
                   AND (o.status = 'REFUNDABLE' OR o.status = 'SUCCESS')
                   AND r.status = 'accept')
                UNION
                (SELECT user2 friend_name
                 FROM relationship r
                          JOIN `order` o ON user2 = o.username
                 WHERE user1 = ?
                   AND o.game_id = ?
                   AND (o.status = 'REFUNDABLE' OR o.status = 'SUCCESS')
                   AND r.status = 'accept')
                """.trimIndent(),
            jsonArrayOf(username, gameId, username, gameId)
        ).rows.map { it.getString("friend_name").toString() }
    }

    override suspend fun addFriend(username: String, target: String) {
        database.queryWithParamsAwait(
            """
                SELECT user1, user2
                FROM relationship
                WHERE user1 = ? AND user2 = ?
                   OR user1 = ? AND user2 = ?;
            """.trimIndent(),
            jsonArrayOf(username, target, target, username)
        ).let {
            if (it.rows.isNotEmpty())
                throw ServiceException("Friend Application Duplicate")
        }

        try {
            database.updateWithParamsAwait(
                """
                INSERT INTO relationship (user1, user2, status) VALUES (?, ?, 'pending');
                """.trimIndent(),
                jsonArrayOf(username, target)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            e.message?.contains("FOREIGN KEY").let {
                if (it == true) {
                    throw ServiceException("User Not Found")
                } else {
                    throw e
                }
            }
        }
    }

    override suspend fun getApplicationList(username: String): List<FriendApplication> {
        return database.queryWithParamsAwait(
            """
                SELECT user2 AS `to`, status FROM relationship
                WHERE user1 = ?;
            """.trimIndent(),
            jsonArrayOf(username)
        ).rows.map {
            it.toFriendApplication()
        }
    }

    override suspend fun getReplyList(username: String): List<FriendReply> {
        return database.queryWithParamsAwait(
            """
                SELECT user1 AS `from`, status FROM relationship
                WHERE user2 = ?;
            """.trimIndent(),
            jsonArrayOf(username)
        ).rows.map {
            it.toFriendReply()
        }
    }

    override suspend fun replyTo(username: String, applicant: String, newStatus: String): Boolean {
        return database.updateWithParamsAwait(
            """
                UPDATE relationship SET status = ?
                WHERE user1 = ? AND user2 = ? AND status = 'pending';
            """.trimIndent(),
            jsonArrayOf(newStatus, username, applicant)
        ).updated == 1
    }
}
