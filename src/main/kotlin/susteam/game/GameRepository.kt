package susteam.game

import java.time.Instant

interface GameRepository {

    suspend fun createGame(
        name: String,
        price: Int,
        publishDate: Instant,
        author: String,
        introduction: String?,
        description: String?
    ): Int

    suspend fun addKeyMap(
        gameId: Int,
        gameKey: String
    )

    suspend fun getGameByGameKey(
        gameKey: String
    ): Game?

    suspend fun getGameKey(
        gameId: Int
    ): String?

    suspend fun updateGame(
        gameId: Int,
        game: Game
    ): Boolean

    suspend fun createVersion(
        gameId: Int,
        branch: String,
        uploadTime: Instant,
        versionName: String,
        url: String,
        updateUrl: String?,
    )

    suspend fun getById(id: Int): Game?

    suspend fun getVersion(gameId: Int, versionName: String): GameVersion?

    suspend fun getNewestVersion(gameId: Int, branchName: String): GameVersion?

    suspend fun getVersionOfBranch(gameId: Int, branchName: String): List<GameVersion>

    suspend fun getAllBranch(gameId: Int): List<String>

    suspend fun getAllGameProfileOrderByPublishDate(): List<GameProfile>

    suspend fun getAllGameProfileOrderByName(): List<GameProfile>

    suspend fun getAllGameProfile(vararg order: Pair<String, String>, limit: Int? = null): List<GameProfile>

    suspend fun getRandomGameProfile(limit: Int): List<GameProfile> {
        return getAllGameProfile("rand()" to "", limit = limit)
    }

    suspend fun getGameProfile(gameId: Int): GameProfile?

    suspend fun getGameDetail(gameId: Int): GameDetail?

    suspend fun createGameImage(gameId: Int, url: String, type: String): Boolean

    suspend fun updateGameImage(gameId: Int, url: String, type: String): Boolean

    suspend fun getTag(gameId: Int): List<String>

    suspend fun getAllTag(): List<String>

    suspend fun getGameProfileWithTags(tags: List<String>): List<GameProfile>

    suspend fun addTag(gameId: Int, tag: String)

    suspend fun getGameProfiles(games: List<Int>): List<GameProfile>

    suspend fun getDevelopedGameProfile(author: String): List<GameProfile>
}
