package susteam.user.impl

import com.google.inject.Inject
import io.vertx.ext.sql.SQLOperations
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.user.User
import susteam.user.UserRepository
import susteam.user.UserRole
import susteam.user.toUser
import java.sql.SQLIntegrityConstraintViolationException

class UserRepositoryImpl @Inject constructor(private val database: SQLOperations): UserRepository {

    override suspend fun create(username: String, password: String, mail: String) {
        try {
            database.updateWithParamsAwait(
                """INSERT INTO user (username, password, mail) VALUES (?, ?, ?);""",
                jsonArrayOf(username, password, mail)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            if (e.message?.contains("user.PRIMARY") == true) {
                throw ServiceException("Cannot create user '$username'", e)
            } else if (e.message?.contains("user.mail") == true) {
                throw ServiceException("Cannot create user '$username', mail '$mail' already exist", e)
            } else {
                throw e
            }
        }
    }

    override suspend fun get(username: String): User? {
        return database.querySingleWithParamsAwait(
            """SELECT username, mail, avatar, description, balance FROM user WHERE username = ?;""",
            jsonArrayOf(username)
        )?.let { User(it.getString(0), it.getString(1), it.getString(2), it.getString(3), it.getInteger(4)) }
    }

    override suspend fun getPasswordHash(username: String): String? {
        return database.querySingleWithParamsAwait(
            """SELECT password FROM user WHERE username = ?;""",
            jsonArrayOf(username)
        )?.getString(0)
    }

    override suspend fun getRole(username: String): UserRole? {
        val result = database.queryWithParamsAwait(
            """
                SELECT u.username, u.mail, u.avatar, u.description, u.balance, ur.role
                FROM user u
                LEFT JOIN user_roles ur on u.username = ur.username
                WHERE u.username = ?;
            """.trimIndent(),
            jsonArrayOf(username)
        ).rows

        val user = result.firstOrNull()?.toUser() ?: return null
        val roles = result.mapNotNull { it.getString("role") }

        return UserRole(user, roles)
    }

}