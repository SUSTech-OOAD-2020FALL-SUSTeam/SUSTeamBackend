package susteam.friend

interface FriendRepository {

    suspend fun getFriendsUsername(username: String): List<String>

    suspend fun getFriendsUsernameHaveGame(username: String, gameId: Int): List<String>

    suspend fun addFriend(username: String, target: String)

    suspend fun getApplicationList(username: String): List<FriendApplication>

    suspend fun getReplyList(username: String): List<FriendReply>

    suspend fun replyTo(username: String, applicant: String, newStatus: String): Boolean
}
