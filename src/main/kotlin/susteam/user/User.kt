package susteam.user

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf

data class User(
    val username: String,
    val avatar: String?,
    val description: String?,
    val balance: Int
)

typealias Auth = io.vertx.ext.auth.User

val Auth.username
    get() = this.principal().getString("username")

data class UserRole(
    val user: User,
    val roles: List<String>
)

fun User.toJson(): JsonObject = jsonObjectOf(
    "username" to username,
    "avatar" to (avatar ?: defaultAvatar()),
    "description" to description
)

fun JsonObject.toUser(): User = User(
    getString("username"),
    getString("avatar"),
    getString("description"),
    getInteger("balance")
)

fun UserRole.toJson(): JsonObject = jsonObjectOf(
    "username" to user.username,
    "avatar" to (user.avatar ?: defaultAvatar()),
    "description" to user.description,
    "roles" to json { array(roles) }
)

fun defaultAvatar(): String = "/avatar/default.jpg"
