package susteam.friend

import com.google.inject.Guice
import com.google.inject.Key
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.stomp.Destination
import io.vertx.ext.stomp.StompClient
import io.vertx.ext.stomp.StompServer
import io.vertx.ext.stomp.StompServerHandler
import io.vertx.kotlin.core.json.jsonObjectOf
import io.vertx.kotlin.ext.bridge.permittedOptionsOf
import io.vertx.kotlin.ext.stomp.*
import kotlinx.coroutines.*
import susteam.TestModule
import susteam.friend.impl.FriendRepositoryMock
import susteam.notification.MessageNotifier
import susteam.status.UserStatus
import susteam.user.Auth
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

@Suppress("BlockingMethodInNonBlockingContext")
class FriendServiceTest : StringSpec() {
    init {
        val module = TestModule.create()
        val injector = Guice.createInjector(module)
        val vertx = injector.getInstance(Vertx::class.java)
        val webConfig = injector.getInstance(Key.get(JsonObject::class.java, TestModule.Config::class.java))
            .getJsonObject("webserver_config")

        val messageQueue = ArrayBlockingQueue<String>(4096)

        runBlocking {
            StompServer.create(vertx).handler(
                StompServerHandler.create(vertx).bridge(
                    bridgeOptionsOf(
                        inboundPermitteds = listOf(permittedOptionsOf(addressRegex = """^/messageList/\w+$""")),
                        outboundPermitteds = listOf(permittedOptionsOf(addressRegex = """^/messageList/\w+$"""))
                    )
                ).destinationFactory { _, name ->
                    if (name.startsWith("/messageList")) {
                        return@destinationFactory Destination.topic(vertx, name)
                    } else {
                        return@destinationFactory null
                    }
                }
            ).listenAwait(
                webConfig.getInteger("stompPort"),
                webConfig.getString("host")
            )

            StompClient.create(vertx).connectAwait(
                webConfig.getInteger("stompPort"),
                webConfig.getString("host")
            ).subscribe("/messageList/test001") {
                messageQueue.add(it.bodyAsString)
            }

            StompClient.create(vertx).connectAwait(
                webConfig.getInteger("stompPort"),
                webConfig.getString("host")
            ).subscribe("/messageList/test002") {
                messageQueue.add(it.bodyAsString)
            }

            StompClient.create(vertx).connectAwait(
                webConfig.getInteger("stompPort"),
                webConfig.getString("host")
            ).subscribe("/messageList/author") {
                messageQueue.add(it.bodyAsString)
            }
        }

        val repository = FriendRepositoryMock().apply { init() }
        val status = injector.getInstance(UserStatus::class.java)
        val notifier = injector.getInstance(MessageNotifier::class.java)
        val service = FriendService(repository, status, notifier)

        "test getFriends" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            service.getFriends(auth) shouldBe listOf(
                Friend("poorCustomer", false, null),
                Friend("richCustomer", false, null)
            )
        }

        "test addFriend" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            messageQueue.clear()
            service.addFriend(auth, "test001")
            messageQueue.poll(2, TimeUnit.SECONDS) shouldBe jsonObjectOf(
                "message" to "A friend application from admin"
            ).toString()
        }

        "test getApplicationList" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            service.getApplicationList(auth) shouldBe listOf(
                FriendApplication("poorCustomer", "accept"),
                FriendApplication("richCustomer", "accept"),
                FriendApplication("test001", "pending")
            )
        }

        "test getReplyList" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            service.getReplyList(auth) shouldBe listOf(
                FriendReply("author", "pending"),
                FriendReply("test002", "pending")
            )
        }

        "test replyTo" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            messageQueue.clear()
            service.replyTo(auth, "author", true) shouldBe true
            messageQueue.poll(5, TimeUnit.SECONDS) shouldBe jsonObjectOf(
                "message" to "admin has accepted your application"
            ).toString()
            service.replyTo(auth, "test002", false) shouldBe true
            messageQueue.poll(5, TimeUnit.SECONDS) shouldBe jsonObjectOf(
                "message" to "admin has rejected your application"
            ).toString()
            service.replyTo(auth, "test002", true) shouldBe false
            messageQueue.poll(5, TimeUnit.SECONDS) shouldBe null
        }
    }
}