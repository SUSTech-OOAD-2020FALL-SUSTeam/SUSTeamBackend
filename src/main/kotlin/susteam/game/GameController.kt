package susteam.game

import com.google.inject.Inject
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.file.readFileAwait
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.storage.StorageService
import susteam.storage.getStorageFile
import susteam.user.Auth
import susteam.user.isAdmin
import susteam.user.username
import javax.imageio.ImageIO


class GameController @Inject constructor(
    private val service: GameService,
    private val fileSystem: FileSystem,
    private val storage: StorageService
) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/game/:gameId").coroutineHandler(::handleGetGame)
        router.get("/game/:gameId/profile").coroutineHandler(::handleGetGameProfile)
        router.get("/game/:gameId/detail").coroutineHandler(::handleGetGameDetail)
        router.post("/game").coroutineHandler(::handlePublishGame)
        router.put("/game/:gameId").coroutineHandler(::handleUpdateDescription)
        router.get("/games").coroutineHandler(::handleGetAllGames)
        router.get("/games/recommend").coroutineHandler(::handleGetRecommendGames)

        router.get("/game/:gameId/version/:versionName").coroutineHandler(::handleGetVersion)
        router.get("/game/:gameId/version").coroutineHandler(::handleGetNewestVersion)
        router.post("/game/:gameId/version").coroutineHandler(::handlePublishGameVersion)
        router.post("/game/:gameId/upload").coroutineHandler(::handleUploadGameVersion)
        router.get("/game/:gameId/version/:versionName/download").coroutineHandler(::handleDownloadGameVersion)

        router.post("/game/:gameId/image").coroutineHandler(::handleUploadGameImage)

        router.get("/game/:gameId/tags").coroutineHandler(::handleGetTags)
        router.get("/tags").coroutineHandler(::handleGetAllTag)
        router.get("/games/tags").coroutineHandler(::handleGetGameProfileWithTags)
        router.post("/game/:gameId/tag").coroutineHandler(::handleAddTag)

    }

    private suspend fun handleGetGame(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val game: Game = service.getGame(gameId)

        context.success(
            jsonObjectOf(
                "game" to game.toJson()
            )
        )
    }

    private suspend fun handleGetGameProfile(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val gameProfile: GameProfile = service.getGameProfile(gameId)

        context.success(
            jsonObjectOf(
                "gameProfile" to gameProfile.toJson()
            )
        )
    }

    private suspend fun handleGetGameDetail(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val gameDetail: GameDetail = service.getGameDetail(gameId)

        context.success(
            jsonObjectOf(
                "gameDetail" to gameDetail.toJson()
            )
        )
    }

    private suspend fun handlePublishGame(context: RoutingContext) {
        val params = context.bodyAsJson
        val name = params.getString("name") ?: throw ServiceException("Game name is empty")
        val price = params.getInteger("price") ?: throw ServiceException("Price is empty")
        val introduction = params.getString("introduction")
        val description = params.getString("description")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.publishGame(auth, name, price, introduction, description)

        context.success()
    }

    suspend fun handleUpdateDescription(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID is empty")

        val params = context.bodyAsJson
        val description = params.getString("description")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.updateDescription(auth, gameId, description)

        context.success()
    }

    suspend fun handleGetAllGames(context: RoutingContext) {
        val request = context.request()
        val order = request.getParam("order")

        val gamesList: List<GameProfile> = when (order) {
            "publishDate" -> {
                service.getAllGameProfileOrderByPublishDate()
            }
            "name" -> {
                service.getAllGameProfileOrderByName()
            }
            else -> {
                service.getAllGameProfile()
            }
        }

        context.success(
            jsonObjectOf(
                "games" to JsonArray(gamesList.map { it.toJson() })
            )
        )
    }

    suspend fun handleGetRecommendGames(context: RoutingContext) {
        val request = context.request()
        val numberOfGames = request.getParam("number")?.toIntOrNull() ?: 6

        val gamesList: List<GameProfile> = service.getRandomGameProfile(numberOfGames)

        context.success(
            jsonObjectOf(
                "games" to JsonArray(gamesList.map { it.toJson() })
            )
        )
    }


    suspend fun handlePublishGameVersion(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID is empty")

        val params = context.bodyAsJson
        val versionName = params.getString("name") ?: throw ServiceException("Game version name is empty")
        val url = params.getStorageFile("url") ?: throw ServiceException("URL is empty")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.publishGameVersion(auth, gameId, versionName, url)

        context.success()
    }

    suspend fun handleGetVersion(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")
        val versionName = request.getParam("versionName") ?: throw ServiceException("Version name not found")

        val gameVersion: GameVersion = service.getGameVersion(gameId, versionName)

        context.success(
            jsonObjectOf(
                "gameVersion" to gameVersion.toJson()
            )
        )
    }

    suspend fun handleGetNewestVersion(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val gameVersion: GameVersion = service.getNewestVersion(gameId)

        context.success(
            jsonObjectOf(
                "gameVersion" to gameVersion.toJson()
            )
        )
    }

    suspend fun handleUploadGameVersion(context: RoutingContext) {

        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID is empty")

        val user = context.user() ?: throw ServiceException("Permission denied, please login")
        val game = service.getGame(gameId)

        if (!user.isAdmin() && game.author != user.username) {
            throw ServiceException("Permission denied")
        }

        if (context.fileUploads().size != 1) {
            throw ServiceException("Must uploads 1 file")
        }
        val file = context.fileUploads().firstOrNull() ?: throw ServiceException("Must uploads 1 file")

        val storageFile = storage.upload(file, user, false)

        context.success(
            jsonObjectOf(
                "url" to storageFile.url
            )
        )
    }

    suspend fun handleDownloadGameVersion(context: RoutingContext) {

        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID is empty")
        val versionName = request.getParam("versionName") ?: throw ServiceException("Game version name is empty")
        val auth = context.user() ?: throw ServiceException("Permission denied, please login")

        val storageFile = service.download(auth, gameId, versionName)

        context.put("allow-storage", storageFile.id)
        context.reroute("/api/store/${storageFile.id}")
    }


    suspend fun handleUploadGameImage(context: RoutingContext) {
        val gameId = context.request().getParam("gameId")?.toIntOrNull()
            ?: throw ServiceException("Game ID is empty")

        val user = context.user() ?: throw ServiceException("Permission denied, please login")
        val game = service.getGame(gameId)

        if (!user.isAdmin() && game.author != user.username) {
            throw ServiceException("Permission denied")
        }

        val images = context.fileUploads().filter {
            val file = fileSystem.readFileAwait(it.uploadedFileName()).bytes
            val image = ImageIO.read(file.inputStream()) ?: throw ServiceException("Cannot decode image")

            val (width, height) = when (it.name()) {
                "FullSize" -> 1920 to 1080
                "CardSize" -> 640 to 854
                else -> return@filter true
            }

            if (image.width != width || image.height != height) {
                throw ServiceException("Image size error, it should be ${width}*${height}")
            }
            return@filter true
        }

        val stores = images.map {
            val type = when (it.name()) {
                "FullSize" -> "F"
                "CardSize" -> "C"
                else -> "N"
            }
            val store = storage.uploadImage(it)
            service.uploadGameImage(gameId, store, type)
            store
        }

        context.success(
            jsonObjectOf(
                "images" to JsonArray(stores.map { it.url })
            )
        )

    }

    private suspend fun handleGetTags(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val tags: List<String> = service.getTag(gameId)

        context.success(
            jsonObjectOf(
                "tags" to JsonArray(tags.map { it })
            )
        )
    }

    private suspend fun handleGetAllTag(context: RoutingContext) {
        val tags: List<String> = service.getAllTag()

        context.success(
            jsonObjectOf(
                "tags" to JsonArray(tags.map { it })
            )
        )
    }

    suspend fun handleGetGameProfileWithTags(context: RoutingContext) {
        val tags: List<String> = context.queryParam("tag")
        val gamesList: List<GameProfile> = service.getGameProfileWithTags(tags)

        context.success(
            jsonObjectOf(
                "games" to JsonArray(gamesList.map { it.toJson() })
            )
        )
    }

    suspend fun handleAddTag(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID is empty")

        val params = context.bodyAsJson
        val tag = params.getString("tag") ?: throw ServiceException("Tag is empty")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.addTag(auth, gameId, tag)

        context.success()
    }

}
