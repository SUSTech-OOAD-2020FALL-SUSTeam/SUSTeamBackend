package susteam.friend

import com.google.inject.Inject
import susteam.user.Auth
import susteam.user.username
import java.time.Instant

class FriendService @Inject constructor(
    private val repository: FriendRepository
) {
    suspend fun getFriends(auth: Auth): List<Friend> {
        return repository.getFriendsUsername(auth.username).map {
            // TODO: Use Hashmap or redis to get a real status
            Friend(it, "online", Instant.now())
        }
    }

    suspend fun addFriend(auth: Auth, username: String) {
        repository.addFriend(auth.username, username)
    }

    suspend fun getApplicationList(auth: Auth): List<FriendApplication> {
        return repository.getApplicationList(auth.username)
    }

    suspend fun getReplyList(auth: Auth): List<FriendReply> {
        return repository.getReplyList(auth.username)
    }

    suspend fun replyTo(auth: Auth, to: String, agree: Boolean): Boolean {
        return repository.replyTo(auth.username, to, if (agree) "accept" else "reject")
    }

}