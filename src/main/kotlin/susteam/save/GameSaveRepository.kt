package susteam.save

import java.time.Instant

interface GameSaveRepository {

    suspend fun getAllGameSaveName(
        username: String,
        gameId: Int
    ): List<GameSave>

    suspend fun uploadGameSave(
        username: String,
        gameId: Int,
        saveName: String,
        savedTime: Instant,
        url: String
    )

    suspend fun deleteGameSave(
        username: String,
        gameId: Int,
        saveName: String
    )

    suspend fun getGameSave(
        username: String,
        gameId: Int,
        saveName: String
    ): GameSave?
}
