package susteam.record


import com.google.inject.Inject
import susteam.ServiceException
import susteam.game.GameRepository
import susteam.order.OrderRepository
import susteam.order.OrderStatus
import susteam.user.Auth
import susteam.user.isAdmin
import susteam.user.isDeveloper
import susteam.user.username

class RecordService @Inject constructor(
    private val repository: RecordRepository,
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

    suspend fun addRecord(
        username: String,
        gameKey: String,
        score: Int
    ) {
        val game = gameRepository.getGameByGameKey(gameKey)
            ?: throw ServiceException("Wrong game key")

        val gameId = game.id
        val recordPermission = when {
            orderRepository.checkOrder(username, gameId) == OrderStatus.SUCCESS -> true
            else -> false
        }

        if (!recordPermission) {
            throw ServiceException("Permission denied, user not own the game")
        }

        repository.insertRecord(gameId, username, score)
    }

    suspend fun getRank(
        gameKey: String,
        rankNum: Int
    ): List<Record> {
        val game = gameRepository.getGameByGameKey(gameKey)
            ?: throw ServiceException("Game does not exist")
        val gameId = game.id

        return repository.getRankRecord(gameId, rankNum)
    }

    suspend fun getUserMax(
        username: String,
        gameKey: String,
    ): Record {

        val game = gameRepository.getGameByGameKey(gameKey)
            ?: throw ServiceException("Wrong game key")

        val gameId = game.id
        val processPermission = when {
            orderRepository.checkOrder(username, gameId) == OrderStatus.SUCCESS -> true
            else -> false
        }
        if (!processPermission) {
            throw ServiceException("Permission denied, user not own the game")
        }

        return repository.getUserScoreMax(username, gameId)
            ?: Record(0,gameId,username, 0);
    }

}
