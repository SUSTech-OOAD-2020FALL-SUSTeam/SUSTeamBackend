package susteam.web

import com.google.inject.Guice
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.coroutineScope
import susteam.Controller
import susteam.achievement.AchievementController
import susteam.announcement.AnnouncementController
import susteam.comment.CommentController
import susteam.commentThumb.CommentThumbController
import susteam.discount.DiscountController
import susteam.friend.FriendController
import susteam.game.GameController
import susteam.order.OrderController
import susteam.record.RecordController
import susteam.save.GameSaveController
import susteam.status.UserStatus
import susteam.storage.StorageController
import susteam.user.UserController
import susteam.web.handler.StatusUpdateHandler
import susteam.web.handler.TokenUserHandler

class APIServerVerticle : CoroutineVerticle() {

    lateinit var router: Router
    lateinit var module: ServiceModule

    override suspend fun start() = coroutineScope {
        router = Router.router(vertx)
        module = ServiceModule(vertx, config)

        config.getJsonObject("webserver_config").getString("cors_origin")?.let {
            router.route().handler(
                CorsHandler.create(it)
                    .allowedHeader("Content-Type")
                    .allowedHeader("Authorization")
                    .allowedMethod(HttpMethod.GET)
                    .allowedMethod(HttpMethod.POST)
                    .allowedMethod(HttpMethod.HEAD)
                    .allowedMethod(HttpMethod.OPTIONS)
                    .allowedMethod(HttpMethod.DELETE)
                    .allowedMethod(HttpMethod.PUT)
            )
        }

        router.route().handler(BodyHandler.create().setDeleteUploadedFilesOnEnd(true))


        router.route().handler { routingContext ->
            routingContext.response().putHeader("Content-Type", "application/json")
            routingContext.next()
        }

        val injector = Guice.createInjector(module)

        router.route().handler(injector.getInstance(TokenUserHandler::class.java))
        router.route().handler(injector.getInstance(StatusUpdateHandler::class.java))

        val controllers: List<Controller> = listOf(
            injector.getInstance(UserController::class.java),
            injector.getInstance(GameController::class.java),
            injector.getInstance(StorageController::class.java),
            injector.getInstance(CommentController::class.java),
            injector.getInstance(AnnouncementController::class.java),
            injector.getInstance(OrderController::class.java),
            injector.getInstance(GameSaveController::class.java),
            injector.getInstance(FriendController::class.java),
            injector.getInstance(AchievementController::class.java),
            injector.getInstance(RecordController::class.java),
            injector.getInstance(CommentThumbController::class.java),
            injector.getInstance(DiscountController::class.java)
        )
        for (controller in controllers) {
            controller.route(router)
        }

        vertx.setPeriodic(1000 * 60) {
            UserStatus.updateOnlineStatus()
        }

        Unit

    }

    fun router(): Router = router

    override suspend fun stop() {
        module.close()
    }

}
