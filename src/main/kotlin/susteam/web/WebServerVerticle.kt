package susteam.web

import io.vertx.core.DeploymentOptions
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.file.readFileAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlinx.coroutines.coroutineScope

class WebServerVerticle : CoroutineVerticle() {

    override suspend fun start() = coroutineScope {

        val server = vertx.createHttpServer()
        val router = Router.router(vertx)

        val api = APIServerVerticle()
        vertx.deployVerticleAwait(api, DeploymentOptions().setConfig(config))
        router.mountSubRouter("/api", api.router())


        val defaultAvatar = vertx.fileSystem().readFileAwait("default.jpg")

        router.get("/avatar/default.jpg").handler { context ->
            context.response().putHeader("Content-Type", "image/jpeg")
            context.response().end(defaultAvatar)
        }

        val webConfig = config.getJsonObject("webserver_config")

        server.requestHandler(router).listenAwait(
            webConfig.getInteger("port"),
            webConfig.getString("host")
        )

        println("Web server listen on ${webConfig.getString("host")}:${webConfig.getInteger("port")}")

        Unit

    }

}