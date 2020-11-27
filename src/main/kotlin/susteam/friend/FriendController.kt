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
        router.get("/friend/apply/:username").coroutineHandler(::handleAddFriend)
        router.get("/friend/apply").coroutineHandler(::handleGetFriendsApplication)
        router.get("/friend/reply").coroutineHandler(::handleGetFriendsReply)
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

    suspend fun handleAddFriend(context: RoutingContext) {
        val request = context.request()
        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")
        val username = request.getParam("username") ?: throw ServiceException("Username is empty")
        service.addFriend(auth, username)
        context.success()
    }

    suspend fun handleGetFriendsApplication(context: RoutingContext) {
        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")
        val applicationList = service.getApplicationList(auth)
        context.success(
            jsonObjectOf(
                "application" to JsonArray(applicationList.map { it.toJson() })
            )
        )
    }

    suspend fun handleGetFriendsReply(context: RoutingContext) {
        val auth: Auth = context.user() ?: throw ServiceException("Permission denied, please login")
        val replyList = service.getReplyList(auth)
        context.success(
            jsonObjectOf(
                "reply" to JsonArray(replyList.map { it.toJson() })
            )
        )
    }


}

