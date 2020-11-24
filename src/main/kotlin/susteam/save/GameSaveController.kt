package susteam.save

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.storage.StorageService
import susteam.user.username

class GameSaveController @Inject constructor(
    private val service: GameSaveService,
    private val storage: StorageService
) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/save/:username/:gameId").coroutineHandler(::handleGetAllGameSave)
        router.post("/save/:username/:gameId").coroutineHandler(::handleUploadGameSave)
        router.get("/save/:username/:gameId/:saveName/delete").coroutineHandler(::handleDeleteGameSave)
        router.get("/save/:username/:gameId/:saveName").coroutineHandler(::handleGetGameSave)
    }

    private suspend fun handleGetAllGameSave(context: RoutingContext) {
        val request = context.request()

        val auth = context.user() ?: throw ServiceException("Permission denied, please login")
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val gameSaves = service.getAllGameSaveName(auth, gameId)

        context.success(
            jsonObjectOf(
                "gameSaves" to JsonArray(gameSaves.map { it.toJson() })
            )
        )
    }

    private suspend fun handleUploadGameSave(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID is empty")

        val auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.checkBought(auth, gameId)

        if (context.fileUploads().size != 1) {
            throw ServiceException("Must uploads 1 file")
        }
        val file = context.fileUploads().firstOrNull() ?: throw ServiceException("Must uploads 1 file")

        val storageFile = storage.upload(file, auth, false)

        service.uploadGameSave(auth, gameId, context.user().username + "-" + gameId, storageFile)

        context.success(
            jsonObjectOf(
                "url" to storageFile.url
            )
        )
    }

    private suspend fun handleDeleteGameSave(context: RoutingContext) {
        val request = context.request()

        val auth = context.user() ?: throw ServiceException("Permission denied, please login")
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")
        val saveName = request.getParam("saveName")?: throw ServiceException("Save not found")

        service.deleteGameSave(auth, gameId, saveName)

        context.success()
    }

    private suspend fun handleGetGameSave(context: RoutingContext) {
        val request = context.request()

        val auth = context.user() ?: throw ServiceException("Permission denied, please login")
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")
        val saveName = request.getParam("saveName")?: throw ServiceException("Save not found")

        val storageFile = service.download(auth, gameId, saveName)

        context.put("allow-storage", storageFile.id)
        context.reroute("/api/store/${storageFile.id}")
    }
}
