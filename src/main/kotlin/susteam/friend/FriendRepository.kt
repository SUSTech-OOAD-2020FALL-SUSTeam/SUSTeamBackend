package susteam.friend

interface FriendRepository {

    suspend fun getFriendsUsername(username: String): List<String>

    suspend fun addFriend(from: String, to: String)

    suspend fun getApplicationList(username: String): List<FriendApplication>

    suspend fun getReplyList(username: String): List<FriendReply>
}