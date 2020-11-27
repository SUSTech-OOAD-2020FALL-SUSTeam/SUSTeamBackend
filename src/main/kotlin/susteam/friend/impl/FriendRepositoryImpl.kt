package susteam.friend.impl

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import susteam.friend.FriendRepository

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
}