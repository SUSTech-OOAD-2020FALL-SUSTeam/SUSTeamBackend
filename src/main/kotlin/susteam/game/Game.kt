package susteam.game

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant

data class Game(
    val id: Int,
    val name: String,
    val price: Int,
    val publishDate: Instant,
    val author: String,
    val description: String?
)

data class GameVersion(
    val gameId: Int,
    val name: String,
    val url: String
)

fun Game.toJson(): JsonObject = jsonObjectOf(
    "id" to id,
    "name" to name,
    "price" to price,
    "publishDate" to publishDate,
    "author" to author,
    "description" to description
)

fun JsonObject.toGame(): Game = Game(
    getInteger("id"),
    getString("name"),
    getInteger("price"),
    getInstant("publishDate"),
    getString("author"),
    getString("description")
)

fun GameVersion.toJson(): JsonObject = jsonObjectOf(
    "gameId" to gameId,
    "name" to name,
    "url" to url
)

fun JsonObject.toGameVersion(): GameVersion = GameVersion(
    getInteger("gameId"),
    getString("name"),
    getString("url")
)