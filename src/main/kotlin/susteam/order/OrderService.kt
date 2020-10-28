package susteam.order


import com.google.inject.Inject
import io.vertx.core.Vertx
import susteam.ServiceException
import susteam.game.Game
import susteam.game.GameRepository
import susteam.repository.RepositoryProvider
import susteam.user.Auth
import susteam.user.User
import susteam.user.UserRepository
import susteam.user.username
import java.time.Instant

class OrderService @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository,
    private val orderRepositoryProvider: RepositoryProvider<OrderRepository>,
    private val userRepositoryProvider: RepositoryProvider<UserRepository>,
    val vertx: Vertx
) {

    suspend fun getOrderByUsername(username: String): List<Order> {
        return orderRepository.getOrderByUsername(username)
    }

    suspend fun getOrderByGameId(gameId: Int): List<Order> {
        return orderRepository.getOrderByGameId(gameId)
    }

    suspend fun createOrder(
        auth: Auth,
        gameId: Int,
        price: Int
    ): OrderStatus {
        val game: Game = gameRepository.getById(gameId) ?: throw ServiceException("Game does not exist")

        val customer: User = userRepository.get(auth.username) ?: throw ServiceException("Unknown error")

        val haveBoughtStatus: OrderStatus = orderRepository.checkOrder(customer.username, gameId)
        if (haveBoughtStatus == OrderStatus.SUCCESS) {
            throw ServiceException("User '${customer.username}' have already bought '${game.name}'")
        }

        val author: User = userRepository.get(game.author) ?: throw ServiceException("Unknown error")

        val purchaseTime: Instant = Instant.now()

        val orderId = orderRepository.createOrder(customer.username, game.id, purchaseTime, price)

        orderRepositoryProvider.transaction { transaction, orderRepo ->
            val userRepo = userRepositoryProvider.provide(transaction)

            userRepo.updateUser(
                customer.copy(balance = customer.balance - price)
            )
            userRepo.updateUser(
                author.copy(balance = author.balance + price)
            )
            orderRepo.updateOrder(orderId, OrderStatus.REFUNDABLE)

            val curUser: User = userRepo.get(customer.username)!!

            if (curUser.balance < 0) {
                transaction.rollback()
            } else {
                transaction.commit()
            }
        }

        val order = orderRepository.getOrder(orderId)!!
        if (order.status == "FAIL") return OrderStatus.FAIL
        if (order.status == "REFUNDABLE") return OrderStatus.REFUNDABLE
        if (order.status == "REFUNDED") return OrderStatus.REFUNDED
        return OrderStatus.SUCCESS

    }
}
