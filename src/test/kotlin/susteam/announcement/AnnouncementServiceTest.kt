package susteam.announcement

import com.google.inject.Guice
import com.google.inject.Key
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import susteam.ServiceException
import susteam.TestModule
import susteam.announcement.impl.AnnouncementRepositoryMock
import susteam.game.impl.GameRepositoryMock
import susteam.user.Auth

class AnnouncementServiceTest : StringSpec() {
    init {
        val module = TestModule.create()
        val injector = Guice.createInjector(module)

        val announcementRepository = AnnouncementRepositoryMock().apply { init() }
        val gameRepository = GameRepositoryMock().apply { init() }
        val service = AnnouncementService(announcementRepository, gameRepository)

        "test createAnnouncement" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            shouldThrow<ServiceException> {
                service.createAnnouncement(auth, 99999, "Announcement", "Game does not exist.")
            }
            shouldNotThrowAny {
                service.createAnnouncement(auth, 1, "Announcement", "Successful Announcement")
            }
            shouldThrow<ServiceException> {
                service.createAnnouncement(auth, 1, "Announcement", "Duplicate Announcement")
            }
        }

        "test getAnnouncementsByGame" {
            val announcement = service.getAnnouncementsByGame(1).getOrNull(0)
            announcement shouldNotBe null
            if (announcement != null) {
                announcement.gameId shouldBe 1
                announcement.title shouldBe "Announcement"
                announcement.content shouldBe "Successful Announcement"
            }
        }
    }
}