package susteam.announcement.impl

import susteam.ServiceException
import susteam.announcement.Announcement
import susteam.announcement.AnnouncementRepository
import susteam.repository.impl.RepositoryMock

import java.time.Instant

class AnnouncementRepositoryMock(
    override val dataset: Map<String, MutableList<*>> = mapOf(
        "announcement" to mutableListOf<AnnouncementRepositoryMockItem>()
    )
) : AnnouncementRepository, RepositoryMock {
    @Suppress("UNCHECKED_CAST")
    private val announcement: MutableList<AnnouncementRepositoryMockItem> =
        dataset["announcement"] as MutableList<AnnouncementRepositoryMockItem>

    data class AnnouncementRepositoryMockItem(
        val game_id: Int,
        var announcement_time: Instant,
        var title: String,
        var content: String
    )

    private fun AnnouncementRepositoryMockItem.toAnnouncement(): Announcement = Announcement(
        game_id, announcement_time, title, content
    )

    override fun init() {}

    override suspend fun create(gameId: Int, announceTime: Instant, title: String, content: String) {
        if (announcement.find { it.game_id == gameId && it.title == title } != null)
            throw ServiceException("Cannot create announcement")
        announcement.add(
            AnnouncementRepositoryMockItem(gameId, announceTime, title, content)
        )
    }

    override suspend fun getByGame(gameId: Int): List<Announcement> =
        announcement.filter { it.game_id == gameId }.map {
            it.toAnnouncement()
        }
}
