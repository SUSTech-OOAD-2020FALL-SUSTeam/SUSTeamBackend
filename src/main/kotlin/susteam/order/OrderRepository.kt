package susteam.order

import java.time.Instant

interface OrderRepository {

    suspend fun getOrderbyGameId(gameId: Int): List<Order>

    suspend fun getOrderbyUsername(username: String): List<Order>

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
}
