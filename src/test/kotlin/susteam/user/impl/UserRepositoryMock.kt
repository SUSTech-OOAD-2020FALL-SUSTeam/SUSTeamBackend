package susteam.user.impl

import susteam.user.User
import susteam.user.UserRepository
import susteam.user.UserRole

class UserRepositoryMock : UserRepository {

    data class UserRepositoryMockItem(
        val user: User,
        val password: String,
        val roles: List<String>
    )

    val admin = UserRepositoryMockItem(
        User("admin", "admin@susteam.com", null, null, 0),
        "\$sha512\$\$58255bd09ab4938bfdfa636fe1a3254be1985762f2ccef2556d67998c9925695\$ujJTh2rta8ItSm/1PYQGxq2GQZXtFEq1yHYhtsIztUi66uaVbfNG7IwX9eoQ817jy8UUeX7X3dMUVGTioLq0Ew==",
        listOf("admin")
    )
    private val data: MutableList<UserRepositoryMockItem> = mutableListOf(
        admin
    )

    override suspend fun create(username: String, password: String, mail: String) {
        data.add(
            UserRepositoryMockItem(
                User(username, password, null, null, 0),
                password,
                emptyList()
            )
        )
    }

    override suspend fun get(username: String): User? {
        return data.find { it.user.username == username }?.user
    }

    override suspend fun getPasswordHash(username: String): String? {
        return data.find { it.user.username == username }?.password
    }

    override suspend fun getRole(username: String): UserRole? {
        return data.find { it.user.username == username }?.let {
            UserRole(it.user, it.roles)
        }
    }

}