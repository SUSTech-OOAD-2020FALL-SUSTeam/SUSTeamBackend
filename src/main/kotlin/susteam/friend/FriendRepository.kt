package susteam.friend

interface FriendRepository {

    suspend fun getFriendsUsername(username: String): List<String>

    suspend fun addFriend(from: String, to: String)

}