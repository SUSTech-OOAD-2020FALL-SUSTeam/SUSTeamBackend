package susteam.order

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.sql.SQLOperations
import io.vertx.ext.sql.TransactionIsolation
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.*
import susteam.game.Game
import susteam.user.User
import java.time.Instant
import java.time.format.DateTimeFormatter.ISO_INSTANT

class OrderRepository @Inject constructor(private val database: JDBCClient) {

    suspend fun getOrderbyGameId(gameId: Int): List<Order> {
        return database.queryWithParamsAwait(
            """
                SELECT
                    username,
                    game_id gameId,
                    status,
                    purchase_time purchaseTime,
                    price
                FROM `order`
                WHERE game_id = ?
                ORDER BY purchaseTime DESC;;
            """.trimIndent(),
            jsonArrayOf(gameId)
        ).rows.map { it.toOrder() }
    }

    suspend fun getOrderbyUsername(username: String): List<Order> {
        return database.queryWithParamsAwait(
            """
                SELECT
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

    suspend fun <T> transaction(block: suspend (SQLConnection) -> T): T {
        return database.getConnectionAwait().use { conn ->
            conn.setTransactionIsolationAwait(TransactionIsolation.SERIALIZABLE)
            block(conn)
        }
    }

    suspend fun createOrder(
        sql: SQLOperations,
        haveBoughtStatus: Status,
        user: User,
        author: User,
        game: Game,
        purchaseTime: Instant,
        price: Int
    ): Boolean {
        //TODO 还没有加中间商
        if( haveBoughtStatus == Status.NULL ) {
            sql.updateWithParamsAwait(
                """INSERT INTO `order` (username, game_id, status, purchase_time, price) VALUES (?, ?, ?, ?, ?);""",
                jsonArrayOf(user.username, game.id, "fail", ISO_INSTANT.format(purchaseTime), price)
            )
        }
        else {
            sql.updateWithParamsAwait(
                """UPDATE `order` SET status = 'fail' where game_id = ? and username = ?;""",
                jsonArrayOf(game.id, user.username)
            )
        }

        val userBalance: Int? = sql.querySingleWithParamsAwait(
                """SELECT balance from user where username = ?;""",
                jsonArrayOf(user.username)
            )?.getInteger(0)

        if( userBalance == null || userBalance < price ) return false

        sql.updateWithParamsAwait(
            """
                UPDATE user SET balance = balance-? where username = ?;
            """.trimIndent(),
            jsonArrayOf(price, user.username)
        )
        sql.updateWithParamsAwait(
            """
                UPDATE user SET balance = balance+? where username = ?;
            """.trimIndent(),
            jsonArrayOf(price, author.username)
        )
        sql.updateWithParamsAwait(
            """
                UPDATE `order` SET status = 'success' where game_id = ? and username = ?;
            """.trimIndent(),
            jsonArrayOf(game.id, user.username)
        )
        return true
    }

    suspend fun checkOrder(
        username: String,
        gameId: Int
    ): Status {
        val status: String? = database.querySingleWithParamsAwait(
            """SELECT status FROM `order` WHERE username = ? and game_id = ?;""",
            jsonArrayOf(username, gameId)
        )?.getString(0)
        print(status)
        if ( status == "success" || status == "refundable" ) return Status.SUCCESS
        if( status == "fail" || status == "refunded" ) return Status.FAIL
        return Status.NULL
    }
}
