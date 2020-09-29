package susteam.storage

import com.google.inject.Inject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class StorageController @Inject constructor(private val service: StorageService) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/store/*").handler(StaticHandler.create("store"))
        router.post("/store").coroutineHandler(::handleStore)
    }

    suspend fun handleStore(context: RoutingContext) {
        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")
        //TODO: 鉴权
        val fileUploads = context.fileUploads()
        val urlList = ArrayList<String>()
        for (file in fileUploads) {
            urlList.add(service.upload(file))
        }

        context.success(
            jsonObjectOf(
                "url" to urlList
            )
        )
    }
}