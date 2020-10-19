package susteam.storage

import com.google.inject.Inject
import io.vertx.core.http.impl.MimeMapping
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import susteam.CoroutineController
import susteam.ServiceException

class StorageController @Inject constructor(private val service: StorageService) : CoroutineController() {
    override fun route(router: Router) {
        router.get("/image/*").handler(StaticHandler.create("storage/image"))
        router.get("/store/:uuid").coroutineHandler { context ->
            val request = context.request()
            val storage = service.getStorage(request.getParam("uuid"))

            if (storage.isPublic || context.get<String>("allow-storage") == storage.uuid) {
                val filename = storage.fileName
                val response = context.response()
                response.putHeader("Content-Disposition", """attachment; filename="$filename"""")
                response.putHeader(
                    "Content-Type",
                    MimeMapping.getMimeTypeForFilename(filename) ?: "application/octet-stream"
                )
                context.next()
            } else {
                // TODO no permission error handle
                throw ServiceException("Download permission denied")
            }
        }.handler(StaticHandler.create("storage/store"))
    }
}