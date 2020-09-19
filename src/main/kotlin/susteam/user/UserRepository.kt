package susteam.user

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import java.sql.SQLIntegrityConstraintViolationException

class UserRepository @Inject constructor(private val database: JDBCClient) {

    suspend fun create(username: String, password: String) {
        try {
            database.updateWithParamsAwait(
                """INSERT INTO user (username, password) VALUES (?, ?);""",
                jsonArrayOf(username, password)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            if (e.message?.contains("user.PRIMARY") == true) {
                throw ServiceException("Cannot create user '$username'", e)
            } else {
                throw e
            }
        }
    }

    suspend fun get(username: String): User? {
        return database.querySingleWithParamsAwait(
            """SELECT username, avatar, description, balance FROM user WHERE username = ?;""",
            jsonArrayOf(username)
        )?.let { User(it.getString(0), it.getString(1), it.getString(2), it.getInteger(3)) }
    }

    suspend fun getPasswordHash(username: String): String? {
        return database.querySingleWithParamsAwait(
            """SELECT password FROM user WHERE username = ?;""",
            jsonArrayOf(username)
        )?.getString(0)
    }

    suspend fun getRole(username: String): UserRole? {
        val result = database.queryWithParamsAwait(
            """
                SELECT u.username, u.avatar, u.description, u.balance, ur.role
                FROM user u
                LEFT JOIN user_roles ur on u.username = ur.username
                WHERE u.username = ?;
            """.trimIndent(),
            jsonArrayOf(username)
        ).rows

        val user = result.firstOrNull()?.toUser() ?: return null
        val roles = result.map { it.getString("role") }

        return UserRole(user, roles)
    }

}