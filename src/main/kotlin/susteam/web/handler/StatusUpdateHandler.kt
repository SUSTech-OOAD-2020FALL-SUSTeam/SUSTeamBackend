package susteam.web.handler

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import susteam.status.UserStatus

class StatusUpdateHandler : Handler<RoutingContext> {
    override fun handle(context: RoutingContext) {
        context.user()?.let {
            UserStatus.updateStatusWithApiCalled(it)
        }
        context.next()
    }
}