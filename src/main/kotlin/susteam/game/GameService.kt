package susteam.game

import com.google.inject.Inject
import susteam.ServiceException
import susteam.storage.StorageImage
import susteam.user.Auth
import susteam.user.isAdmin
import susteam.user.isDeveloper
import susteam.user.username
import java.time.Instant

class GameService @Inject constructor(
    private val repository: GameRepository
) {

    suspend fun getGame(gameId: Int): Game {
        return repository.getById(gameId) ?: throw ServiceException("Game does not exist")
    }

    suspend fun getGameProfile(gameId: Int): GameProfile {
        return repository.getGameProfile(gameId) ?: throw ServiceException("Game does not exist")
    }

    suspend fun getGameDetail(gameId: Int): GameDetail {
        return repository.getGameDetail(gameId) ?: throw ServiceException("Game does not exist")
    }

    suspend fun getGameVersion(gameId: Int, versionName: String): GameVersion {
        return repository.getVersion(gameId, versionName) ?: throw ServiceException("Version does not exist")
    }

    suspend fun publishGame(
        auth: Auth,
        gameName: String,
        price: Int,
        introduction: String?,
        description: String?
    ) {
        if (gameName.isBlank()) {
            throw ServiceException("Game name is blank")
        }
        if (price < 0) {
            throw ServiceException("Price is less than zero")
        }

        if (!auth.isAdmin() && !auth.isDeveloper()) {
            throw ServiceException("Permission denied")
        }

        val publishDate: Instant = Instant.now()
        repository.createGame(gameName, price, publishDate, auth.username, introduction, description)
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

        if (!auth.isAdmin() && !auth.isDeveloper()) {
            throw ServiceException("Permission denied")
        }

        repository.createVersion(gameId, versionName, url)
    }

    suspend fun updateDescription(
        auth: Auth,
        gameId: Int,
        description: String?
    ) {
        val game = repository.getById(gameId) ?: throw ServiceException("Game does not exist")

        val havePermission = when {
            auth.isAdmin() -> true
            auth.isDeveloper() -> {
                game.author == auth.username
            }
            else -> false
        }
        if (!havePermission) {
            throw ServiceException("Permission denied")
        }

        repository.updateDescription(gameId, description)
    }

    suspend fun getAllGameProfileOrderByPublishDate(): List<GameProfile> {
        return repository.getAllGameProfileOrderByPublishDate()
    }

    suspend fun getAllGameProfileOrderByName(): List<GameProfile> {
        return repository.getAllGameProfileOrderByName()
    }

    suspend fun getAllGameProfile(): List<GameProfile> {
        return repository.getAllGameProfile()
    }

    suspend fun getRandomGameProfile(numberOfGames: Int): List<GameProfile> {
        if (numberOfGames <= 0) throw ServiceException("Number of Games must be greater than zero")
        return repository.getRandomGameProfile(numberOfGames)
    }

    suspend fun uploadGameImage(gameId: Int, image: StorageImage, type: String) {
        when (type) {
            "N" -> repository.createGameImage(gameId, image.id, type)
            "F" -> {
                val profile = getGameProfile(gameId)
                if (profile.imageFullSize != null){
                    repository.updateGameImage(gameId, image.id, type)
                } else {
                    repository.createGameImage(gameId, image.id, type)
                }
            }
            "C" -> {
                val profile = getGameProfile(gameId)
                if (profile.imageCardSize != null){
                    repository.updateGameImage(gameId, image.id, type)
                } else {
                    repository.createGameImage(gameId, image.id, type)
                }
            }
            else -> throw ServiceException("Cannot recognize game image type '${type}'")
        }
    }

}