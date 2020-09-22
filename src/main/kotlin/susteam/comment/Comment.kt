package susteam.comment

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant

data class Comment(
    val username: String,
    val gameId: Int,
    val commentTime: Instant,
    val content: String,
    val score: Int
)

fun Comment.toJson(): JsonObject = jsonObjectOf(
    "username" to username,
    "gameId" to gameId,
    "commentTime" to commentTime,
    "content" to content,
    "score" to score
)

fun JsonObject.toComment(): Comment = Comment(
    getString("username"),
    getInteger("gameId"),
    getInstant("commentTime"),
    getString("content"),
    getInteger("score")
)