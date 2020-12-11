package susteam.achievement

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class AchievementController @Inject constructor(private val service: AchievementService) : CoroutineController() {
    override fun route(router: Router) {
        router.get("/achievement/:gameId/:achievementName").coroutineHandler(::handleGetAchievement)
        router.post("/achievement/:gameId").coroutineHandler(::handleAddAchievement)
        router.get("/achievement/:gameId").coroutineHandler(::handleGetAllAchievement)
        router.post("/achieveProcess/:gameKey").coroutineHandler(::handleUpdateUserAchievementProcess)
        router.get("/achieveProcess/:username/:gameId/:achievementName").coroutineHandler(::handleGetUserAchievementProcess)
        router.get("/valuedAchieveProcess/:username").coroutineHandler(::handleGetValuedAchievementProcess)
    }

    suspend fun handleGetAchievement(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")
        val achievementName =
            request.getParam("achievementName") ?: throw ServiceException("Achievement name not found")

        val achievement = service.getAchievement(gameId, achievementName)
        context.success(
            jsonObjectOf(
                "achievement" to achievement.toJson()
            )
        )
    }

    suspend fun handleAddAchievement(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val params = context.bodyAsJson
        val achievementName =
            params.getString("achievementName") ?: throw ServiceException("Achievement name not found")
        val description = params.getString("description") ?: throw ServiceException("Description not found")
        val achievementCount =
            params.getInteger("achievementCount") ?: throw ServiceException("achievementCount not found")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        val achievementId = service.addAchievement(auth, gameId, achievementName, description, achievementCount)

        context.success(
            jsonObjectOf(
                "achievementId" to achievementId
            )
        )
    }

    suspend fun handleGetAllAchievement(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val achievements = service.getAllAchievement(gameId)
        context.success(
            jsonObjectOf(
                "achievements" to JsonArray(achievements.map { it })
            )
        )
    }

    suspend fun handleUpdateUserAchievementProcess(context: RoutingContext) {
        val request = context.request()

        val gameKey = request.getParam("gameKey") ?: throw ServiceException("Game Key not found")

        val params = context.bodyAsJson
        val username = params.getString("username") ?: throw ServiceException("Username not found")
        val achievementName =
            params.getString("achievementName") ?: throw ServiceException("Achievement name not found")
        val rateOfProcess =
            params.getInteger("rateOfProcess") ?: throw ServiceException("Rate of process not found")


        service.updateUserAchievementProcess(username, gameKey, achievementName, rateOfProcess)
        context.success()
    }

    suspend fun handleGetUserAchievementProcess(context: RoutingContext) {
        val request = context.request()

        val username = request.getParam("username") ?: throw ServiceException("Username not found")
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")
        val achievementName =
                request.getParam("achievementName") ?: throw ServiceException("Achievement name not found")

        val userAchievementProcess = service.getUserAchievementProcess(username, gameId, achievementName)

        context.success(
                jsonObjectOf(
                        "userAchievementProcess" to userAchievementProcess.toJson()
                )
        )
    }

    suspend fun handleGetValuedAchievementProcess(context: RoutingContext) {
        val request = context.request()

        val username = request.getParam("username") ?: throw ServiceException("Username not found")

        val valuedAchievementProcess = service.getAllValuedAchievementProcess(username)

        context.success(
                jsonObjectOf(
                        "ValuedAchievementProcess" to JsonArray(valuedAchievementProcess.map { it })
                )
        )
    }
}
