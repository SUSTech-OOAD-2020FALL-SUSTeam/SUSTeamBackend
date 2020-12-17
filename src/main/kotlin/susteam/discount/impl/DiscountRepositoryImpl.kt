package susteam.discount.impl

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.discount.Discount
import susteam.discount.DiscountRepository
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT

class DiscountRepositoryImpl @Inject constructor(private val database: JDBCClient) : DiscountRepository {
    override suspend fun getDiscount(gameId: Int): Discount? {
        return database.querySingleWithParamsAwait(
            """
                SELECT game_id    gameId,
                       percentage,
                       start_time startTime,
                       end_time   endTime
                FROM discount
                WHERE game_id = ?
                  AND start_time <= ?
                  AND end_time >= ?
                ORDER BY percentage
                LIMIT 1;
            """.trimIndent(),
            jsonArrayOf(gameId, ISO_INSTANT.format(Instant.now()), ISO_INSTANT.format(Instant.now()))
        )?.let {
            if (it.getDouble(1) != null)
                Discount(
                    it.getInteger(0),
                    it.getDouble(1),
                    it.getInstant(2),
                    it.getInstant(3)
                )
            else null
        }
    }

    override suspend fun addDiscount(
        gameId: Int,
        percentage: Double,
        startTime: Instant,
        endTime: Instant
    ) {
        database.updateWithParamsAwait(
            """
                INSERT INTO discount (game_id, percentage, start_time, end_time)
                VALUES (?, ?, ?, ?);
            """.trimIndent(),
            jsonArrayOf(gameId, percentage, ISO_INSTANT.format(startTime), ISO_INSTANT.format(endTime))
        )
    }
}
