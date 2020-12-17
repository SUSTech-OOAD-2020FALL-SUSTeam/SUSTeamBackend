package susteam.discount

import java.time.Instant

interface DiscountRepository {
    suspend fun getDiscount(gameId: Int): Discount?

    suspend fun addDiscount(
        gameId: Int,
        percentage: Double,
        startTime: Instant,
        endTime: Instant
    )
}
