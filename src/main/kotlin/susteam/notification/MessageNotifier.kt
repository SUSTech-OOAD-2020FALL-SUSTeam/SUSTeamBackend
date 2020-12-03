package susteam.notification

import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import java.net.URLEncoder

object MessageNotifier {

    lateinit var eventBus: EventBus

    fun sendTo(username: String, message: JsonObject) {
        eventBus.send("/messageList/${URLEncoder.encode(username, Charsets.UTF_8)}", message)
    }

}