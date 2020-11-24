package susteam.order.impl

import susteam.order.Order
import susteam.order.OrderRepository
import susteam.order.OrderStatus
import susteam.repository.impl.RepositoryMock
import java.time.Instant

class OrderRepositoryMock(
    override val dataset: Map<String, MutableList<*>> = mapOf(
        "order" to mutableListOf<OrderRepositoryMockItem>()
    )
) : OrderRepository, RepositoryMock {
    data class OrderRepositoryMockItem(
        val id: Int,
        val username: String,
        val gameId: Int,
        var status: String,// refundable  refunded  fail  success
        val purchaseTime: Instant,
        val price: Int
    )

    data class UserRepositoryMockItem(
        val username: String,
        var balance: Int
    )

    data class GameRepositoryMockItem(
        val gameId: Int,
        val author: String
    )

    @Suppress("UNCHECKED_CAST")
    private var orders: MutableList<OrderRepositoryMockItem> =
        dataset["order"] as MutableList<OrderRepositoryMockItem>

    override fun init() {
        orders.add(
            OrderRepositoryMockItem(
                1, "poorCustomer", 1, "REFUND", Instant.now(), 10
            )
        )
        orders.add(
            OrderRepositoryMockItem(
                2, "poorCustomer", 1, "FAIL", Instant.now(), 10
            )
        )
        orders.add(
            OrderRepositoryMockItem(
                3, "richCustomer", 1, "SUCCESS", Instant.now(), 10
            )
        )
        orders.add(
            OrderRepositoryMockItem(
                4, "poorCustomer", 2, "REFUNDABLE", Instant.now(), 10
            )
        )
    }

    override suspend fun getOrderByGameId(gameId: Int): List<Order> {
        return orders.filter { it.gameId == gameId }
            .map { Order(it.id, it.username, it.gameId, it.status, it.purchaseTime, it.price) }
    }

    override suspend fun getOrderByUsername(username: String): List<Order> {
        return orders.filter { it.username == username }
            .map { Order(it.id, it.username, it.gameId, it.status, it.purchaseTime, it.price) }
    }

    override suspend fun createOrder(
        username: String,
        gameId: Int,
        purchaseTime: Instant,
        price: Int
    ): Int {
        orders.add(
            OrderRepositoryMockItem(
                orders.size + 1, username, gameId, "FAIL", purchaseTime, price
            )
        )
        return orders.size
    }

    override suspend fun checkOrder(
        username: String,
        gameId: Int
    ): OrderStatus {
        val count = orders.filter {
            it.username == username && it.gameId == gameId &&
                    (it.status == "SUCCESS" || it.status == "REFUNDABLE")
        }.size
        if (count == 0) return OrderStatus.FAIL
        return OrderStatus.SUCCESS
    }

    override suspend fun updateOrder(
        orderId: Int,
        status: OrderStatus
    ) {
        orders.find { it.id == orderId }?.let { it.status == status.toString() }
    }

    override suspend fun getOrder(
        orderId: Int
    ): Order? {
        return orders.find { it.id == orderId }?.let {
            Order(it.id, it.username, it.gameId, it.status, it.purchaseTime, it.price)
        }
    }

    override suspend fun getBoughtGameByUsername(username: String): List<Int> {
        return orders.filter { it.status == "SUCCESS" || it.status == "REFUNDABLE" }.map { it.gameId }
    }
}
