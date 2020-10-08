package susteam.announcement

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant

data class Announcement(
    val gameId: Int,
    val announceTime: Instant,
    val title: String,
    val content: String
)

fun Announcement.toJson(): JsonObject = jsonObjectOf(
    "gameId" to gameId,
    "announceTime" to announceTime,
    "title" to title,
    "content" to content
)

fun JsonObject.toAnnouncement(): Announcement = Announcement(
    getInteger("gameId"),
    getInstant("announceTime"),
    getString("title"),
    getString("content")
)