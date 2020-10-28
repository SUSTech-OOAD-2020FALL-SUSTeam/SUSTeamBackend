package susteam.order

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class OrderController @Inject constructor(private val service: OrderService) : CoroutineController() {

    override fun route(router: Router) {

        router.post("/order").coroutineHandler(::handleCreateOrder)
        router.get("/user/:username/orders").coroutineHandler(::handleGetOrderbyUsername)
        router.get("/game/:gameId/orders").coroutineHandler(::handleGetOrderbyGameId)
    }

    suspend fun handleGetOrderbyUsername(context: RoutingContext) {
        val request = context.request()
        val username = request.getParam("username") ?: throw ServiceException("Username not found")

        val orderList = service.getOrderByUsername(username)

        context.success(
            jsonObjectOf(
                "orders" to JsonArray(orderList.map { it.toJson() })
            )
        )
    }

    suspend fun handleGetOrderbyGameId(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val orderList = service.getOrderByGameId(gameId)

        context.success(
            jsonObjectOf(
                "orders" to JsonArray(orderList.map { it.toJson() })
            )
        )
    }

    suspend fun handleCreateOrder(context: RoutingContext) {
        val params = context.bodyAsJson
        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")
        val gameId = params.getInteger("gameId")?: throw ServiceException("Game ID not found")
        val price = params.getInteger("price")?: throw ServiceException("Price not found")

        val status: OrderStatus = service.createOrder(auth, gameId, price)

        context.success(
            jsonObjectOf(
                "status" to status.name
            )
        )
    }
}
