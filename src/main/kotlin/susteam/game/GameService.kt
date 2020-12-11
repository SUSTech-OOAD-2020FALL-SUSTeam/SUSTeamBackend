package susteam.game

import com.google.inject.Inject
import io.vertx.ext.auth.HashingStrategy
import susteam.ServiceException
import susteam.order.OrderRepository
import susteam.order.OrderStatus
import susteam.storage.StorageFile
import susteam.storage.StorageImage
import susteam.user.Auth
import susteam.user.isAdmin
import susteam.user.isDeveloper
import susteam.user.username
import java.time.Instant
import kotlin.random.Random

class GameService @Inject constructor(
    private val repository: GameRepository,
    private val orderRepository: OrderRepository
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
    ): String {
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

        var gameKey = ""
        val charSet = "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm"
        for (i in 1..100) {
            gameKey = gameKey + charSet[Random.nextInt(62)]
        }

        val gameId = repository.createGame(gameName, price, publishDate, auth.username, introduction, description)
        repository.addKeyMap(gameId, gameKey)

        return gameKey
    }

    suspend fun getGameIdByGameKey(gameKey: String): Int {
        return repository.getGameId(gameKey) ?: throw ServiceException("Game id does not exist")
    }

    suspend fun getGameKey(gameId: Int, auth: Auth): String {
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

        return repository.getGameKey(gameId) ?: throw ServiceException("Game key does not exist")
    }

    suspend fun getNewestVersion(
        gameId: Int
    ): GameVersion {
        return repository.getNewestVersion(gameId) ?: throw ServiceException("Game or version does not exist")
    }

    suspend fun publishGameVersion(
        auth: Auth,
        gameId: Int,
        versionName: String,
        url: StorageFile
    ) {
        if (versionName.isBlank()) {
            throw ServiceException("Game version name is blank")
        }

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

        val uploadTime = Instant.now()
        repository.createVersion(gameId, uploadTime, versionName, url.id)
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

    suspend fun download(auth: Auth, gameId: Int, versionName: String): StorageFile {
        val downloadPermission = when {
            auth.isAdmin() -> true
            auth.username == getGame(gameId).author -> true
            orderRepository.checkOrder(auth.username, gameId) == OrderStatus.SUCCESS -> true
            else -> false
        }
        if (!downloadPermission) {
            throw ServiceException("Permission denied, user not own the game")
        }
        return getGameVersion(gameId, versionName).url
    }

    suspend fun uploadGameImage(gameId: Int, image: StorageImage, type: String) {
        when (type) {
            "N" -> repository.createGameImage(gameId, image.id, type)
            "F" -> {
                val profile = getGameProfile(gameId)
                if (profile.imageFullSize != null) {
                    repository.updateGameImage(gameId, image.id, type)
                } else {
                    repository.createGameImage(gameId, image.id, type)
                }
            }
            "C" -> {
                val profile = getGameProfile(gameId)
                if (profile.imageCardSize != null) {
                    repository.updateGameImage(gameId, image.id, type)
                } else {
                    repository.createGameImage(gameId, image.id, type)
                }
            }
            else -> throw ServiceException("Cannot recognize game image type '${type}'")
        }
    }

    suspend fun getTag(gameId: Int): List<String> {
        repository.getById(gameId) ?: throw ServiceException("Game does not exist")

        return repository.getTag(gameId)
    }

    suspend fun getAllTag(): List<String> {
        return repository.getAllTag()
    }

    suspend fun getGameProfileWithTags(tags: List<String>): List<GameProfile> {
        return repository.getGameProfileWithTags(tags)
    }

    suspend fun addTag(
        auth: Auth,
        gameId: Int,
        tag: String
    ) {
        if (tag.isBlank()) {
            throw ServiceException("Tag is blank")
        }

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

        repository.addTag(gameId, tag)
    }

    suspend fun getDevelopedGameProfile(author: Auth): List<GameProfile> {
        return repository.getDevelopedGameProfile(author.username)
    }
}
