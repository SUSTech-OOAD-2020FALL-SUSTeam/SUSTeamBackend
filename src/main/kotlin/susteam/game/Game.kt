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
        val introduction: String?,
        val description: String?
)

data class GameVersion(
        val gameId: Int,
        val name: String,
        val url: String
)

data class GameImage(
        val gameId: Int,
        val fileName: String,
        //TODO this value uncertain
        val type: String

)

data class GameProfile(
        val id: Int,
        val name: String,
        val price: Int,
        val publishDate: Instant,
        val author: String,
        val introduction: String?,
        // description数据量比较大，GameProfile 中没有 description
        val imageFullSize: String,
        val imageCardSize: String
)

data class GameDetail(
        val game: Game,
        val images: List<String>
        //TODO type of this List uncertain
)

fun Game.toJson(): JsonObject = jsonObjectOf(
        "id" to id,
        "name" to name,
        "price" to price,
        "publishDate" to publishDate,
        "author" to author,
        "introduction" to introduction,
        "description" to description
)

fun JsonObject.toGame(): Game = Game(
        getInteger("gameId"),
        getString("name"),
        getInteger("price"),
        getInstant("publishDate"),
        getString("author"),
        getString("introduction"),
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

fun GameProfile.toJson(): JsonObject = jsonObjectOf(
        "id" to id,
        "name" to name,
        "price" to price,
        "publishDate" to publishDate,
        "author" to author,
        "introduction" to introduction,
        "imageFullSize" to imageFullSize,
        "imageCardSize" to imageCardSize
)

fun JsonObject.toGameProfile(): GameProfile = GameProfile(
        getInteger("gameId"),
        getString("name"),
        getInteger("price"),
        getInstant("publishDate"),
        getString("author"),
        getString("description"),
        getString("imageFullSize"),
        getString("imageCardSize")
)

fun GameDetail.toJson(): JsonObject = jsonObjectOf(
        "game" to game.toJson(),
        "images" to images
)

fun JsonObject.toGameDetail(): GameDetail = GameDetail(
        getJsonObject("game").toGame(),
        getJsonArray("images").map { it.toString() }
)

fun GameImage.toJson(): JsonObject = jsonObjectOf(
        "gameId" to gameId,
        "fileName" to fileName,
        "type" to type
)

fun JsonObject.toGameImage(): GameImage = GameImage(
        getInteger("gameId"),
        getString("fileName"),
        getString("type")
)