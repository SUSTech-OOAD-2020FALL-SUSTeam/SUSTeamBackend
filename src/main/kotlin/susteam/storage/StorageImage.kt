package susteam.storage

import com.google.protobuf.ServiceException
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

interface StorageImage {
    val id: String
    val url: String
    val path: String
}

object StorageImageFactory {
    lateinit var urlPrefix: String
    lateinit var pathPrefix: String

    fun fromId(id: String): StorageImage = StorageImageImpl(id, urlPrefix, pathPrefix)

    fun fromUrl(url: String): StorageImage = if (url.startsWith(urlPrefix)) {
        StorageImageImpl(url.removePrefix("$urlPrefix/"), urlPrefix, pathPrefix)
    } else {
        throw ServiceException("cannot decode storage image from url")
    }

    fun from(string: String): StorageImage = when {
        string.startsWith("$urlPrefix/") -> fromUrl(string)
        !string.contains('/') -> fromId(string)
        else -> throw ServiceException("cannot decode storage image from url")
    }

    class StorageImageImpl(override val id: String, urlPrefix: String, pathPrefix: String) : StorageImage {
        override val url: String = "$urlPrefix/$id"
        override val path: String = "$pathPrefix/$id"
    }
}

fun String.toStorageImage() = StorageImageFactory.from(this)

fun JsonObject.getStorageImage(key: String) = StorageImageFactory.from(this.getString(key))

fun JsonArray.getStorageImage(key: Int) = StorageImageFactory.from(this.getString(key))