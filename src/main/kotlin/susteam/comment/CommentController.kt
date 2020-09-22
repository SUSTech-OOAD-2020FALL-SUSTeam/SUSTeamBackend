package susteam.comment

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException

class CommentController @Inject constructor(private val service: CommentService) : CoroutineController() {

    override fun route(router: Router) {
        // XXX: Check if paths are correct.
        router.get("/comment/user/:username").coroutineHandler(::handleGetCommentsByUser)
        router.get("/comment/:gameId").coroutineHandler(::handleGetCommentsByGame)

        router.post("/comment").coroutineHandler(::handleCreateComment)
    }

    suspend fun handleGetCommentsByUser(context: RoutingContext) {
        val request = context.request()
        val username: String = request.getParam("username") ?: throw ServiceException("Username not found")

        val comments: List<Comment> = service.getCommentsByUser(username)

        // XXX: Check if the mapping is correct.
        context.success(
            jsonObjectOf(
                "comments" to JsonArray(comments.map { comment -> comment.toJson() })
            )
        )
    }

    suspend fun handleGetCommentsByGame(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toInt() ?: throw ServiceException("Game ID not found")

        val comments: List<Comment> = service.getCommentsByGame(gameId)

        // XXX: Check if the mapping is correct.
        context.success(
            jsonObjectOf(
                "comments" to JsonArray(comments.map { comment -> comment.toJson() })
            )
        )
    }

    suspend fun handleCreateComment(context: RoutingContext) {
        val params = context.bodyAsJson
        val username = params.getString("username") ?: throw ServiceException("Username is empty")
        val gameId = params.getInteger("gameId") ?: throw ServiceException("Game ID is invalid")
        val content = params.getString("content") ?: throw ServiceException("Content is empty")
        val score = params.getInteger("score") ?: throw ServiceException("Score is invalid")

        service.createComment(username, gameId, content, score)

        context.success()
    }

}