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

class FriendServiceTest : StringSpec() {
    init {


        val module = TestModule.create()
        val injector = Guice.createInjector(module)
        val vertx = injector.getInstance(Vertx::class.java)
        val webConfig = injector.getInstance(Key.get(JsonObject::class.java, TestModule.Config::class.java))
            .getJsonObject("webserver_config")

        val messageList = mutableListOf<String>()
        val messageQueue = ArrayBlockingQueue<String>(4096)

        StompServer.create(
            vertx, stompServerOptionsOf(
                port = webConfig.getInteger("stompPort"),
                host = webConfig.getString("host"),
                secured = true
            )
        ).handler(
            StompServerHandler.create(vertx).bridge(
                bridgeOptionsOf(
                    inboundPermitteds = listOf(permittedOptionsOf(addressRegex = """^/messageList/\w+$""")),
                    outboundPermitteds = listOf(permittedOptionsOf(addressRegex = """^/messageList/\w+$"""))
                )
            )
        ).listen()


        val stompClient = StompClient.create(vertx).connect(
            webConfig.getInteger("stompPort"),
            webConfig.getString("host")
        ) {
            if (it.succeeded()) {
                val connection = it.result()
                println("Connection established")
                connection.subscribe("/messageList/test001") { frame ->
                    println(frame)
                }
            } else {
                println("Connection failed")
            }
        }

        val repository = FriendRepositoryMock().apply { init() }
        val status = injector.getInstance(UserStatus::class.java)
        val notifier = injector.getInstance(MessageNotifier::class.java)
        val service = FriendService(repository, status, notifier)

        println("Hello from outside")

        "test getFriends" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            service.getFriends(auth) shouldBe listOf(
                Friend("poorCustomer", false, null),
                Friend("richCustomer", false, null)
            )
            println("Hello 123")
        }

        "test addFriend" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            messageQueue.clear()
            service.addFriend(auth, "test001")
            messageQueue.poll(10, TimeUnit.SECONDS) shouldBe ""
        }

        "test getApplicationList" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
        }

        "test getReplyList" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
        }

        "test replyTo" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
        }
    }
}