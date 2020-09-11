package susteam

import io.vertx.core.Vertx
import io.vertx.kotlin.core.closeAwait
import io.vertx.kotlin.core.deployVerticleAwait

suspend fun main() {

    val vertx = Vertx.vertx()

    try {
        vertx.deployVerticleAwait(Application())
    } catch (t: Throwable) {
        t.printStackTrace()
        vertx.closeAwait()
    }

}