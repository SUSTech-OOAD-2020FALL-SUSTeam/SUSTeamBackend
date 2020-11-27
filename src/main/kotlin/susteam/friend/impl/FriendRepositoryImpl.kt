package susteam.friend.impl

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.friend.FriendRepository
import java.sql.SQLIntegrityConstraintViolationException

class FriendRepositoryImpl @Inject constructor(private val database: JDBCClient) : FriendRepository {
    override suspend fun getFriendsUsername(username: String): List<String> {
        return database.queryWithParamsAwait(
            """(SELECT user1 friend_name
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

    override suspend fun addFriend(from: String, to: String) {
        database.queryWithParamsAwait(
            """
                SELECT user1, user2
                FROM relationship
                WHERE user1 = ? and user2 = ?
                   OR user1 = ? and user2 = ?;
            """.trimIndent(),
            jsonArrayOf(from, to, to, from)
        ).let {
            if (it.rows.isNotEmpty())
                throw ServiceException("Friend Application Duplicate")
        }

        try {
            database.updateWithParamsAwait(
                """
                INSERT INTO relationship (user1, user2, status) VALUES (?, ?, 'pending');
                """.trimIndent(),
                jsonArrayOf(from, to)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            val message = e.message ?: throw e

            if (message.contains("FOREIGN KEY")) {
                throw ServiceException("User Not Found")
            } else {
                throw e
            }
        }
    }
}