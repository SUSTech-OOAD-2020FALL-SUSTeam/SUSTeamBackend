package susteam.storage

import com.google.inject.Inject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import susteam.CoroutineController

class StorageController @Inject constructor(private val service: StorageService) : CoroutineController() {
    override fun route(router: Router) {
        router.get("/image/*").handler(StaticHandler.create("storage/image"))
        router.get("/store/:uuid").coroutineHandler { context ->
            val request = context.request()
            val filename = service.getFileName(request.getParam("uuid"))
            context.response().putHeader("Content-Disposition", "attachment; filename=\"${filename}\"")
        }.handler(StaticHandler.create("storage/store"))
    }
}