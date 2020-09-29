package susteam.storage

import com.google.inject.Inject
import io.vertx.ext.web.FileUpload

class StorageService @Inject constructor(
    private val repository: StorageRepository
) {
    suspend fun upload(file: FileUpload): String {
        val uploadPath = file.uploadedFileName()
        val fileSuffix = file.fileName().substringAfterLast('.', "")
        return repository.store(uploadPath, fileSuffix)
    }
}