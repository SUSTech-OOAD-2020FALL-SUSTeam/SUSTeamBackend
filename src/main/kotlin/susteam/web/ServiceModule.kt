package susteam.web

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import io.vertx.core.Vertx
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.ext.sql.closeAwait
import susteam.game.GameController
import susteam.game.GameRepository
import susteam.game.GameService
import susteam.storage.StorageController
import susteam.storage.StorageImageFactory
import susteam.storage.StorageRepository
import susteam.storage.StorageService
import susteam.user.UserController
import susteam.user.UserRepository
import susteam.user.UserService
import susteam.web.handler.TokenUserHandler

class ServiceModule(
    private val vertx: Vertx,
    private val config: JsonObject
) : AbstractModule() {

    @BindingAnnotation
    @Target(
        AnnotationTarget.FIELD,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
    )
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Config

    private val database = JDBCClient.create(vertx, config.getJsonObject("database_config"))

    override fun configure() {
        val webConfig = config.getJsonObject("webserver_config")

        bind(Vertx::class.java).toInstance(vertx)
        bind(JsonObject::class.java).annotatedWith(Config::class.java).toInstance(config)
        bind(JDBCClient::class.java).toInstance(database)
        bind(JWTAuth::class.java).toInstance(
            JWTAuth.create(
                vertx, JWTAuthOptions().addPubSecKey(
                    PubSecKeyOptions(webConfig.getJsonObject("rsa_key"))
                )
            )
        )
        bind(FileSystem::class.java).toInstance(vertx.fileSystem())

        bind(StorageImageFactory::class.java).toInstance(
            StorageImageFactory(
                "http://${webConfig.getString("host")}:${webConfig.getInteger("port")}/api/image",
                "storage/image"
            )
        )

        bind(TokenUserHandler::class.java)

        bind(UserController::class.java)
        bind(UserService::class.java)
        bind(UserRepository::class.java)

        bind(GameController::class.java)
        bind(GameService::class.java)
        bind(GameRepository::class.java)

        bind(StorageController::class.java)
        bind(StorageService::class.java)
        bind(StorageRepository::class.java)
    }

    suspend fun close() {
        database.closeAwait()
    }
}