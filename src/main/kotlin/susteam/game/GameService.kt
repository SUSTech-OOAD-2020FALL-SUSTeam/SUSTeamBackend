package susteam.game

import com.google.inject.Inject
import susteam.ServiceException
import java.time.Instant

class GameService @Inject constructor(
        private val repository: GameRepository
) {

    suspend fun getGame(gameName: String): Game {
        return repository.get(gameName) ?: throw ServiceException("Game does not exist")
    }

    suspend fun getGameVersion(gameName: String, versionName: String): GameVersion {
        return repository.getVersion(gameName, versionName) ?: throw ServiceException("Version do not exist")
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

        repository.create(gameName, price, publishDate, author, description)
    }

    suspend fun publishGameVersion(
            gameName: String,
            versionName: String,
            url: String
    ) {
        if (versionName.isBlank()) {
            throw ServiceException("Game version name is blank")
        }
        if (url.isBlank()) {
            throw ServiceException("Url is blank")
        }

        repository.createVersion(gameName, versionName, url)
    }

    suspend fun updateDescription(
            gameName: String,
            description: String?
    ) {
        repository.updateDescription(gameName, description)
    }

}