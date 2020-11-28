package susteam.order.impl

import com.google.inject.Inject
import io.vertx.ext.sql.SQLOperations
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.order.Order
import susteam.order.OrderRepository
import susteam.order.OrderStatus
import susteam.order.toOrder
import java.time.Instant
import java.time.format.DateTimeFormatter

class OrderRepositoryImpl @Inject constructor(private val database: SQLOperations) : OrderRepository {

    override suspend fun getOrderByGameId(gameId: Int): List<Order> {
        return database.queryWithParamsAwait(
            """
                SELECT
                    order_id orderId,
                    username,
                    game_id gameId,
                    status,
                    purchase_time purchaseTime,
                    price
                FROM `order`
                WHERE game_id = ?
                ORDER BY purchaseTime DESC;
            """.trimIndent(),
            jsonArrayOf(gameId)
        ).rows.map { it.toOrder() }
    }

    override suspend fun getOrderByUsername(username: String): List<Order> {
        return database.queryWithParamsAwait(
            """
                SELECT
                    order_id orderId,
                    username,
                    game_id gameId,
                    status,
                    purchase_time purchaseTime,
                    price
                FROM `order`
                WHERE username = ?
                ORDER BY purchaseTime DESC;
            """.trimIndent(),
            jsonArrayOf(username)
        ).rows.map { it.toOrder() }
    }

    override suspend fun createOrder(
        username: String,
        gameId: Int,
        purchaseTime: Instant,
        price: Int
    ): Int {
        return database.updateWithParamsAwait(
            """INSERT INTO `order` (username, game_id, status, purchase_time, price) VALUES (?, ?, ?, ?, ?);""",
            jsonArrayOf(
                username,
                gameId,
                OrderStatus.FAIL.toString(),
                DateTimeFormatter.ISO_INSTANT.format(purchaseTime),
                price
            )
        ).keys.getInteger(0)
    }

    override suspend fun updateOrder(orderId: Int, status: OrderStatus) {
        database.updateWithParamsAwait(
            """UPDATE `order` SET status = ? where order_id = ?;""",
            jsonArrayOf(status.toString(), orderId)
        )
    }


    override suspend fun checkOrder(
        username: String,
        gameId: Int
    ): OrderStatus {
        val status: Int? = database.querySingleWithParamsAwait(
            """
                SELECT count(*)
                FROM `order`
                WHERE username = ?
                  and game_id = ?
                  and (status = 'SUCCESS' or status = 'REFUNDABLE');
            """.trimIndent(),
            jsonArrayOf(username, gameId)
        )?.getInteger(0)

        if (status == null || status == 0) return OrderStatus.FAIL
        return OrderStatus.SUCCESS
    }

    override suspend fun getOrder(
        orderId: Int
    ): Order? {
        return database.querySingleWithParamsAwait(
            """
                SELECT order_id, username, game_id, status, purchase_time, price
                FROM `order`
                WHERE order_id = ?;
            """.trimIndent(),
            jsonArrayOf(orderId)
        )?.let {
            Order(
                it.getInteger(0), it.getString(1), it.getInteger(2),
                it.getString(3), it.getInstant(4), it.getInteger(5)
            )
        }
    }

    override suspend fun getBoughtGameByUsername(username: String): List<Int> {
        return database.queryWithParamsAwait(
            """
            SELECT game_id
            FROM `order`
            WHERE username = ?
              and (status = 'SUCCESS' or status = 'REFUNDABLE');
            """.trimIndent(), jsonArrayOf(username)
        ).results.map { it.getInteger(0) }
    }

}
