package susteam.game

import com.google.inject.Guice
import com.google.inject.Key
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import susteam.ServiceException
import susteam.TestModule
import susteam.game.impl.GameRepositoryMock
import susteam.order.impl.OrderRepositoryMock
import susteam.storage.toStorageFile
import susteam.storage.toStorageImage
import susteam.user.Auth


class GameServiceTest : StringSpec() {
    init {
        val module = TestModule.create()
        val injector = Guice.createInjector(module)

        val repository = GameRepositoryMock().apply { init() }
        val orderRepository = OrderRepositoryMock().apply { init() }
        val service = GameService(repository, orderRepository)

        "test getGame" {
            shouldThrow<ServiceException> {
                service.getGame(1111111)
            }
            val game = service.getGame(1)
            game.name shouldBe "mock"
        }

        "test getGameVersion" {
            shouldThrow<ServiceException> {
                service.getGameVersion(1111111, "??????")
            }
            shouldThrow<ServiceException> {
                service.getGameVersion(1, "??????")
            }
            shouldThrow<ServiceException> {
                service.getGameVersion(2, "v1.0")
            }
            val gv = service.getGameVersion(1, "v1.0")
            gv.url shouldNotBe null
            //TODO url still not test
        }

        "test getGameProfile" {
            shouldThrow<ServiceException> {
                service.getGameProfile(99999)
            }
            val gp = service.getGameProfile(1)
            gp.imageFullSize?.url shouldNotBe null
            gp.imageCardSize?.url shouldBe null
            gp.name shouldBe "mock"
            gp.price shouldBe 100
        }

        "test getGameDetail" {
            shouldThrow<ServiceException> {
                service.getGameDetail(99999)
            }
            val gd = service.getGameDetail(1)
            gd.tags.contains("heihei") shouldBe true
            gd.tags.contains("hhhhhh") shouldBe false
            gd.game.name shouldBe "mock"
            gd.game.price shouldBe 100
        }

        "test publishGame" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            service.publishGame(auth, "hello", 100, "1", "11")
            service.getGame(2) shouldNotBe null
            shouldThrow<ServiceException> {
                service.publishGame(auth, "hello", 100, "1", "11")
            }
        }

        "test updateGame" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            val game = service.getGame(1)
            service.updateGame(auth, 1, game.copy(description = "22"))
            service.getGame(1).description shouldBe "22"
            shouldThrow<ServiceException> {
                service.updateGame(auth, 1111, game.copy(description = "22"))
            }
        }

        "test publishVersion" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            service.publishGameVersion(auth, 1, "v1", "111".toStorageFile())

            shouldThrow<ServiceException> {
                service.publishGameVersion(auth, 999, "vvv", "111".toStorageFile())
            }
            shouldThrow<ServiceException> {
                service.publishGameVersion(auth, 1, "v1", "1222".toStorageFile())
            }
            shouldThrow<ServiceException> {
                service.publishGameVersion(auth, 1, "v2", "111".toStorageFile())
            }
        }

        "test getAllGameProfileOrderByPublishDate" {
            //TODO
        }

        "test getAllGameProfileOrderByName" {
            //TODO
        }

        "test getAllGameProfile" {
            //TODO
        }

        "test getRandomGameProfile" {
            //TODO
        }

        "test getGameProfileWithTags" {
            //TODO
        }

        "test uploadGameImage" {
            shouldThrow<ServiceException> {
                service.uploadGameImage(1, "urlimage2".toStorageImage(), "J")
            }
        }

        "test getTag" {
            val list = service.getTag(1)
            list.contains("heihei") shouldBe true
            list.contains("huohuo") shouldBe true
            list.contains("???") shouldBe false
        }

        "test getAllTag" {
            val list = service.getAllTag()
            list.contains("heihei") shouldBe true
            list.contains("huohuo") shouldBe true
            list.contains("???") shouldBe false
        }

        "test addTag" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            service.addTag(auth, 1, "haha")
            shouldThrow<ServiceException> {
                service.addTag(auth, 1, "haha")
            }
        }
    }
}
