package susteam.game

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.discount.Discount
import susteam.discount.toDiscount
import susteam.discount.toJson
import susteam.storage.StorageFile
import susteam.storage.StorageImage
import susteam.storage.getStorageFile
import susteam.storage.getStorageImage
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
    val uploadTime: Instant,
    val name: String,
    val url: StorageFile
)

data class GameImage(
    val gameId: Int,
    val url: StorageImage,
    val type: String
)

data class GameProfile(
    val gameId: Int,
    val name: String,
    val price: Int,
    val publishDate: Instant,
    val author: String,
    val introduction: String?,
    val imageFullSize: StorageImage?,
    val imageCardSize: StorageImage?,
    val discount: Discount?
)

data class GameDetail(
    val game: Game,
    val images: List<GameImage>,
    val tags: List<String>,
    val discount: Discount?
)

data class GameTag(
    val gameId: Int,
    val tag: String
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
    getInteger("id"),
    getString("name"),
    getInteger("price"),
    getInstant("publishDate"),
    getString("author"),
    getString("introduction"),
    getString("description")
)

fun GameVersion.toJson(): JsonObject = jsonObjectOf(
    "gameId" to gameId,
    "uploadTime" to uploadTime,
    "name" to name,
    "url" to url.url
)

fun JsonObject.toGameVersion(): GameVersion = GameVersion(
    getInteger("gameId"),
    getInstant("uploadTime"),
    getString("name"),
    getStorageFile("url")!!
)

fun GameProfile.toJson(): JsonObject = jsonObjectOf(
    "gameId" to gameId,
    "name" to name,
    "price" to price,
    "publishDate" to publishDate,
    "author" to author,
    "introduction" to introduction,
    "imageFullSize" to imageFullSize?.url,
    "imageCardSize" to imageCardSize?.url,
    "discount" to discount?.toJson()
)

fun JsonObject.toGameProfile(): GameProfile = GameProfile(
    getInteger("gameId"),
    getString("name"),
    getInteger("price"),
    getInstant("publishDate"),
    getString("author"),
    getString("introduction"),
    getStorageImage("imageFullSize"),
    getStorageImage("imageCardSize"),
    if (getDouble("percentage") != null)
        Discount(
            getInteger("gameId"),
            getDouble("percentage"),
            getInstant("startTime"),
            getInstant("endTime")
        )
    else null
)

fun GameDetail.toJson(): JsonObject = jsonObjectOf(
    "gameId" to game.id,
    "name" to game.name,
    "price" to game.price,
    "publishDate" to game.publishDate,
    "author" to game.author,
    "introduction" to game.introduction,
    "description" to game.description,
    "images" to JsonArray(images.map { it.toJson() }),
    "tags" to JsonArray(tags),
    "discount" to discount?.toJson()
)

fun JsonObject.toGameDetail(): GameDetail {
    val obj = this.copy()
    obj.put("id", obj.getInteger("gameId"))

    return GameDetail(
        obj.toGame(),
        getJsonArray("images").map { (it as JsonObject).toGameImage() },
        getJsonArray("tags").map { it as String },
        getJsonObject("discount").toDiscount()
    )
}

fun GameImage.toJson(): JsonObject = jsonObjectOf(
    "gameId" to gameId,
    "url" to url.url,
    "type" to type
)

fun JsonObject.toGameImage(): GameImage = GameImage(
    getInteger("gameId"),
    getStorageImage("url")!!,
    getString("type")
)

fun GameTag.toJson(): JsonObject = jsonObjectOf(
    "gameId" to gameId,
    "tag" to tag
)

fun JsonObject.toGameTag(): GameTag = GameTag(
    getInteger("gameId"),
    getString("tag")
)
