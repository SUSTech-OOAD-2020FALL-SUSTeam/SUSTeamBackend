package susteam.achievement

import com.google.inject.Inject
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import susteam.ServiceException
import susteam.game.GameRepository
import susteam.order.OrderRepository
import susteam.order.OrderStatus
import susteam.user.Auth
import susteam.user.isAdmin
import susteam.user.isDeveloper
import susteam.user.username

class AchievementService @Inject constructor(
    private val repository: AchievementRepository,
    private val gameRepository: GameRepository,
    private val orderRepository: OrderRepository
) {
    suspend fun isDeveloper(auth: Auth, gameId: Int) {
        val game = gameRepository.getById(gameId) ?: throw ServiceException("Game does not exist")

        val havePermission = when {
            auth.isAdmin() -> true
            auth.isDeveloper() -> {
                game.author == auth.username
            }
            else -> false
        }
        if (!havePermission) {
            throw ServiceException("Permission denied")
        }
    }

    suspend fun getAchievement(
        gameKey: String,
        achievementName: String
    ): Achievement {
        val gameId = gameRepository.getGameByGameKey(gameKey)?.id ?: throw ServiceException("Game does not exist")

        return repository.getAchievement(gameId, achievementName) ?: throw ServiceException("Achievement not exist")
    }

    suspend fun addAchievement(
        auth: Auth,
        gameKey: String,
        achievementName: String,
        description: String,
        achievementCount: Int
    ): Int {
        if (achievementName.isBlank()) {
            throw ServiceException("Achievement name is blank")
        } else if (achievementName.length > 255) {
            throw ServiceException("Achievement name is too long")
        } else if (description.isBlank()) {
            throw ServiceException("Description is blank")
        } else if (description.length > 255) {
            throw ServiceException("Description is too long")
        }

        val gameId = gameRepository.getGameByGameKey(gameKey)?.id ?: throw ServiceException("Game does not exist")
        isDeveloper(auth, gameId)

        return repository.addAchievement(gameId, achievementName, description, achievementCount)
    }

    suspend fun getAllAchievement(
        gameKey: String
    ): List<Achievement> {
        val gameId = gameRepository.getGameByGameKey(gameKey)?.id ?: throw ServiceException("Game does not exist")
        return repository.getAllAchievement(gameId)
    }

    suspend fun updateUserAchievementProcess(
        username: String,
        gameKey: String,
        achievementName: String,
        rateOfProcess: Int
    ) {
        val achievement = getAchievement(gameKey, achievementName)
        var finished = false
        if ( achievement.achieveCount <= rateOfProcess ) {
            finished = true
        }

        val processPermission = when {
            orderRepository.checkOrder(username, achievement.gameId) == OrderStatus.SUCCESS -> true
            else -> false
        }
        if (!processPermission) {
            throw ServiceException("Permission denied, user not own the game")
        }

        if (!repository.insertUserAchievementProcess(username, achievement.achievementId, rateOfProcess, finished)) {
            repository.changeUserAchievementProcess(username, achievement.achievementId, rateOfProcess, finished)
        }
    }

    suspend fun getUserAchievementProcess(
        username: String,
        gameKey: String,
        achievementName: String,
    ): UserAchievementProcess {
        val achievement = getAchievement(gameKey, achievementName)

        val processPermission = when {
            orderRepository.checkOrder(username, achievement.gameId) == OrderStatus.SUCCESS -> true
            else -> false
        }
        if (!processPermission) {
            throw ServiceException("Permission denied, user not own the game")
        }

        return repository.getUserAchievementProcess(username, achievement.achievementId)
            ?: UserAchievementProcess(username, achievement.achievementId, 0, false)
    }


    suspend fun getAllValuedAchievementProcess(
            username: String,
    ): List<ValuedAchievementProcess> {
        return repository.getValuedAchievementProcess(username)
    }

}
