package susteam.friend

import com.google.inject.Inject
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.ServiceException
import susteam.game.GameRepository
import susteam.notification.MessageNotifier
import susteam.status.UserStatus
import susteam.user.Auth
import susteam.user.username

class FriendService @Inject constructor(
    private val repository: FriendRepository,
    private val gameRepository: GameRepository,
    private val status: UserStatus,
    private val notifier: MessageNotifier
) {
    suspend fun getFriends(auth: Auth): List<Friend> {
        return repository.getFriendsUsername(auth.username).map { friendName ->
            val friendStatus = status.getStatus(friendName)
            Friend(friendName, friendStatus?.online ?: false, friendStatus?.lastSeen)
        }
    }

    suspend fun invitedFriend(auth: Auth, username: String, gameKey: String) {
        val game = gameRepository.getGameByGameKey(gameKey) ?: throw ServiceException("Game does not exist")
        if (username in getFriends(auth).map{ it.username })
            notifier.sendTo(username, jsonObjectOf("message" to "${auth.username} invite you to play ${game.name}"))
        else throw ServiceException("${username} is not your friend")
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
