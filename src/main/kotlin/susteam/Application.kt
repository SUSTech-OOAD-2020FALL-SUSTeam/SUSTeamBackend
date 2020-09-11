package susteam

import io.vertx.kotlin.coroutines.CoroutineVerticle

class Application: CoroutineVerticle() {

    override suspend fun start() {
        println("Hello Vert.x")
        vertx.close()
    }

}