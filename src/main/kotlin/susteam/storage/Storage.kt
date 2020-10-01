package susteam.storage

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant

data class Storage(
    val uuid: String,
    val fileName: String,
    val uploader: String,
    val uploadTime: Instant,
    val isPublic: Boolean
)

fun Storage.toJson(): JsonObject = jsonObjectOf(
    "uuid" to uuid,
    "fileName" to fileName,
    "uploader" to uploader,
    "uploadTime" to uploadTime,
    "isPublic" to isPublic,
)

fun JsonObject.toStorage(): Storage = Storage(
    getString("uuid"),
    getString("fileName"),
    getString("uploader"),
    getInstant("uploadTime"),
    getBoolean("isPublic")
)