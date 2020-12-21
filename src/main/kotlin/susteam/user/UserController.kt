package susteam.user

import com.google.inject.Inject
import io.vertx.core.file.FileSystem
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.file.readFileAwait
import io.vertx.kotlin.core.json.jsonObjectOf
import susteam.CoroutineController
import susteam.ServiceException
import susteam.storage.StorageService
import javax.imageio.ImageIO

class UserController @Inject constructor(
    private val service: UserService,
    private val fileSystem: FileSystem,
    private val storage: StorageService
) : CoroutineController() {

    override fun route(router: Router) {
        router.get("/token").coroutineHandler(::handleGetToken)
        router.post("/token").coroutineHandler(::handleLogin)

        router.post("/user").coroutineHandler(::handleSignUp)
        router.get("/user/:username").coroutineHandler(::handleGetUser)
        router.put("/user/:username").coroutineHandler(::handleUpdateUser)
        router.put("/user/:username/avatar").coroutineHandler(::handleUploadAvatar)
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

        val userRole = service.getUserRole(user)

        context.success(
            jsonObjectOf(
                "token" to true,
                "tokenInfo" to user.principal(),
                "userRole" to userRole.toJson()
            )
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

    suspend fun handleUploadAvatar(context: RoutingContext) {
        val username = context.request().getParam("username") ?: throw ServiceException("Username not found")
        val user = context.user() ?: throw ServiceException("Permission denied, please login")

        if (!user.isAdmin() && username != user.username) {
            throw ServiceException("Permission denied")
        }

        val image = context.fileUploads().first()
        val bytes = fileSystem.readFileAwait(image.uploadedFileName()).bytes
        ImageIO.read(bytes.inputStream()) ?: throw ServiceException("Cannot decode image")

        val store = storage.uploadImage(image)
        service.updateUser(username, avatar = store.url)

        context.success()
    }

    suspend fun handleUpdateUser(context: RoutingContext) {
        val params = context.bodyAsJson
        val request = context.request()

        val username = request.getParam("username") ?: throw ServiceException("Username not found")
        val description: String? = params.getString("description")

        service.updateUser(username, description = description)

        context.success()
    }

}
