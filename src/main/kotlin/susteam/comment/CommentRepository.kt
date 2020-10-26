package susteam.comment

import java.time.Instant

interface CommentRepository {

    suspend fun create(
        username: String,
        gameId: Int,
        commentTime: Instant,
        content: String,
        score: Int
    )

    suspend fun getExists(username: String, gameId: Int): Boolean

    suspend fun modify(
        username: String,
        gameId: Int,
        commentTime: Instant,
        newContent: String,
        newScore: Int
    )

    suspend fun getByUser(username: String): List<Comment>

    suspend fun getByGame(gameId: Int): List<Comment>
}
