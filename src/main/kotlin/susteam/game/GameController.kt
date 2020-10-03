package susteam.game

import com.google.inject.Inject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class GameController @Inject constructor(private val service: GameService) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/game/:gameId").coroutineHandler(::handleGetGame)
        router.get("/game/:gameId/profile").coroutineHandler(::handleGetGameProfile)
        router.get("/game/:gameId/detail").coroutineHandler(::handleGetGameDetail)
        router.post("/game").coroutineHandler(::handlePublishGame)
        router.put("/game/:gameId").coroutineHandler(::handleUpdateDescription)
        router.get("/games/").coroutineHandler(::handleGetAllGames)
        router.get("/games/recommend").coroutineHandler(::handleGetRecommendGames)

        router.get("/game/:gameId/version/:versionName").coroutineHandler(::handleGetVersion)
        router.post("/game/:gameId/version").coroutineHandler(::handlePublishGameVersion)
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

    suspend fun handlePublishGameVersion(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID is empty")

        val params = context.bodyAsJson
        val versionName = params.getString("name") ?: throw ServiceException("Game version name is empty")
        val url = params.getString("url") ?: throw ServiceException("URL is empty")

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

    suspend fun handleGetAllGames(context: RoutingContext) {
        val request = context.request()
        val order = request.getParam("order")
        println(order) // TODO delete it

        val gamesList: List<GameProfile> = when (order) {
            "publishDate" -> { service.getAllGameProfileOrderByPublishDate() }
            "name" -> { service.getAllGameProfileOrderByName() }
            else -> { service.getAllGameProfile()  }
        }

        context.success(
            jsonObjectOf(
                "games" to gamesList
            )
        )
    }

    suspend fun handleGetRecommendGames(context: RoutingContext) {
        val request = context.request()
        val numberOfGames = request.getParam("number")?.toIntOrNull() ?: 6

        val gamesList: List<GameProfile> = service.getRandomGameProfile(numberOfGames)

        context.success(
            jsonObjectOf(
                "games" to gamesList
            )
        )
    }

}