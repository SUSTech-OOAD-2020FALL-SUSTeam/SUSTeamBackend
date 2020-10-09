package susteam.storage

import com.google.inject.Inject
import susteam.ServiceException
import susteam.user.Auth
import susteam.user.username
import java.time.Instant

class StorageService @Inject constructor(
    private val repository: StorageRepository,
    private val imageFactory: StorageImageFactory
) {
    suspend fun upload(file: FileStorage, auth: Auth, isPublic: Boolean): String {
        return if (file.isImage) {
            repository.storeImage(file.uploadName, file.suffix)
        } else {
            repository.record(
                file.uuid, file.fileName, auth.username, Instant.now(), isPublic
            )
            repository.store(file.uploadName)
        }
    }

    suspend fun uploadImage(file: FileStorage): StorageImage {
        val id = repository.storeImage(file.uploadName, file.suffix)
        return imageFactory.fromId(id)
    }

    suspend fun getFileName(uuid: String) =
        repository.getFileName(uuid) ?: throw ServiceException("File not found")
}