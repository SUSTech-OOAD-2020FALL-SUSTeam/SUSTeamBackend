package susteam.friend.impl

import susteam.ServiceException
import susteam.friend.FriendApplication
import susteam.friend.FriendReply
import susteam.friend.FriendRepository
import susteam.repository.impl.RepositoryMock

class FriendRepositoryMock(
    override val dataset: Map<String, MutableList<*>> = mapOf(
        "friend" to mutableListOf<FriendRepositoryMockItem>()
    )
) : FriendRepository, RepositoryMock {
    @Suppress("UNCHECKED_CAST")
    private val data: MutableList<FriendRepositoryMockItem> =
        dataset["friend"] as MutableList<FriendRepositoryMockItem>

    data class FriendRepositoryMockItem(
        val user1: String,
        val user2: String,
        var status: String
    )

    fun FriendRepositoryMockItem.toFriendApplication(): FriendApplication =
        FriendApplication(user2, status)

    fun FriendRepositoryMockItem.toFriendReply(): FriendReply =
        FriendReply(user1, status)

    override fun init() {
        data.add(FriendRepositoryMockItem("admin", "poorCustomer", "accept"))
        data.add(FriendRepositoryMockItem("admin", "richCustomer", "accept"))
        data.add(FriendRepositoryMockItem("author", "admin", "pending"))
        data.add(FriendRepositoryMockItem("test002", "admin", "pending"))
    }

    override suspend fun getFriendsUsername(username: String): List<String> {
        return data.filter {
            return@filter (it.user1 == username || it.user2 == username) && it.status == "accept"
        }.map {
            if (it.user1 == username) {
                return@map it.user2
            } else {
                return@map it.user1
            }
        }
    }

    override suspend fun addFriend(username: String, target: String) {
        data.find {
            return@find (it.user1 == username && it.user2 == target) || (it.user1 == target && it.user2 == username)
        }?.let {
            throw ServiceException("Friend Application Duplicate")
        }

        data.add(FriendRepositoryMockItem(username, target, "pending"))
    }

    override suspend fun getApplicationList(username: String): List<FriendApplication> {
        return data.filter {
            return@filter it.user1 == username
        }.map {
            it.toFriendApplication()
        }
    }

    override suspend fun getReplyList(username: String): List<FriendReply> {
        return data.filter {
            return@filter it.user2 == username
        }.map {
            it.toFriendReply()
        }
    }

    override suspend fun replyTo(username: String, applicant: String, newStatus: String): Boolean {
        data.find {
            return@find it.user1 == applicant && it.user2 == username && it.status == "pending"
        }?.let {
            it.status = newStatus
            return true
        } ?: return false
    }
}