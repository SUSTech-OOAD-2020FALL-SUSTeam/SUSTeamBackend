package susteam.game.impl

import susteam.ServiceException
import susteam.game.*
import susteam.repository.impl.RepositoryMock
import susteam.storage.toStorageFile
import susteam.storage.toStorageImage
import java.time.Instant

class GameRepositoryMock(
    override val dataset: Map<String, MutableList<*>> = mapOf(
        "game" to mutableListOf<GameRepositoryMockItem>(),
        "gameVersion" to mutableListOf<GameVersionRepositoryMockItem>(),
        "gameImage" to mutableListOf<GameImageRepositoryMockItem>(),
        "gameTag" to mutableListOf<GameTagRepositoryMockItem>()
    )
) : GameRepository, RepositoryMock {
    data class GameRepositoryMockItem(
        val id: Int,
        val name: String,
        var price: Int,
        val publishDate: Instant,
        var author: String,
        var introduction: String?,
        var description: String?
    )

    data class GameVersionRepositoryMockItem(
        val gameId: Int,
        val name: String,
        val url: String
    )

    data class GameImageRepositoryMockItem(
        var gameId: Int,
        var url: String,
        var type: String
    )

    data class GameTagRepositoryMockItem(
        val gameId: Int,
        val tag: String
    )

    private val mockGame = GameRepositoryMockItem(
        1, "mock", 100, Instant.now(), "author", "MOCK", "MOCK!"
    )

    @Suppress("UNCHECKED_CAST")
    private var games: MutableList<GameRepositoryMockItem> =
        dataset["game"] as MutableList<GameRepositoryMockItem>

    @Suppress("UNCHECKED_CAST")
    private var gameVersions: MutableList<GameVersionRepositoryMockItem> =
        dataset["gameVersion"] as MutableList<GameVersionRepositoryMockItem>

    @Suppress("UNCHECKED_CAST")
    private var gameImages: MutableList<GameImageRepositoryMockItem> =
        dataset["gameImage"] as MutableList<GameImageRepositoryMockItem>

    @Suppress("UNCHECKED_CAST")
    private var gameTags: MutableList<GameTagRepositoryMockItem> =
        dataset["gameTag"] as MutableList<GameTagRepositoryMockItem>


    override fun init() {
        games.add(mockGame)
        gameTags.add(GameTagRepositoryMockItem(1, "heihei"))
        gameTags.add(GameTagRepositoryMockItem(1, "huohuo"))
        gameVersions.add(GameVersionRepositoryMockItem(1, "v1.0", "url1"))
        gameImages.add(GameImageRepositoryMockItem(1, "urlimage", "F"))
    }

    override suspend fun createGame(
        name: String,
        price: Int,
        publishDate: Instant,
        author: String,
        introduction: String?,
        description: String?
    ): Int {
        if (games.find { it.name == name } != null)
            throw ServiceException("Cannot create game '$name'")
        games.add(
            GameRepositoryMockItem(
                games.size + 1, name, price, publishDate, author, introduction, description
            )
        )
        return games.size
    }

    override suspend fun updateGame(
        gameId: Int,
        game: Game
    ): Boolean {
        if (games.find { it.id == gameId } == null)
            return false
        val toUpdate = games.find { it.id == gameId }!!

        toUpdate.price = game.price
        toUpdate.introduction = game.introduction
        toUpdate.description = game.description

        return true
    }

    override suspend fun createVersion(gameId: Int, uploadTime: Instant, versionName: String, url: String) {
        TODO("Not yet implemented")
    }

//    override suspend fun createVersion(
//        gameId: Int,
//        versionName: String,
//        url: String
//    ) {
//        if (games.find { it.id == gameId } == null)
//            throw ServiceException("Cannot find game by id'$gameId'")
//        if (gameVersions.find { it.name == versionName } != null)
//            throw ServiceException("Cannot create version name '$versionName'")
//        if (gameVersions.find { it.url == url } != null)
//            throw ServiceException("url already exist")
//        gameVersions.add(
//            GameVersionRepositoryMockItem(
//                gameId, versionName, url
//            )
//        )
//    }

    override suspend fun getById(id: Int): Game? {
        return games.find { it.id == id }?.let {
            Game(it.id, it.name, it.price, it.publishDate, it.author, it.introduction, it.description)
        }
    }

    override suspend fun getVersion(gameId: Int, versionName: String): GameVersion? {
        TODO("Not yet implemented")
//        return gameVersions.find { it.name == versionName && it.gameId == gameId }?.let {
//            GameVersion(it.gameId, it.name, it.url.toStorageFile())
//        }
    }

    override suspend fun getNewestVersion(gameId: Int): GameVersion? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllGameProfileOrderByPublishDate(): List<GameProfile> {
        return emptyList()
    }

    override suspend fun getAllGameProfileOrderByName(): List<GameProfile> {
        return emptyList()
    }

    override suspend fun getAllGameProfile(vararg order: Pair<String, String>, limit: Int?): List<GameProfile> {
        return emptyList()
    }

    override suspend fun getRandomGameProfile(limit: Int): List<GameProfile> {
        return emptyList()
    }

    override suspend fun getGameProfileWithTags(tags: List<String>): List<GameProfile> {
        return emptyList()
    }

    override suspend fun getGameProfile(gameId: Int): GameProfile? {
        if (games.find { it.id == gameId } == null)
            return null
        val game = getById(gameId)!!
        val sizeF = gameImages.find { it.gameId == gameId && it.type == "F" }?.url
        val sizeC = gameImages.find { it.gameId == gameId && it.type == "C" }?.url
        return GameProfile(
            game.id,
            game.name,
            game.price,
            game.publishDate,
            game.author,
            game.introduction,
            sizeF?.toStorageImage(),
            sizeC?.toStorageImage()
        )
    }

    override suspend fun getGameDetail(gameId: Int): GameDetail? {
        if (games.find { it.id == gameId } == null)
            return null
        val game = games.find { it.id == gameId }?.let {
            Game(it.id, it.name, it.price, it.publishDate, it.author, it.introduction, it.description)
        }!!
        val images = gameImages.filter { it.gameId == gameId }
            .map { GameImage(it.gameId, it.url.toStorageImage(), it.type) }.toList()
        val tags = gameTags.filter { it.gameId == gameId }.map { it.tag }.toList()

        return GameDetail(game, images, tags)
    }

    override suspend fun createGameImage(gameId: Int, url: String, type: String): Boolean {
        if (games.find { it.id == gameId } == null)
            return false
        gameImages.add(
            GameImageRepositoryMockItem(
                gameId, url, type
            )
        )
        return true
    }

    override suspend fun updateGameImage(gameId: Int, url: String, type: String): Boolean {
        if (games.find { it.id == gameId } == null)
            return false
        gameImages.find { it.gameId == gameId && it.type == type }!!.url = url
        return true
    }

    override suspend fun getTag(gameId: Int): List<String> {
        return gameTags.filter { it.gameId == gameId }.map { it.tag }.toList()
    }

    override suspend fun getAllTag(): List<String> {
        return gameTags.map { it.tag }.toList()
    }

    override suspend fun addTag(gameId: Int, tag: String) {
        if (gameTags.find { it.gameId == gameId && it.tag == tag } != null)
            throw ServiceException("already exist")

        gameTags.add(
            GameTagRepositoryMockItem(
                gameId, tag
            )
        )
    }

    override suspend fun getGameProfiles(games: List<Int>): List<GameProfile> {
        return games.mapNotNull { getGameProfile(it) }
    }

    override suspend fun getDevelopedGameProfile(author: String): List<GameProfile> {
        TODO("Not implemented yet")
    }

}
