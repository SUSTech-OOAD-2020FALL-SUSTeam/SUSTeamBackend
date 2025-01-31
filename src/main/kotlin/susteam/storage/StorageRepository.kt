package susteam.storage

import com.google.inject.Inject
import io.vertx.core.file.FileSystem
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.file.existsAwait
import io.vertx.kotlin.core.file.moveAwait
import io.vertx.kotlin.core.file.readFileAwait
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import java.nio.file.Path
import java.security.MessageDigest
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant
import java.util.*


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

    suspend fun getStorage(uuid: String): Storage? {
        return database.querySingleWithParamsAwait(
            """
                SELECT uuid, file_name, uploader, upload_time, is_public
                FROM storage
                WHERE uuid = ?;
            """.trimIndent(), jsonArrayOf(uuid)
        )?.let {
            Storage(
                it.getString(0), it.getString(1), it.getString(2),
                it.getInstant(3), it.getBoolean(4)
            )
        }
    }

    suspend fun store(uploadPath: String): String {
        val id = UUID.randomUUID().toString()
        val storePath = Path.of(storeRoot, id).toString()
        fileSystem.moveAwait(uploadPath, storePath)
        return id
    }

    suspend fun storeImage(uploadPath: String, extension: String = ""): String {
        val md5 = md5(fileSystem.readFileAwait(uploadPath).bytes)
        val id = if (extension.isBlank()) md5 else "${md5}.${extension}"
        val storePath = Path.of(imageRoot, id).toString()

        if (!fileSystem.existsAwait(storePath)) {
            fileSystem.moveAwait(uploadPath, storePath)
        }
        return id
    }

    private fun md5(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        md.update(bytes)
        val digest = md.digest()
        return digest.joinToString("") { String.format("%02X", it) }.take(32)
    }
}
