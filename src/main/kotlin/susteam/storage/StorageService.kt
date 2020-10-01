package susteam.storage

import com.google.inject.Inject
import io.vertx.ext.web.FileUpload
import susteam.user.Auth
import susteam.user.username
import java.nio.file.Path
import java.time.Instant

class StorageService @Inject constructor(
    private val repository: StorageRepository
) {
    // TODO: Add parameter isPublic
    suspend fun upload(auth: Auth, fileUpload: FileUpload): String {
        val uploadPath = fileUpload.uploadedFileName()
        val fileName = fileUpload.fileName()
        val fileSuffix = Path.of(fileName).toFile().extension
        val uuid = Path.of(uploadPath).toFile().name
        return if (fileUpload.contentType().startsWith("image")) {
            repository.storeImage(uploadPath, fileSuffix)
        } else {
            val currentTime = Instant.now()
            repository.record(uuid, fileName, auth.username, currentTime, false)
            repository.store(uploadPath, fileSuffix)
        }
    }
}