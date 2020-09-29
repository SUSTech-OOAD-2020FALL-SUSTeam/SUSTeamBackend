package susteam.storage

import com.google.inject.Inject
import io.vertx.core.Vertx
import io.vertx.kotlin.core.file.moveAwait

class StorageRepository @Inject constructor(
    private val vertx: Vertx
) {
    suspend fun store(uploadPath: String, fileSuffix: String): String {
        val fileSystem = vertx.fileSystem()
        val storePath = uploadPath.replace("file-uploads", "store") + "." + fileSuffix
        fileSystem.moveAwait(uploadPath, storePath)
        return storePath
    }
}
