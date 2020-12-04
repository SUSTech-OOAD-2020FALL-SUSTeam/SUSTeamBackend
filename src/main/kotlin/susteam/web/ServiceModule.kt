package susteam.web

import com.google.inject.AbstractModule
import com.google.inject.BindingAnnotation
import com.google.inject.TypeLiteral
import io.vertx.core.Vertx
import io.vertx.core.file.FileSystem
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.sql.SQLOperations
import io.vertx.kotlin.ext.sql.closeAwait
import susteam.achievement.AchievementController
import susteam.achievement.AchievementRepository
import susteam.achievement.AchievementService
import susteam.achievement.impl.AchievementRepositoryImpl
import susteam.announcement.AnnouncementController
import susteam.announcement.AnnouncementRepository
import susteam.announcement.AnnouncementService
import susteam.announcement.impl.AnnouncementRepositoryImpl
import susteam.comment.CommentController
import susteam.comment.CommentRepository
import susteam.comment.CommentService
import susteam.comment.impl.CommentRepositoryImpl
import susteam.friend.FriendController
import susteam.friend.FriendRepository
import susteam.friend.FriendService
import susteam.friend.impl.FriendRepositoryImpl
import susteam.game.GameController
import susteam.game.GameRepository
import susteam.game.GameService
import susteam.game.impl.GameRepositoryImpl
import susteam.notification.MessageNotifier
import susteam.order.OrderController
import susteam.order.OrderRepository
import susteam.order.OrderService
import susteam.order.impl.OrderRepositoryImpl
import susteam.repository.RepositoryProvider
import susteam.repository.impl.RepositoryProviderImpl
import susteam.save.GameSaveController
import susteam.save.GameSaveRepository
import susteam.save.GameSaveService
import susteam.save.impl.GameSaveRepositoryImpl
import susteam.status.UserStatus
import susteam.storage.*
import susteam.user.UserController
import susteam.user.UserRepository
import susteam.user.UserService
import susteam.user.impl.UserRepositoryImpl
import susteam.web.handler.StatusUpdateHandler
import susteam.web.handler.TokenUserHandler
import javax.inject.Provider

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

        bind(SQLOperations::class.java).toInstance(database)

        bind(MessageNotifier::class.java).toInstance(MessageNotifier)

        MessageNotifier.eventBus = vertx.eventBus()

        StorageImageFactory.urlPrefix = "${webConfig.getString("server_url")}/api/image"
        StorageImageFactory.pathPrefix = "storage/image"
        StorageFileFactory.urlPrefix = "${webConfig.getString("server_url")}/api/store"
        StorageFileFactory.pathPrefix = "storage/store"

        bind(TokenUserHandler::class.java)
        bind(StatusUpdateHandler::class.java)

        bind(UserStatus::class.java).toInstance(UserStatus)

        bind(UserController::class.java)
        bind(UserService::class.java)
        bind(UserRepository::class.java).to(UserRepositoryImpl::class.java)
        bind(object : TypeLiteral<RepositoryProvider<UserRepository>>() {}).toProvider(Provider {
            RepositoryProviderImpl(database, ::UserRepositoryImpl)
        })

        bind(GameController::class.java)
        bind(GameService::class.java)
        bind(GameRepository::class.java).to(GameRepositoryImpl::class.java)

        bind(StorageController::class.java)
        bind(StorageService::class.java)
        bind(StorageRepository::class.java)

        bind(CommentController::class.java)
        bind(CommentService::class.java)
        bind(CommentRepository::class.java).to(CommentRepositoryImpl::class.java)

        bind(AnnouncementController::class.java)
        bind(AnnouncementService::class.java)
        bind(AnnouncementRepository::class.java).to(AnnouncementRepositoryImpl::class.java)

        bind(OrderController::class.java)
        bind(OrderService::class.java)
        bind(OrderRepository::class.java).to(OrderRepositoryImpl::class.java)

        bind(object : TypeLiteral<RepositoryProvider<OrderRepository>>() {}).toProvider(Provider {
            RepositoryProviderImpl(database, ::OrderRepositoryImpl)
        })

        bind(GameSaveController::class.java)
        bind(GameSaveService::class.java)
        bind(GameSaveRepository::class.java).to(GameSaveRepositoryImpl::class.java)

        bind(FriendController::class.java)
        bind(FriendService::class.java)
        bind(FriendRepository::class.java).to(FriendRepositoryImpl::class.java)

        bind(AchievementController::class.java)
        bind(AchievementService::class.java)
        bind(AchievementRepository::class.java).to(AchievementRepositoryImpl::class.java)
    }

    suspend fun close() {
        database.closeAwait()
    }
}
