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
}