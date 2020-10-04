package susteam.storage

import com.google.inject.Inject
import io.vertx.core.file.FileSystem
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.file.existsAwait
import io.vertx.kotlin.core.file.moveAwait
import io.vertx.kotlin.core.file.readFileAwait
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant


class StorageRepository @Inject constructor(
    private val fileSystem: FileSystem,
    private val database: JDBCClient
) {
    // TODO: Read from config file
    private val storeRoot = "storage/store"
    private val imageRoot = "storage/image"

    init {
        fileSystem.mkdirsBlocking(storeRoot)
        fileSystem.mkdirsBlocking(imageRoot)
    }

    suspend fun record(
        uuid: String,
        fileName: String,
        uploader: String,
        uploadTime: Instant,
        isPublic: Boolean
    ) {
        try {
            database.updateWithParamsAwait(
                """
                    INSERT INTO `storage` (uuid, file_name, uploader, upload_time, is_public) 
                    VALUES (?, ?, ?, ?, ?);
                """.trimIndent(),
                jsonArrayOf(uuid, fileName, uploader, uploadTime.toString(), isPublic)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            throw ServiceException("Failed to record storage", e)
        }
    }

    suspend fun store(uploadPath: String, fileSuffix: String) = storeImpl(uploadPath, fileSuffix, storeRoot)

    suspend fun storeImage(uploadPath: String): String {
        val md5 = md5(fileSystem.readFileAwait(uploadPath).bytes)
        val storePath = Path.of(imageRoot, md5).toString()

        if (!fileSystem.existsAwait(storePath)) {
            fileSystem.moveAwait(uploadPath, storePath)
        }
        return md5
    }

    suspend fun storeImpl(uploadPath: String, fileSuffix: String, rootPath: String): String {
        val filename = Path.of(uploadPath).fileName.toString().let {
            if (fileSuffix.isNotBlank()) {
                "$it.$fileSuffix"
            } else {
                it
            }
        }
        val storePath = Path.of(rootPath, filename).toString()

        fileSystem.moveAwait(uploadPath, storePath)
        return storePath
    }

    fun md5(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(bytes)
        val digest = md.digest()
        return digest.joinToString("") { String.format("%02X", it) }.take(32)
    }
}
