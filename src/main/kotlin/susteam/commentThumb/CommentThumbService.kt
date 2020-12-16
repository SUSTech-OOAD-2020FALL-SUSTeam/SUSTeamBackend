package susteam.commentThumb

import com.google.inject.Inject
import io.vertx.core.json.JsonObject
import susteam.ServiceException
import susteam.order.OrderStatus
import susteam.record.Record
import susteam.user.Auth
import susteam.user.username
import java.time.Instant

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

}
