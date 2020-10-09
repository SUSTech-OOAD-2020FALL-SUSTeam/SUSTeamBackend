package susteam.user

import com.google.inject.Inject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException

class UserController @Inject constructor(private val service: UserService) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/token").coroutineHandler(::handleGetToken)
        router.post("/token").coroutineHandler(::handleLogin)

        router.post("/user").coroutineHandler(::handleSignUp)
        router.get("/user/:username").coroutineHandler(::handleGetUser)
    }

    suspend fun handleGetToken(context: RoutingContext) {
        val user: Auth? = context.user()

        if (user == null) {
            context.success(
                jsonObjectOf(
                    "token" to false
                )
            )
            return
        }

        context.success(
            jsonObjectOf("token" to true).mergeIn(user.principal())
        )
    }

    suspend fun handleLogin(context: RoutingContext) {
        val params = context.bodyAsJson
        val username = params.getString("username") ?: throw ServiceException("Username is empty")
        val password = params.getString("password") ?: throw ServiceException("Password is empty")

        val token = service.authUser(username, password)

        context.success(
            jsonObjectOf(
                "token" to token
            )
        )
    }

    suspend fun handleSignUp(context: RoutingContext) {
        val params = context.bodyAsJson
        val username = params.getString("username") ?: throw ServiceException("Username is empty")
        val password = params.getString("password") ?: throw ServiceException("Password is empty")
        val mail = params.getString("mail") ?: throw ServiceException("Mail is empty")

        service.signUpUser(username, password, mail)

        context.success()
    }

    suspend fun handleGetUser(context: RoutingContext) {
        val request = context.request()
        val username = request.getParam("username") ?: throw ServiceException("Username not found")

        val user = service.getUser(username)

        context.success(
            jsonObjectOf(
                "user" to user.toJson()
            )
        )
    }

}