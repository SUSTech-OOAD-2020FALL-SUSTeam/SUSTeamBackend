package susteam.commentThumb

import com.google.inject.Inject
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class CommentThumbController @Inject constructor(
    private val service: CommentThumbService
) : CoroutineController() {

    override fun route(router: Router) {
        router.post("/commentThumb").coroutineHandler(::handleSetCommentThumb)
        router.get("/commentThumb/:commenter/:gameId").coroutineHandler(::handleGetCommentThumbSum)
        router.get("/game/:gameId/commentThumb/:username").coroutineHandler(::handleGetCommentThumbByGame)
    }


    suspend fun handleSetCommentThumb(context: RoutingContext) {
        val params = context.bodyAsJson
        val gameId = params.getInteger("gameId") ?: throw ServiceException("Game ID is invalid")
        val commenter = params.getString("commenter") ?: throw ServiceException("commenter is invalid")
        val voteNum = params.getInteger("vote") ?: throw ServiceException("vote is invalid")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.setCommentThumb(auth, gameId, commenter, voteNum)

        context.success()
    }

    suspend fun handleGetCommentThumbSum(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")
        val commenter = request.getParam("commenter") ?: throw ServiceException("commenter not found")

        val sum = service.getCommentThumbSum(gameId, commenter)

        context.success(
            jsonObjectOf(
                "voteSum" to sum
            )
        )

    }

    suspend fun handleGetCommentThumbByGame(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")
        val username = request.getParam("username") ?: throw ServiceException("username not found")

        val list = service.getCommentThumbByGame(gameId, username)

        context.success(
            jsonObjectOf(
                "commentThumbs" to list.map { it.toJson() }
            )
        )

    }

}