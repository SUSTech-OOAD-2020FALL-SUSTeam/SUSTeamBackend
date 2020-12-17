package susteam.discount

import com.google.inject.Inject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class DiscountController @Inject constructor(
    private val service: DiscountService
) : CoroutineController() {
    override fun route(router: Router) {
        router.get("/discount/:gameId").coroutineHandler(::handleGetDiscount)
        router.post("/discount/:gameId").coroutineHandler(::handleAddDiscount)
    }

    suspend fun handleGetDiscount(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val discount = service.getDiscount(gameId)

        context.success(
            jsonObjectOf(
                "discount" to if( discount != null ) discount.toJson() else null
            )
        )
    }

    suspend fun handleAddDiscount(context: RoutingContext) {
        val request = context.request()
        val gameId = request.getParam("gameId")?.toIntOrNull() ?: throw ServiceException("Game ID not found")

        val params = context.bodyAsJson
        val percentage = params.getDouble("percentage") ?: throw ServiceException("Percentage is empty")
        val startTime = params.getInstant("startTime") ?: throw ServiceException("Start time is empty")
        val endTime = params.getInstant("endTime") ?: throw ServiceException("End time is empty")

        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        service.addDiscount(auth, gameId, percentage, startTime, endTime)

        context.success()
    }
}
