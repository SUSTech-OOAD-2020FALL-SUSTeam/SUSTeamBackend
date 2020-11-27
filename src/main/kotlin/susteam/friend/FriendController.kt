package susteam.friend

import com.google.inject.Inject
import io.vertx.core.json.JsonArray
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.user.Auth

class FriendController @Inject constructor(
    private val service: FriendService
) : CoroutineController() {
    override fun route(router: Router) {
        router.get("/friend").coroutineHandler(::handleGetFriends)
    }

    suspend fun handleGetFriends(context: RoutingContext) {
        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")

        val friendList: List<Friend> = service.getFriends(auth)

        context.success(
            jsonObjectOf(
                "friends" to JsonArray(friendList.map { it.toJson() })
            )
        )
    }


}

