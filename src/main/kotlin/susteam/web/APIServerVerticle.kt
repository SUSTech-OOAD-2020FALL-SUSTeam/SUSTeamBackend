package susteam.web

import com.google.inject.Guice
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.coroutineScope
import susteam.Controller
import susteam.announcement.AnnouncementController
import susteam.comment.CommentController
import susteam.game.GameController
import susteam.storage.StorageController
import susteam.user.UserController
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
                    .allowedHeader("Access-Control-Allow-Method")
                    .allowedHeader("Access-Control-Allow-Origin")
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

        val controllers: List<Controller> = listOf(
            injector.getInstance(UserController::class.java),
            injector.getInstance(GameController::class.java),
            injector.getInstance(StorageController::class.java),
            injector.getInstance(CommentController::class.java),
            injector.getInstance(AnnouncementController::class.java)
        )
        for (controller in controllers) {
            controller.route(router)
        }

        Unit

    }

    fun router(): Router = router

    override suspend fun stop() {
        module.close()
    }

}