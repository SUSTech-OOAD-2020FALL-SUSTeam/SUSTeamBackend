package susteam.commentThumb

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf

data class CommentThumb(
    val commenter: String,
    val gameId: Int,
    val username: String,
    val vote: Int
)

fun CommentThumb.toJson(): JsonObject = jsonObjectOf(
    "commenter" to commenter,
    "gameId" to gameId,
    "username" to username,
    "vote" to vote
)

fun JsonObject.toCommentThumb(): CommentThumb = CommentThumb(
    getString("commenter"),
    getInteger("gameId"),
    getString("username"),
    getInteger("vote")
)