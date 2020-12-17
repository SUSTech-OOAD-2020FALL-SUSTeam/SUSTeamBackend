package susteam.commentThumb

import com.google.inject.Inject
import susteam.ServiceException
import susteam.user.Auth
import susteam.user.username

class CommentThumbService @Inject constructor(
    private val repository: CommentThumbRepository
) {
    suspend fun setCommentThumb(
        auth: Auth,
        gameId: Int,
        commenter: String,
        voteNum: Int
    ) {
        if (voteNum < -1 || voteNum > 1) {
            throw ServiceException("Vote num is invalid")
        }

        if (!repository.insertCommentThumb(auth.username, gameId, commenter, voteNum)) {
            repository.changeCommentThumb(auth.username, gameId, commenter, voteNum)
        }
    }

    suspend fun getCommentThumbSum(
        gameId: Int,
        commenter: String
    ): Int {
        return repository.calCommentThumbSum(gameId, commenter) ?: 0
    }

    suspend fun getCommentThumbByGame(gameId: Int, username: String): List<CommentThumb> {
        return repository.getCommentThumbByGame(gameId, username)
    }

    suspend fun getCommentThumbSumByGame(gameId: Int): List<Triple<String,Int,Int>> {
        return repository.getCommentThumbSumByGame(gameId)
    }

}
