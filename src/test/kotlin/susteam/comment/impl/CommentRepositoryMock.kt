package susteam.comment.impl

import susteam.ServiceException
import susteam.comment.Comment
import susteam.comment.CommentRepository
import susteam.repository.impl.RepositoryMock
import java.time.Instant

class CommentRepositoryMock(
    override val dataset: Map<String, MutableList<*>> = mapOf(
        "comment" to mutableListOf<CommentRepositoryMockItem>()
    )
) : CommentRepository, RepositoryMock {
    @Suppress("UNCHECKED_CAST")
    private val data: MutableList<CommentRepositoryMockItem> =
        dataset["comment"] as MutableList<CommentRepositoryMockItem>

    data class CommentRepositoryMockItem(
        val username: String,
        val gameId: Int,
        var commentTime: Instant,
        var content: String,
        var score: Int
    )

    private fun CommentRepositoryMockItem.toComment(): Comment = Comment(
        username, gameId, commentTime, content, score
    )

    override fun init() {}

    override suspend fun create(username: String, gameId: Int, commentTime: Instant, content: String, score: Int) {
        if (getExists(username, gameId)) {
            throw ServiceException("Comment failed")
        }
        data.add(CommentRepositoryMockItem(username, gameId, commentTime, content, score))
    }

    override suspend fun getExists(username: String, gameId: Int): Boolean =
        data.find { it.username == username && it.gameId == gameId }?.let { true } ?: false

    override suspend fun modify(
        username: String,
        gameId: Int,
        commentTime: Instant,
        newContent: String,
        newScore: Int
    ) {
        data.find { it.username == username && it.gameId == gameId }?.let {
            it.commentTime = commentTime
            it.content = newContent
            it.score = newScore
        } ?: throw ServiceException("Cannot update comment")
    }

    override suspend fun getByUser(username: String): List<Comment> =
        data.filter { it.username == username }.map {
            it.toComment()
        }

    override suspend fun getByGame(gameId: Int): List<Comment> =
        data.filter { it.gameId == gameId }.map {
            it.toComment()
        }
}