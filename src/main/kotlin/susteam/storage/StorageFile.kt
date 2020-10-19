package susteam.storage

import com.google.protobuf.ServiceException
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

interface StorageFile {
    val id: String
    val url: String
    val path: String
}

object StorageFileFactory {
    lateinit var urlPrefix: String
    lateinit var pathPrefix: String

    fun fromId(id: String): StorageFile = StorageFileImpl(id, urlPrefix, pathPrefix)

    fun fromUrl(url: String): StorageFile = if (url.startsWith(urlPrefix)) {
        StorageFileImpl(url.removePrefix("$urlPrefix/"), urlPrefix, pathPrefix)
    } else {
        throw ServiceException("cannot decode storage file from url")
    }

    fun from(string: String): StorageFile = when {
        string.startsWith("$urlPrefix/") -> fromUrl(string)
        !string.contains('/') -> fromId(string)
        else -> throw ServiceException("cannot decode storage file from url")
    }

    class StorageFileImpl(override val id: String, urlPrefix: String, pathPrefix: String) : StorageFile {
        override val url: String = "$urlPrefix/$id"
        override val path: String = "$pathPrefix/$id"
    }
}

fun String.toStorageFile() = StorageFileFactory.from(this)

fun JsonObject.getStorageFile(key: String) = this.getString(key)?.let { StorageFileFactory.from(it) }

fun JsonArray.getStorageFile(key: Int) = this.getString(key)?.let { StorageFileFactory.from(it) }