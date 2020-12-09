package susteam.user

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.storage.toStorageImage

data class User(
    val username: String,
    val mail: String,
    val avatar: String?,
    val description: String?,
    val balance: Int
)

typealias Auth = io.vertx.ext.auth.User

val Auth.username
    get() = this.principal().getString("username")

fun Auth.isAuthorized(permission: String): Boolean {
    return this.principal().getJsonArray("permissions").contains(permission)
}

fun Auth.isAdmin() = this.isAuthorized("role:admin")
fun Auth.isDeveloper() = this.isAuthorized("role:developer")

data class UserRole(
    val user: User,
    val roles: List<String>
)

fun User.toJson(): JsonObject = jsonObjectOf(
    "username" to username,
    "mail" to mail,
    "avatar" to (avatar ?: defaultAvatar()),
    "description" to description
)

fun JsonObject.toUser(): User = User(
    getString("username"),
    getString("mail"),
    (getString("avatar") ?: defaultAvatar()).toStorageImage().url,
    getString("description"),
    getInteger("balance")
)

fun UserRole.toJson(): JsonObject = jsonObjectOf(
    "username" to user.username,
    "mail" to user.mail,
    "avatar" to (user.avatar ?: defaultAvatar()),
    "description" to user.description,
    "balance" to user.balance,
    "roles" to json { array(roles) }
)

fun defaultAvatar(): String = "0B01A8D8572A6E78E62718BAA31817A6.jpg"
