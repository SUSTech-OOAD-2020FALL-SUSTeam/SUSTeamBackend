package susteam.storage

import com.google.inject.Inject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class StorageController @Inject constructor(private val service: StorageService) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/store/*").handler(StaticHandler.create("store"))
        router.post("/store").coroutineHandler(::handleStorePrivate)
        router.post("/storePublic").coroutineHandler(::handleStorePublic)
    }

    suspend fun handleStoreImpl(context: RoutingContext, isPublic: Boolean) {
        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")
        val urlList = context.fileUploads().map {
            service.upload(it, auth, isPublic)
        }

        context.success(
            jsonObjectOf(
                "url" to urlList
            )
        )
    }

    suspend fun handleStorePublic(context: RoutingContext) = handleStoreImpl(context, true)
    suspend fun handleStorePrivate(context: RoutingContext) = handleStoreImpl(context, false)
}