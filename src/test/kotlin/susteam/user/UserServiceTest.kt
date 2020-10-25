package susteam.user

import com.google.inject.Guice
import com.google.inject.Key
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.vertx.ext.auth.jwt.JWTAuth
import susteam.ServiceException
import susteam.TestModule
import susteam.user.impl.UserRepositoryMock

class UserServiceTest : StringSpec() {
    init {
        val module = TestModule.create()
        val injector = Guice.createInjector(module)

        val repository = UserRepositoryMock().apply { init() }
        val jwt = injector.getInstance(JWTAuth::class.java)
        val service = UserService(repository, jwt)

        "test authUser" {
            service.authUser("admin", "123456") shouldNotBe null
            shouldThrow<ServiceException> {
                service.authUser("admin", "abcdef")
            }
        }

        "test signUpUser" {
            service.signUpUser("test001", "test001", "test001@susteam.com")
            service.getUser("test001") shouldNotBe null

            shouldThrow<ServiceException> {
                service.signUpUser("test001", "test001", "test001@susteam.com")
            }
        }

        "test getUser" {
            service.getUser("admin") shouldBe repository.admin.user
            shouldThrow<ServiceException> {
                service.getUser("UNKNOWN_USER")
            }
        }

        "test getUserRole" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            service.getUserRole(auth) shouldBe UserRole(
                repository.admin.user,
                repository.admin.roles
            )
        }

        "test updateUser" {
            service.updateUser(User("admin","aaa","hi","hahaha",10))
            val user = service.getUser("admin")
            user.mail shouldBe "aaa"
            user.avatar shouldBe "hi"
            user.description shouldBe "hahaha"
            user.balance shouldBe 10

            shouldThrow<ServiceException> {
                service.updateUser(User("benben","","","",0))
            }
        }
    }
}
