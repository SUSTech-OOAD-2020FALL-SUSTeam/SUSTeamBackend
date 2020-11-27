package susteam.friend

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant

data class Friend(
    val username: String,
    val status: String,
    val lastSeen: Instant
)

fun Friend.toJson(): JsonObject = jsonObjectOf(
    "username" to username,
    "status" to status,
    "lastSeen" to lastSeen
)