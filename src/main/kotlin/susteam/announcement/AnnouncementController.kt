package susteam.announcement

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class AnnouncementController @Inject constructor(private val service: AnnouncementService) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/game/:gameId/announcements").coroutineHandler(::handleGetAnnouncementsByGame)

        router.post("/announcement").coroutineHandler(::handleCreateAnnouncement)
    }

    suspend fun handleGetAnnouncementsByGame(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val announcements: List<Announcement> = service.getAnnouncementsByGame(gameId)

        context.success(
            jsonObjectOf(
                "announcements" to JsonArray(announcements.map { announcement -> announcement.toJson() })
            )
        )
    }

    suspend fun handleCreateAnnouncement(context: RoutingContext) {
        val params = context.bodyAsJson
        val gameId = params.getInteger("gameId") ?: throw ServiceException("Game ID is invalid")
        val title = params.getString("title") ?: throw ServiceException("Title is empty")
        val content = params.getString("content") ?: throw ServiceException("Content is empty")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.createAnnouncement(auth, gameId, title, content)

        context.success()
    }

}