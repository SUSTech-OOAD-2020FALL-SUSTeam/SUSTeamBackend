package susteam.achievement

import com.google.inject.Inject
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
        gameId: Int,
        achievementName: String
    ): Achievement {
        return repository.getAchievement(gameId, achievementName) ?: throw ServiceException("Achievement not exist")
    }

    suspend fun addAchievement(
        auth: Auth,
        gameId: Int,
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

        isDeveloper(auth, gameId)

        return repository.addAchievement(gameId, achievementName, description, achievementCount)
    }

    suspend fun getAllAchievement(
        gameId: Int
    ): List<Achievement> {
        return repository.getAllAchievement(gameId)
    }

    suspend fun updateUserAchievementProcess(
        auth: Auth,
        username: String,
        gameId: Int,
        achievementName: String,
        rateOfProcess: Int
    ) {
        isDeveloper(auth, gameId)

        val achievement = getAchievement(gameId, achievementName)
        var finished = false
        if ( achievement.achieveCount <= rateOfProcess ) {
            finished = true
        }

        val processPermission = when {
            orderRepository.checkOrder(username, gameId) == OrderStatus.SUCCESS -> true
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
        gameId: Int,
        achievementName: String,
    ): UserAchievementProcess {
        val achievement = getAchievement(gameId, achievementName)

        val processPermission = when {
            orderRepository.checkOrder(username, gameId) == OrderStatus.SUCCESS -> true
            else -> false
        }
        if (!processPermission) {
            throw ServiceException("Permission denied, user not own the game")
        }

        return repository.getUserAchievementProcess(username, achievement.achievementId)
            ?: UserAchievementProcess(username, achievement.achievementId, 0, false)
    }

}
