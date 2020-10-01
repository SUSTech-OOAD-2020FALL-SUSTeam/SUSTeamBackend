package susteam.storage

import com.google.inject.Inject
import susteam.user.Auth
import susteam.user.username
import java.time.Instant

class StorageService @Inject constructor(
    private val repository: StorageRepository
) {
    suspend fun upload(file: FileStorage, auth: Auth, isPublic: Boolean): String {
        return if (file.isImage) {
            repository.storeImage(file.uploadName, file.suffix)
        } else {
            repository.record(
                file.uuid, file.fileName, auth.username, Instant.now(), isPublic
            )
            repository.store(file.uploadName, file.suffix)
        }
    }
}