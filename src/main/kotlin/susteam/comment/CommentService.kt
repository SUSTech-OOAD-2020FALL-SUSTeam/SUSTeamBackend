package susteam.comment

import com.google.inject.Inject
import susteam.ServiceException
import susteam.game.GameRepository
import susteam.user.UserRepository
import java.time.Instant

class CommentService @Inject constructor(
    private val commentRepository: CommentRepository,
    private val userRepository: UserRepository,
    private val gameRepository: GameRepository
) {

    suspend fun getCommentsByUser(username: String): List<Comment> {
        return commentRepository.getByUser(username)
    }

    suspend fun getCommentsByGame(gameId: Int): List<Comment> {
        return commentRepository.getByGame(gameId)
    }

    suspend fun createComment(
        username: String,
        gameId: Int,
        content: String,
        score: Int
    ) {
        if (content.isBlank()) {
            throw ServiceException("Content is blank")
        }
        else if (content.length > 255) {
            throw ServiceException("Content is too long")
        }
        if (score !in 0..5) {
            throw ServiceException("Invalid score")
        }
        userRepository.get(username) ?: throw ServiceException("User does not exist")
        gameRepository.getById(gameId) ?: throw ServiceException("Game does not exist")

        val commentTime: Instant = Instant.now()

        commentRepository.create(username, gameId, commentTime, content, score)
    }

}