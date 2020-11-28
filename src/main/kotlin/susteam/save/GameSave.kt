package susteam.save

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.storage.StorageFile
import susteam.storage.getStorageFile
import java.time.Instant

data class GameSave(
    val username: String,
    val gameId: Int,
    val saveName: String,
    val savedTime: Instant,
    val url: StorageFile
)

fun GameSave.toJson(): JsonObject = jsonObjectOf(
    "username" to username,
    "gameId" to gameId,
    "saveName" to saveName,
    "savedTime" to savedTime,
    "url" to url.url
)

fun JsonObject.toGameSave(): GameSave = GameSave(
    getString("username"),
    getInteger("gameId"),
    getString("saveName"),
    getInstant("savedTime"),
    getStorageFile("url")!!
)
