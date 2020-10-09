package susteam.announcement

import com.google.inject.Inject
import susteam.ServiceException
import susteam.game.GameRepository
import susteam.user.Auth
import susteam.user.isAdmin
import susteam.user.isDeveloper
import susteam.user.username
import java.time.Instant

class AnnouncementService @Inject constructor(
    private val announcementRepository: AnnouncementRepository,
    private val gameRepository: GameRepository
) {

    suspend fun getAnnouncementsByGame(gameId: Int): List<Announcement> {
        return announcementRepository.getByGame(gameId)
    }

    suspend fun createAnnouncement(
        auth: Auth,
        gameId: Int,
        title: String,
        content: String
    ) {
        if (content.isBlank()) {
            throw ServiceException("Content is blank")
        } else if (title.isBlank()) {
            throw ServiceException("Title is blank")
        } else if (content.length > 4095) {
            throw ServiceException("Content is too long")
        } else if (title.length > 255) {
            throw ServiceException("Title is too long")
        }
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

        val announceTime: Instant = Instant.now()

        announcementRepository.create(gameId, announceTime, title, content)
    }

}