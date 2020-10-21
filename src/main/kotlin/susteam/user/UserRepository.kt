package susteam.user

interface UserRepository {

    suspend fun create(username: String, password: String, mail: String)
    suspend fun get(username: String): User?
    suspend fun getPasswordHash(username: String): String?
    suspend fun getRole(username: String): UserRole?

}