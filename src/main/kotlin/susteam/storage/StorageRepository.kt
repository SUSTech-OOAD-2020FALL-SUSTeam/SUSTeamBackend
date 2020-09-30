package susteam.storage

import com.google.inject.Inject
import io.vertx.core.file.FileSystem
import io.vertx.kotlin.core.file.moveAwait
import java.nio.file.Path

class StorageRepository @Inject constructor(
    private val fileSystem: FileSystem
) {
    private val storeRoot = "store"

    init {
        fileSystem.mkdirsBlocking(storeRoot)
    }

    suspend fun store(uploadPath: String, fileSuffix: String): String {
        val filename = Path.of(uploadPath).fileName.toString().let {
            if (fileSuffix.isNotBlank()) {
                "$it.$fileSuffix"
            } else {
                it
            }
        }
        val storePath = Path.of(storeRoot, filename).toString()

        fileSystem.moveAwait(uploadPath, storePath)
        return storePath
    }
}
