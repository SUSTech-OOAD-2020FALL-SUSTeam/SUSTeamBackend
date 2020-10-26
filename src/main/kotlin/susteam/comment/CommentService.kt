package susteam.comment

import com.google.inject.Inject
import susteam.ServiceException
import susteam.game.GameRepository
import susteam.user.Auth
import susteam.user.UserRepository
import susteam.user.username
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
        auth: Auth,
        gameId: Int,
        content: String,
        score: Int
    ) {
        checkComment(content, score, auth, gameId)
        val commentTime: Instant = Instant.now()
        commentRepository.create(auth.username, gameId, commentTime, content, score)
    }

    suspend fun modifyComment(
        auth: Auth,
        gameId: Int,
        newContent: String,
        newScore: Int
    ) {
        checkComment(newContent, newScore, auth, gameId)
        if (!commentRepository.getExists(auth.username, gameId)) {
            throw ServiceException("No such comment for given user and game")
        }
        val commentTime: Instant = Instant.now()
        commentRepository.modify(auth.username, gameId, commentTime, newContent, newScore)
    }

    private suspend fun checkComment(
        content: String,
        score: Int,
        auth: Auth,
        gameId: Int
    ) {
        if (content.isBlank()) {
            throw ServiceException("Content is blank")
        } else if (content.length > 255) {
            throw ServiceException("Content is too long")
        }
        if (score !in 0..5) {
            throw ServiceException("Invalid score")
        }
        userRepository.get(auth.username) ?: throw ServiceException("User does not exist")
        gameRepository.getById(gameId) ?: throw ServiceException("Game does not exist")
    }

}