package susteam.announcement

import java.time.Instant

interface AnnouncementRepository {

    suspend fun create(
        gameId: Int,
        announceTime: Instant,
        title: String,
        content: String
    )

    suspend fun getByGame(gameId: Int): List<Announcement>
}
