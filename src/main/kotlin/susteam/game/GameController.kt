package susteam.game

import com.google.inject.Inject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException

class GameController @Inject constructor(private val service: GameService) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/game/:gameName").coroutineHandler(::handleGetGame)
        router.get("/game/:gameName/version/:versionName").coroutineHandler(::handleGetVersion)

        router.post("/game").coroutineHandler(::handlePublishGame)
        router.post("/gameVersion").coroutineHandler(::handlePublishGameVersion)
        router.post("/game").coroutineHandler(::handleUpdateDescription)
        // TODO 这几个path我不清楚我写的对不对
    }

    suspend fun handlePublishGame(context: RoutingContext) {
        val params = context.bodyAsJson
        val gameName = params.getString("gameName") ?: throw ServiceException("Game name is empty")
        val price = params.getInteger("price") ?: throw ServiceException("Price is empty")
        val author = params.getString("author") ?: throw ServiceException("Author is empty")
        val description = params.getString("description")

        service.publishGame(gameName, price, author, description)

        context.success()
    }

    suspend fun handlePublishGameVersion(context: RoutingContext) {
        val params = context.bodyAsJson
        val gameId = params.getInteger("gameId") ?: throw ServiceException("Game id is empty")
        val versionName = params.getString("versionName") ?: throw ServiceException("Game version name is empty")
        val url = params.getString("url") ?: throw ServiceException("Url is empty")

        service.publishGameVersion(gameId, versionName, url)

        context.success()
    }

    suspend fun handleUpdateDescription(context: RoutingContext) {
        val params = context.bodyAsJson
        val gameId = params.getInteger("gameId") ?: throw ServiceException("Game id is empty")
        val description = params.getString("description")

        service.updateDescription(gameId, description)

        context.success()
    }

    suspend fun handleGetGame(context: RoutingContext) {
        val params = context.bodyAsJson
        val gameId = params.getInteger("gameId") ?: throw ServiceException("Game id not found")

        val game: Game = service.getGame(gameId)

        context.success(
                jsonObjectOf(
                        "game" to game.toJson()
                )
        )
    }

    suspend fun handleGetVersion(context: RoutingContext) {
        val params = context.bodyAsJson
        val gameId = params.getInteger("gameId") ?: throw ServiceException("Game id not found")
        val versionName = params.getString("versionName") ?: throw ServiceException("Version name not found")

        val gameVersion: GameVersion = service.getGameVersion(gameId, versionName)

        context.success(
                jsonObjectOf(
                        "gameVersion" to gameVersion.toJson()
                )
        )
    }

}