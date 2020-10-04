package susteam.storage

import com.google.inject.Inject
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.http.sendFileAwait
import io.vertx.kotlin.core.json.jsonObjectOf
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class StorageController @Inject constructor(
    private val service: StorageService,
    private val imageFactory: StorageImageFactory,
    private val vertx: Vertx
) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/image/:id").coroutineHandler(::handleGetImage)
        router.post("/image").coroutineHandler(::handleStoreImage)

        router.get("/store/*").handler(StaticHandler.create("storage/store"))
        router.post("/store").coroutineHandler(::handleStorePrivate)
        router.post("/storePublic").coroutineHandler(::handleStorePublic)
    }

    private suspend fun handleGetImage(context: RoutingContext) {
        val id = context.request().getParam("id") ?: throw ServiceException("Filename not exist")
        val image = imageFactory.fromId(id)

        //TODO give a exact MIME Type
        context.response().putHeader("Content-Type", "image/jpeg")
        context.response().sendFileAwait(image.path)
    }

    suspend fun handleStoreImage(context: RoutingContext) {
        val rerouteRequest: Boolean? = context["storage-request"]
        if (rerouteRequest != true) {
            context.response().end()
            return
        }
        val result = context.fileUploads()
            .map { GlobalScope.async { service.uploadImage(it) } }
            .map { it.await() }

        context.put("storage-result", result)
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