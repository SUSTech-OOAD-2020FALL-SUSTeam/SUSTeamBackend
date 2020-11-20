package susteam.save

import com.google.inject.Inject
import susteam.ServiceException
import susteam.order.OrderRepository
import susteam.order.OrderStatus
import susteam.storage.StorageFile
import susteam.user.Auth
import susteam.user.isAdmin
import susteam.user.username
import java.time.Instant

class GameSaveService @Inject constructor(
    private val repository: GameSaveRepository,
    private val orderRepository: OrderRepository
) {

    suspend fun getAllGameSaveName(auth: Auth, gameId: Int): List<GameSave> {
        checkBought(auth, gameId)
        return repository.getAllGameSaveName(auth.username, gameId)
    }

    suspend fun uploadGameSave(auth: Auth, gameId: Int, saveName: String, file: StorageFile) {
        val savedTime = Instant.now()
        repository.uploadGameSave(auth.username, gameId, saveName, savedTime, file.url)
    }

    suspend fun deleteGameSave(auth: Auth, gameId: Int, saveName: String) {
        checkBought(auth, gameId)
        repository.deleteGameSave(auth.username, gameId, saveName)
    }

    suspend fun getGameSave(auth: Auth, gameId: Int, saveName: String): GameSave {
        return repository.getGameSave(auth.username, gameId, saveName) ?: throw ServiceException("Save does not exist")
    }

    suspend fun download(auth: Auth, gameId: Int, saveName: String): StorageFile {
        checkBought(auth, gameId)
        return getGameSave(auth, gameId, saveName).url
    }

    suspend fun checkBought(auth: Auth, gameId: Int) {
        val downloadPermission = when {
            auth.isAdmin() -> true
            orderRepository.checkOrder(auth.username, gameId) == OrderStatus.SUCCESS -> true
            else -> false
        }
        if (!downloadPermission) {
            throw ServiceException("Permission denied, user not own the game")
        }
    }
}
