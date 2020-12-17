package susteam.discount

import com.google.inject.Inject
import com.google.protobuf.ServiceException
import susteam.game.GameRepository
import susteam.user.Auth
import susteam.user.isAdmin
import susteam.user.isDeveloper
import susteam.user.username
import java.time.Instant

class DiscountService @Inject constructor(
    private val repository: DiscountRepository,
    private val gameRepository: GameRepository
) {
    suspend fun getDiscount(gameId: Int): Discount? {
        gameRepository.getById(gameId) ?: throw ServiceException("Game does not exist")
        return repository.getDiscount(gameId)
    }

    suspend fun addDiscount(
        auth: Auth,
        gameId: Int,
        percentage: Double,
        startTime: Instant,
        endTime: Instant
    ) {
        if( percentage < 0 || percentage > 1 ) throw ServiceException("Percentage must between 0 and 1")

        val game = gameRepository.getById(gameId) ?: throw ServiceException("Game does not exist")

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

        repository.addDiscount(gameId, percentage, startTime, endTime)
    }

    suspend fun getDiscounts(gameId: Int): List<Discount> {
        gameRepository.getById(gameId) ?: throw ServiceException("Game does not exist")
        return repository.getDiscounts(gameId)
    }
}
