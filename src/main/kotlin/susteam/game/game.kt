package susteam.game

import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.jsonObjectOf


data class Game(
    val game_id: Int,
    val game_name: String,
    val price: Double,
    val author_id: Int,
    val description: String?
)


data class GameVersion(
    val game_id: Int,
    val publish_date: String,
    val version_name: String,
    val url: String
)

fun Game.toJson(): JsonObject = jsonObjectOf(
    "game_id" to game_id,
    "game_name" to game_name,
    "price" to price,
    "author_id" to author_id,
    "description" to description
)

fun JsonObject.toGame(): Game = Game(
    getInteger("game_id"),
    getString("game_name"),
    getDouble("price"),
    getInteger("author_id"),
    getString("description")
)

fun GameVersion.toJson(): JsonObject = jsonObjectOf(
    "game_id" to game_id,
    "version_name" to version_name,
    "publish_date" to publish_date,
    "url" to "url"
)

fun JsonObject.toGameVersion(): GameVersion = GameVersion(
    getInteger("game_id"),
    getString("version_name"),
    getString("publish_date"),
    getString("url")
)