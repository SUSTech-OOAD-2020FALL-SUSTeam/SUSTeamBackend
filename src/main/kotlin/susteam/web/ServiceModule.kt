package susteam.web

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.ext.sql.closeAwait
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
        bind(Vertx::class.java).toInstance(vertx)
        bind(JsonObject::class.java).annotatedWith(Config::class.java).toInstance(config)
        bind(JDBCClient::class.java).toInstance(
            database
        )
        bind(JWTAuth::class.java).toInstance(
            JWTAuth.create(
                vertx, JWTAuthOptions().addPubSecKey(
                    PubSecKeyOptions(config.getJsonObject("webserver_config").getJsonObject("rsa_key"))
                )
            )
        )

        bind(TokenUserHandler::class.java)

        bind(UserController::class.java)
        bind(UserService::class.java)
        bind(UserRepository::class.java)
    }

    suspend fun close() {
        database.closeAwait()
    }
}