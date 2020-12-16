package susteam.record

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException

class RecordController @Inject constructor(
    private val service: RecordService
) : CoroutineController() {

    override fun route(router: Router) {
        router.post("/record/:gameKey").coroutineHandler(::handleAddRecord)
        router.get("/record/:gameKey/:rankNum").coroutineHandler(::handleGetRank)
        router.get("/record/max/:gameKey/:username").coroutineHandler(::handleGetUserMax)
    }

    suspend fun handleAddRecord(context: RoutingContext) {
        val request = context.request()

        val gameKey = request.getParam("gameKey") ?: throw ServiceException("Game Key not found")

        val params = context.bodyAsJson
        val username = params.getString("username") ?: throw ServiceException("Username not found")
        val score =
            params.getInteger("score") ?: throw ServiceException("Score not found")

        service.addRecord(username, gameKey, score);
        context.success()
    }


    suspend fun handleGetRank(context: RoutingContext) {
        val request = context.request()
        val gameKey = request.getParam("gameKey") ?: throw ServiceException("Game Key not found")
        val rankNum = request.getParam("rankNum")?.toIntOrNull() ?: throw ServiceException("Rank num not found")

        val records = service.getRank(gameKey, rankNum)

        context.success(
            jsonObjectOf(
                "records" to JsonArray(records.map { it })
            )
        )
    }


    suspend fun handleGetUserMax(context: RoutingContext) {
        val request = context.request()
        val gameKey = request.getParam("gameKey") ?: throw ServiceException("Game Key not found")
        val username = request.getParam("username")?: throw ServiceException("Username not found")

        val record = service.getUserMax(username, gameKey)

        context.success(
            jsonObjectOf(
                "record" to record.toJson()
            )
        )
    }

}

