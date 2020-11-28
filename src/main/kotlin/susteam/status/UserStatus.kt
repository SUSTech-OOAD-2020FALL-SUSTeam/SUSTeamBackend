package susteam.status

import susteam.user.Auth
import susteam.user.username
import java.time.Instant

data class Status(
    var online: Boolean,
    var lastSeen: Instant
)

object UserStatus {

    private val StatusMap = HashMap<String, Status>()

    fun getStatus(username: String): Status? = StatusMap[username]

    fun updateStatusWithApiCalled(auth: Auth) {
        val username = auth.username
        if (StatusMap.containsKey(username)) {
            StatusMap[username]?.apply {
                lastSeen = Instant.now()
            }
        } else {
            StatusMap[username] = Status(true, Instant.now())
        }
        print("update $username status")
    }
}