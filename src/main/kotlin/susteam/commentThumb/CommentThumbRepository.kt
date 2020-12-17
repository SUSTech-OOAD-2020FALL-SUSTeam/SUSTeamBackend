package susteam.commentThumb


interface CommentThumbRepository {
    suspend fun insertCommentThumb(
        username: String,
        gameId: Int,
        commenter: String,
        voteNum: Int
    ): Boolean


    suspend fun changeCommentThumb(
        username: String,
        gameId: Int,
        commenter: String,
        voteNum: Int
    ): Boolean

    suspend fun calCommentThumbSum(
        gameId: Int,
        commenter: String
    ): Int?

    suspend fun getCommentThumbByGame(
        gameId: Int,
        username: String
    ): List<CommentThumb>

    suspend fun getCommentThumbSumByGame(
        gameId: Int
    ): List<Triple<String, Int, Int>>

}
