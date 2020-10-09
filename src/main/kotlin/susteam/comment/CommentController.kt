package susteam.comment

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class CommentController @Inject constructor(private val service: CommentService) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/user/:username/comment").coroutineHandler(::handleGetCommentsByUser)
        router.get("/game/:gameId/comment").coroutineHandler(::handleGetCommentsByGame)

        router.post("/comment").coroutineHandler(::handleCreateComment)

        router.put("/comment").coroutineHandler(::handleEditComment)
    }

    suspend fun handleGetCommentsByUser(context: RoutingContext) {
        val request = context.request()
        val username: String = request.getParam("username") ?: throw ServiceException("Username not found")

        val comments: List<Comment> = service.getCommentsByUser(username)

        context.success(
            jsonObjectOf(
                "comments" to JsonArray(comments.map { it.toJson() })
            )
        )
    }

    suspend fun handleGetCommentsByGame(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val comments: List<Comment> = service.getCommentsByGame(gameId)

        context.success(
            jsonObjectOf(
                "comments" to JsonArray(comments.map { it.toJson() })
            )
        )
    }

    suspend fun handleCreateComment(context: RoutingContext) {
        val params = context.bodyAsJson
        val gameId = params.getInteger("gameId") ?: throw ServiceException("Game ID is invalid")
        val content = params.getString("content") ?: throw ServiceException("Content is empty")
        val score = params.getInteger("score") ?: throw ServiceException("Score is invalid")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.createComment(auth, gameId, content, score)

        context.success()
    }

    suspend fun handleEditComment(context: RoutingContext) {
        val params = context.bodyAsJson
        val gameId = params.getInteger("gameId") ?: throw ServiceException("Game ID is invalid")
        val content = params.getString("content") ?: throw ServiceException("Content is empty")
        val score = params.getInteger("score") ?: throw ServiceException("Score is invalid")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.modifyComment(auth, gameId, content, score)

        context.success()
    }

}