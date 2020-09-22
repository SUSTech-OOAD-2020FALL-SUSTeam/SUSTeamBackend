package susteam.game

import com.google.inject.Inject
import susteam.ServiceException
import susteam.user.Auth
import susteam.user.isAuthorized
import susteam.user.username
import java.time.Instant

class GameService @Inject constructor(
    private val repository: GameRepository
) {

    suspend fun getGame(gameId: Int): Game {
        return repository.getById(gameId) ?: throw ServiceException("Game does not exist")
    }

    suspend fun getGameVersion(gameId: Int, versionName: String): GameVersion {
        return repository.getVersion(gameId, versionName) ?: throw ServiceException("Version does not exist")
    }

    suspend fun publishGame(
        auth: Auth,
        gameName: String,
        price: Int,
        author: String,
        description: String?
    ) {
        if (gameName.isBlank()) {
            throw ServiceException("Game name is blank")
        }
        if (price < 0) {
            throw ServiceException("Price is less than zero")
        }
        if (author.isBlank()) {
            throw ServiceException("Author is blank")
        }

        if (!auth.isAuthorized("role:admin") && !auth.isAuthorized("role:developer")) {
            throw ServiceException("Permission deny")
        }

        val publishDate: Instant = Instant.now()

        repository.createGame(gameName, price, publishDate, author, description)
    }

    suspend fun publishGameVersion(
        auth: Auth,
        gameId: Int,
        versionName: String,
        url: String
    ) {
        if (versionName.isBlank()) {
            throw ServiceException("Game version name is blank")
        }
        if (url.isBlank()) {
            throw ServiceException("URL is blank")
        }

        if (!auth.isAuthorized("role:admin") && !auth.isAuthorized("role:developer")) {
            throw ServiceException("Permission deny")
        }

        repository.createVersion(gameId, versionName, url)
    }

    suspend fun updateDescription(
        auth: Auth,
        gameId: Int,
        description: String?
    ) {
        val game = repository.getById(gameId) ?: throw ServiceException("Game not exist")

        val havePermission = when {
            auth.isAuthorized("role:admin") -> true
            auth.isAuthorized("role:developer") -> {
                game.author == auth.username
            }
            else -> false
        }
        if (!havePermission) {
            throw ServiceException("Permission deny")
        }

        repository.updateDescription(gameId, description)
    }

}