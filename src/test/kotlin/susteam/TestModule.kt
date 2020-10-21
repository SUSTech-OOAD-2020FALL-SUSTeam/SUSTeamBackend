package susteam

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import io.vertx.core.Vertx
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.core.json.jsonObjectOf
import kotlinx.coroutines.runBlocking
import susteam.announcement.AnnouncementRepository
import susteam.comment.CommentRepository
import susteam.game.GameRepository
import susteam.storage.StorageFileFactory
import susteam.storage.StorageImageFactory
import susteam.storage.StorageRepository
import susteam.user.Auth
import susteam.user.UserRepository
import susteam.user.impl.UserRepositoryMock

class TestModule(
    private val vertx: Vertx,
    private val config: JsonObject
) : AbstractModule() {

    companion object {
        @JvmStatic
        fun create(): TestModule {
            val dbConfig = JsonObject(this::class.java.classLoader.getResource("database_config.json")!!.readText())
            val webConfig = JsonObject(this::class.java.classLoader.getResource("webserver_config.json")!!.readText())

            return TestModule(
                Vertx.vertx(),
                jsonObjectOf(
                    "database_config" to dbConfig,
                    "webserver_config" to webConfig
                )
            )
        }
    }

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

    @BindingAnnotation
    @Target(
        AnnotationTarget.FIELD,
        AnnotationTarget.VALUE_PARAMETER,
        AnnotationTarget.FUNCTION,
        AnnotationTarget.PROPERTY_GETTER,
        AnnotationTarget.PROPERTY_SETTER
    )
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AdminAuth

    override fun configure() {
        val webConfig = config.getJsonObject("webserver_config")

        bind(Vertx::class.java).toInstance(vertx)
        bind(JsonObject::class.java).annotatedWith(Config::class.java).toInstance(config)

        val jwtAuth = JWTAuth.create(
            vertx, JWTAuthOptions().addPubSecKey(
                PubSecKeyOptions(webConfig.getJsonObject("rsa_key"))
            )
        )
        bind(JWTAuth::class.java).toInstance(jwtAuth)
        bind(FileSystem::class.java).toInstance(vertx.fileSystem())

        StorageImageFactory.urlPrefix = "${webConfig.getString("server_url")}/api/image"
        StorageImageFactory.pathPrefix = "storage/image"
        StorageFileFactory.urlPrefix = "${webConfig.getString("server_url")}/api/store"
        StorageFileFactory.pathPrefix = "storage/store"

        val adminToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJ1c2VybmFtZSI6ImFkbWluIiwicGVybWlzc2lvbnMiOlsicm9sZTphZG1pbiJdLCJpYXQiOjE2MDAwODgyODB9.aQyz2Xc1fFBWc3D16bP_oEPnEVK3J3Xa-boAVyTdfAgYYmHFGSO5NkrmOKRbJ84Su3m9e9eLrDlTwttJhjHPFY133x8OvxNmjm2FYqrpa5aNnt6X0MnXmSBioE4MFKE63P_O_NaC-bdglVFKG7HmCyTkr5EMIFwZUYMeYSZiWhSvh2t8qEgw0HyhBW3EmIbN2_Xg-hi0GSnm6mwKpigMWTFvCgJKQMlmZomNfSXpSTI-8hEhrOcWzvoV65KdSCcypwzHUC_9WB4DgUQSrZKP3zSFjADfupjs0CiYa0zTu0cCFoivbYfqn46PuwXB01Cu1bB035WeV9LE9ESDKDhUSw"
        jwtAuth.authenticate(jsonObjectOf("jwt" to adminToken)) { result ->
            bind(Auth::class.java).annotatedWith(AdminAuth::class.java).toInstance(result.result())
        }
    }

}