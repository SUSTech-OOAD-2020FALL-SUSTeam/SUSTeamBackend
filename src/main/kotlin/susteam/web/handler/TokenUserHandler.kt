package susteam.web.handler

import com.google.inject.Inject
import io.vertx.core.Handler
import io.vertx.core.json.DecodeException
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf

class TokenUserHandler @Inject constructor(private val jwtAuth: JWTAuth) : Handler<RoutingContext> {

    override fun handle(context: RoutingContext) {
        val token = getToken(context)

        if (token != null) {
            jwtAuth.authenticate(jsonObjectOf("jwt" to token)) { result ->
                if (result.succeeded()) {
                    context.setUser(result.result())
                }
            }
        }

        context.next()
    }

    private fun getToken(context: RoutingContext): String? {
        val header = context.request().getHeader("Authorization")?.trim()
        if (header != null && header.startsWith("Bearer")) {
            return header.removePrefix("Bearer").trim()
        }

        return try {
            context.bodyAsJson?.getString("token")?.trim()
        } catch (e: DecodeException) {
            null
        }
    }

}