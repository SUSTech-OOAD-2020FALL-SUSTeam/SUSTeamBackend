package susteam.user

import com.google.inject.Inject
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.HashingStrategy
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import susteam.ServiceException
import kotlin.random.Random

class UserService @Inject constructor(
    private val repository: UserRepository,
    private val provider: JWTAuth
) {

    private val hashingStrategy = HashingStrategy.load()

    suspend fun authUser(username: String, password: String): String {
        if (username.isBlank() || password.isBlank()) {
            throw ServiceException("Username or password is blank")
        }

        val hash = repository.getPasswordHash(username) ?: throw ServiceException("User does not exist")
        if (!hashingStrategy.verify(hash, password)) {
            throw ServiceException("Authentication failed")
        }

        val userRole = repository.getRole(username)!!
        val permissions = userRole.roles.map { "role:${it}" }

        return provider.generateToken(
            JsonObject().put("username", username),
            JWTOptions()
                .setAlgorithm("RS256")
                .setPermissions(permissions)
                .setExpiresInMinutes(60 * 24)
        )

    }

    suspend fun signUpUser(username: String, password: String, mail: String) {
        if (username.isBlank() || password.isBlank() || mail.isBlank()) {
            throw ServiceException("Username, password or mail is blank")
        }

        val salt = hashingStrategy.generateSalt()
        val hash = hashingStrategy.hash("sha512", emptyMap(), salt, password)

        repository.create(username, hash, mail)
    }

    suspend fun getUser(username: String): User {
        return repository.get(username) ?: throw ServiceException("User does not exist")
    }

    suspend fun getUserRole(user: Auth): UserRole {
        return repository.getRole(user.username) ?: throw ServiceException("User does not exist")
    }

    private fun HashingStrategy.generateSalt() = Random.nextBytes(32).toHexString()

    private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }

}