package susteam.friend

import com.google.inject.Inject
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.notification.MessageNotifier
import susteam.status.UserStatus
import susteam.user.Auth
import susteam.user.username

class FriendService @Inject constructor(
    private val repository: FriendRepository,
    private val status: UserStatus,
    private val notifier: MessageNotifier
) {
    suspend fun getFriends(auth: Auth): List<Friend> {
        return repository.getFriendsUsername(auth.username).map { friendName ->
            val friendStatus = status.getStatus(friendName)
            Friend(friendName, friendStatus?.online ?: false, friendStatus?.lastSeen)
        }
    }

    suspend fun addFriend(auth: Auth, username: String) {
        repository.addFriend(auth.username, username)
        notifier.sendTo(username, jsonObjectOf("message" to "A friend application from ${auth.username}"))
    }

    suspend fun getApplicationList(auth: Auth): List<FriendApplication> {
        return repository.getApplicationList(auth.username)
    }

    suspend fun getReplyList(auth: Auth): List<FriendReply> {
        return repository.getReplyList(auth.username)
    }

    suspend fun replyTo(auth: Auth, target: String, agree: Boolean): Boolean {
        val newStatus: String = if (agree) "accept" else "reject"
        val isSuccess: Boolean = repository.replyTo(auth.username, target, newStatus)
        if (isSuccess) {
            notifier.sendTo(target, jsonObjectOf("message" to "${auth.username} has ${newStatus}ed your application"))
        }
        return isSuccess
    }

}