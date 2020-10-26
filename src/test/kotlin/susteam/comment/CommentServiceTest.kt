package susteam.comment

import com.google.inject.Guice
import com.google.inject.Key
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import susteam.ServiceException
import susteam.TestModule
import susteam.comment.impl.CommentRepositoryMock
import susteam.game.impl.GameRepositoryMock
import susteam.user.Auth
import susteam.user.impl.UserRepositoryMock

class CommentServiceTest : StringSpec() {
    init {
        val module = TestModule.create()
        val injector = Guice.createInjector(module)

        val commentRepository = CommentRepositoryMock().apply { init() }
        val userRepository = UserRepositoryMock().apply { init() }
        val gameRepository = GameRepositoryMock().apply { init() }
        val service = CommentService(commentRepository, userRepository, gameRepository)

        "test createComment" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            shouldThrow<ServiceException> {
                service.createComment(auth, 1000, "Comment on invalid game", 3)
            }
            shouldThrow<ServiceException> {
                service.createComment(auth, 1, "Comment with invalid score", -1)
            }
            shouldNotThrowAny {
                service.createComment(auth, 1, "Successful comment", 5)
            }
            shouldThrow<ServiceException> {
                service.createComment(auth, 1, "Duplicate comment", 5)
            }
        }

        "test modifyComment" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            shouldThrow<ServiceException> {
                service.modifyComment(auth, 1000, "Modify comment on invalid game", 3)
            }
            shouldThrow<ServiceException> {
                service.modifyComment(auth, 1, "Modify comment with invalid score", 6)
            }
            shouldNotThrowAny {
                service.modifyComment(auth, 1, "Successful modification", 2)
            }
        }

        "test getCommentsByUser" {
            // TODO Another way to obtain time for specific comment
            val comment = service.getCommentsByUser("admin").getOrNull(0).shouldNotBeNull()
            val refComment = Comment("admin", 1, comment.commentTime, "Successful modification", 2)
            comment shouldBe refComment
        }

        "test getCommentsByGame" {
            // TODO Another way to obtain time for specific comment
            val comment = service.getCommentsByGame(1).getOrNull(0).shouldNotBeNull()
            val refComment = Comment("admin", 1, comment.commentTime, "Successful modification", 2)
            comment shouldBe refComment
        }
    }
}