package susteam.commentThumb

import com.google.inject.Inject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth
import susteam.user.username

class CommentThumbController @Inject constructor(
    private val service: CommentThumbService
) : CoroutineController() {

    override fun route(router: Router) {
        router.post("/commentThumb").coroutineHandler(::handleSetCommentThumb)
        router.get("/commentThumb/:commenter/:gameId").coroutineHandler(::handleGetCommentThumbSum)
        router.get("/game/:gameId/commentThumb").coroutineHandler(::handleGetCommentThumbByGame)
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

        val auth: Auth? = context.user()

        val commentThumbs = if (auth != null) service.getCommentThumbByGame(gameId, auth.username) else emptyList()
        val thumbSummary = service.getCommentThumbSumByGame(gameId)

        context.success(
            jsonObjectOf(
                "commentThumbs" to commentThumbs.map { it.toJson() },
                "thumbSummary" to thumbSummary.map {
                    jsonObjectOf(
                        "commenter" to it.first,
                        "upvote" to it.second,
                        "downvote" to it.third
                    )
                }
            )
        )

    }

}