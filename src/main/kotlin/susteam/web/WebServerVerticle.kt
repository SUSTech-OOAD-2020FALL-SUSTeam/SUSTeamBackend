package susteam.web

import io.vertx.core.DeploymentOptions
import io.vertx.ext.stomp.Destination
import io.vertx.ext.stomp.StompServer
import io.vertx.ext.stomp.StompServerHandler
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.file.readFileAwait
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.bridge.permittedOptionsOf
import io.vertx.kotlin.ext.stomp.bridgeOptionsOf
import io.vertx.kotlin.ext.stomp.closeAwait
import io.vertx.kotlin.ext.stomp.listenAwait
import kotlinx.coroutines.coroutineScope

class WebServerVerticle : CoroutineVerticle() {

    private lateinit var stompServer: StompServer

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

        router.route().handler(StaticHandler.create())

        val webConfig = config.getJsonObject("webserver_config")

        server.requestHandler(router).listenAwait(
            webConfig.getInteger("port"),
            webConfig.getString("host")
        )

        stompServer = StompServer.create(vertx).handler(
            StompServerHandler.create(vertx).bridge(
                bridgeOptionsOf(
                    inboundPermitteds = listOf(permittedOptionsOf(addressRegex = """^/messageList/\w+$""")),
                    outboundPermitteds = listOf(permittedOptionsOf(addressRegex = """^/messageList/.+$"""))
                )
            ).destinationFactory { _, name ->
                if (name.startsWith("/messageList")) {
                    return@destinationFactory Destination.topic(vertx, name)
                } else {
                    return@destinationFactory null
                }
            }
        ).listenAwait(
            webConfig.getInteger("stompPort"),
            webConfig.getString("host")
        )

        println("Web server listen on ${webConfig.getString("host")}:${webConfig.getInteger("port")}")

    }

    override suspend fun stop() {
        stompServer.closeAwait()
    }

}
