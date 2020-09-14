package susteam

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.closeAwait
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.json.jsonObjectOf
import kotlinx.coroutines.runBlocking

object Main {

    @JvmStatic
    fun main(args: Array<String>): Unit = runBlocking {

        val dbConfig = JsonObject(this::class.java.classLoader.getResource("database_config.json")!!.readText())
        val webConfig = JsonObject(this::class.java.classLoader.getResource("webserver_config.json")!!.readText())

        val option = DeploymentOptions().setConfig(
            jsonObjectOf(
                "database_config" to dbConfig,
                "webserver_config" to webConfig
            )
        )

        val vertx = Vertx.vertx()

        try {
            vertx.deployVerticleAwait(Application(), option)
        } catch (t: Throwable) {
            t.printStackTrace()
            vertx.closeAwait()
        }
    }
}
