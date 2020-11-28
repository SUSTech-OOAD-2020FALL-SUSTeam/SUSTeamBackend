package susteam.order

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant

data class Order(
    val id: Int,
    val username: String,
    val gameId: Int,
    val status: String,// refundable  refunded  fail  success
    val purchaseTime: Instant,
    val price: Int
)

enum class OrderStatus {
    REFUNDABLE, REFUNDED, FAIL, SUCCESS
}

fun Order.toJson(): JsonObject = jsonObjectOf(
    "orderId" to id,
    "username" to username,
    "gameId" to gameId,
    "status" to status,
    "purchaseTime" to purchaseTime,
    "price" to price,
)

fun JsonObject.toOrder(): Order = Order(
    getInteger("orderId"),
    getString("username"),
    getInteger("gameId"),
    getString("status"),
    getInstant("purchaseTime"),
    getInteger("price")
)
