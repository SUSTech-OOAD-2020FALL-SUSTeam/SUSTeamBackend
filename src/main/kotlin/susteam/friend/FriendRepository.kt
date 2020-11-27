package susteam.friend

interface FriendRepository {

    suspend fun getFriendsUsername(username: String): List<String>

}