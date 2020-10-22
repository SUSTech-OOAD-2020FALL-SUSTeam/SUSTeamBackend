package susteam.order


import com.google.inject.Inject
import io.vertx.core.Vertx
import susteam.ServiceException
import susteam.game.Game
import susteam.game.GameRepository
import susteam.user.Auth
import susteam.user.User
import susteam.user.UserRepository
import susteam.user.username
import java.time.Instant

class OrderService @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository,
    val vertx:Vertx
) {

    suspend fun getOrderbyUsername(username: String): List<Order> {
        return orderRepository.getOrderbyUsername(username)
    }

    suspend fun getOrderbyGameId(gameId: Int): List<Order> {
        return orderRepository.getOrderbyGameId(gameId)
    }

    suspend fun createOrder(
        auth: Auth,
        gameId: Int,
        price: Int
    ): Status {
        val game: Game = gameRepository.getById(gameId) ?: throw ServiceException("Game does not exist")

        val costumer: User = userRepository.get(auth.username) ?: throw ServiceException("Unknown error")

        val haveBoughtStatus: Status = orderRepository.checkOrder(costumer.username, gameId)
        println(haveBoughtStatus)
        if( haveBoughtStatus == Status.SUCCESS ) {
            throw ServiceException("User '${costumer.username}' have already bought '${game.name}'")
        }

        val author: User = userRepository.get(game.author) ?: throw ServiceException("Unknown error")

        val purchaseTime: Instant = Instant.now()

        val status = orderRepository.transaction { conn ->
            orderRepository.createOrder(conn, haveBoughtStatus, costumer, author, game, purchaseTime, price)
        }

        if( status ) return Status.REFUNDABLE
        return Status.FAIL
    }
}
