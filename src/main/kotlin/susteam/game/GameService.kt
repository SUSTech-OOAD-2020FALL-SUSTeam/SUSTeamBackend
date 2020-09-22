package susteam.game

import com.google.inject.Inject
import susteam.ServiceException
import java.time.Instant

class GameService @Inject constructor(
        private val repository: GameRepository
) {

    suspend fun getGame(gameId: Int): Game {
        return repository.getById(gameId) ?: throw ServiceException("Game does not exist")
    }

    suspend fun getGameVersion(gameId: Int, versionName: String): GameVersion {
        return repository.getVersion(gameId, versionName) ?: throw ServiceException("Version do not exist")
    }

    suspend fun publishGame(
            gameName: String,
            price: Int,
            author: String,
            description: String?
    ) {
        if (gameName.isBlank()) {
            throw ServiceException("Game name is blank")
        }
        if (price < 0) {
            throw ServiceException("Price less than zero")
        }
        if (author.isBlank()) {
            throw ServiceException("Author is blank")
        }

        val publishDate: Instant = Instant.now()

        repository.createGame(gameName, price, publishDate, author, description)
    }

    suspend fun publishGameVersion(
            gameId: Int,
            versionName: String,
            url: String
    ) {
        if (versionName.isBlank()) {
            throw ServiceException("Game version name is blank")
        }
        if (url.isBlank()) {
            throw ServiceException("Url is blank")
        }

        repository.createVersion(gameId, versionName, url)
    }

    suspend fun updateDescription(
            gameId: Int,
            description: String?
    ) {
        repository.updateDescription(gameId, description)
    }

}