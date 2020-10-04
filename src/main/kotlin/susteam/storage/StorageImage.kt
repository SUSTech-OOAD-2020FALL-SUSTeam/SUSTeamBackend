package susteam.storage

import com.google.protobuf.ServiceException

interface StorageImage {
    val id: String
    val url: String
    val path: String
}

class StorageImageFactory(private val urlPrefix: String, private val pathPrefix: String) {
    fun fromId(id: String): StorageImage = StorageImageImpl(id, urlPrefix, pathPrefix)

    fun fromUrl(url: String): StorageImage = if (url.startsWith(urlPrefix)) {
        StorageImageImpl(url.removePrefix("$urlPrefix/"), urlPrefix, pathPrefix)
    } else {
        throw ServiceException("cannot decode storage image from url")
    }

    class StorageImageImpl(override val id: String, urlPrefix: String, pathPrefix: String) : StorageImage {
        override val url: String = "$urlPrefix/$id"
        override val path: String = "$pathPrefix/$id"
    }
}