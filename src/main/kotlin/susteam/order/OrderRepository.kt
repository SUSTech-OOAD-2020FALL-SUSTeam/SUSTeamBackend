package susteam.order

import java.time.Instant

interface OrderRepository {

    suspend fun getOrderByGameId(gameId: Int): List<Order>

    suspend fun getOrderByUsername(username: String): List<Order>

    suspend fun createOrder(
        username: String,
        gameId: Int,
        purchaseTime: Instant,
        price: Int
    ): Int

    suspend fun checkOrder(
        username: String,
        gameId: Int
    ): OrderStatus

    suspend fun updateOrder(
        orderId: Int,
        status: OrderStatus
    )

    suspend fun getOrder(
        orderId: Int
    ): Order?

    suspend fun getBoughtGameByUsername(username: String): List<Int>
}
