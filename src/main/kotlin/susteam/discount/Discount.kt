package susteam.discount

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf
import java.time.Instant

data class Discount(
    val gameId: Int,
    val percentage: Double?,
    val startTime: Instant?,
    val endTime: Instant?
)

fun Discount.toJson(): JsonObject = jsonObjectOf(
    "gameId" to gameId,
    "percentage" to percentage,
    "startTime" to startTime,
    "endTime" to endTime
)

fun JsonObject.toDiscount(): Discount = Discount(
    getInteger("gameId"),
    getDouble("percentage"),
    getInstant("startTime"),
    getInstant("endTime")
)
