package susteam.notification

import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject

object MessageNotifier {

    lateinit var eventBus: EventBus

    fun sendTo(username: String, message: JsonObject) {
        eventBus.send("/messageList/${username}", message)
    }

}