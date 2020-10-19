package susteam.storage

import com.google.inject.Inject
import io.vertx.ext.web.FileUpload
import susteam.ServiceException
import susteam.user.Auth
import susteam.user.username
import java.time.Instant


class StorageService @Inject constructor(
    private val repository: StorageRepository
) {
    suspend fun upload(file: FileUpload, auth: Auth, isPublic: Boolean): StorageFile {
        val id = repository.store(file.uploadedFileName())
        repository.record(
            id, file.fileName(), auth.username, Instant.now(), isPublic
        )
        return StorageFileFactory.fromId(id)
    }

    suspend fun uploadImage(file: FileUpload): StorageImage {
        val id = repository.storeImage(file.uploadedFileName(), file.extension())
        return StorageImageFactory.fromId(id)
    }

    suspend fun getStorage(uuid: String):Storage =
        repository.getStorage(uuid) ?: throw ServiceException("File not found")

    private fun FileUpload.extension() = this.fileName().substringAfterLast('.', "")
}