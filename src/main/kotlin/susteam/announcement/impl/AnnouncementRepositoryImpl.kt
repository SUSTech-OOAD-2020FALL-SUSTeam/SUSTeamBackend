package susteam.announcement.impl

import susteam.announcement.Announcement
import susteam.announcement.toAnnouncement

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.announcement.AnnouncementRepository
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant

class AnnouncementRepositoryImpl @Inject constructor(private val database: JDBCClient) : AnnouncementRepository {

    override suspend fun create(
        gameId: Int,
        announceTime: Instant,
        title: String,
        content: String
    ) {
        try {
            database.updateWithParamsAwait(
                """
                    INSERT INTO announcement (game_id, announce_time, title, content)
                    VALUES (?, ?, ?, ?);
                """.trimIndent(),
                jsonArrayOf(gameId, announceTime.toString(), title, content)
            )
        } catch (e: SQLIntegrityConstraintViolationException) {
            throw ServiceException("Announce failed", e)
        }
    }

    override suspend fun getByGame(gameId: Int): List<Announcement> {
        return database.queryWithParamsAwait(
            """
                SELECT
                    game_id gameId,
                    announce_time announceTime,
                    title,
                    content
                FROM announcement
                WHERE game_id = ?
                ORDER BY announce_time DESC;
            """.trimIndent(),
            jsonArrayOf(gameId)
        ).rows.map { it.toAnnouncement() }
    }
}
