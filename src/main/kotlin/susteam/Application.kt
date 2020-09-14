package susteam

import io.vertx.core.DeploymentOptions
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import susteam.web.WebServerVerticle

class Application : CoroutineVerticle() {

    override suspend fun start() {

        vertx.deployVerticleAwait(WebServerVerticle(), DeploymentOptions().setConfig(config))

    }


}