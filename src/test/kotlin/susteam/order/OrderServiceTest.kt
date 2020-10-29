package susteam.order

import com.google.inject.Guice
import com.google.inject.Key
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.vertx.core.Vertx
import susteam.TestModule
import susteam.game.impl.GameRepositoryMock
import susteam.order.impl.OrderRepositoryMock
import susteam.repository.RepositoryProvider
import susteam.repository.UserServiceTest
import susteam.repository.impl.RepositoryProviderMock
import susteam.user.Auth
import susteam.user.UserRepository
import susteam.user.impl.UserRepositoryMock
import susteam.user.username

class OrderServiceTest : StringSpec() {
    init {
        val module = TestModule.create()
        val injector = Guice.createInjector(module)

        val orderRepository = OrderRepositoryMock().apply { init() }
        val userRepository = UserRepositoryMock().apply { init() }
        val gameRepository = GameRepositoryMock().apply { init() }

        @Suppress("UNCHECKED_CAST")
        val orderRepositoryProvider =
            RepositoryProviderMock(orderRepository, ::OrderRepositoryMock) as RepositoryProvider<OrderRepository>
        @Suppress("UNCHECKED_CAST")
        val userRepositoryProvider =
            RepositoryProviderMock(userRepository, ::UserRepositoryMock) as RepositoryProvider<UserRepository>

        val vertx = Vertx.vertx()

        val service = OrderService(
            orderRepository, userRepository, gameRepository,
            orderRepositoryProvider, userRepositoryProvider,
            vertx)

        "test getOrderbyGameId" {
            service.getOrderByGameId(999).size shouldBe 0
            service.getOrderByGameId(1).size shouldBe 3
        }

        "test getOrderbyUsername" {
            service.getOrderByUsername("poorCustomer").size shouldBe 3
            service.getOrderByUsername("yinpeiqi").size shouldBe 0
        }

        "test createOrder" {
            val auth = injector.getInstance(Key.get(Auth::class.java, TestModule.AdminAuth::class.java))
            //TODO
        }
    }
}
