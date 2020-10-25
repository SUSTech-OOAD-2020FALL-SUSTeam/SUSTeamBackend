package susteam.user.impl

import susteam.ServiceException
import susteam.repository.impl.RepositoryMock
import susteam.user.User
import susteam.user.UserRepository
import susteam.user.UserRole

class UserRepositoryMock(
    override val dataset: Map<String, MutableList<*>> = mapOf(
        "user" to mutableListOf<UserRepositoryMockItem>()
    )
) : UserRepository, RepositoryMock {

    data class UserRepositoryMockItem(
        var user: User,
        val password: String,
        val roles: List<String>
    )

    val admin = UserRepositoryMockItem(
        User("admin", "admin@susteam.com", null, null, 0),
        "\$sha512\$\$58255bd09ab4938bfdfa636fe1a3254be1985762f2ccef2556d67998c9925695\$ujJTh2rta8ItSm/1PYQGxq2GQZXtFEq1yHYhtsIztUi66uaVbfNG7IwX9eoQ817jy8UUeX7X3dMUVGTioLq0Ew==",
        listOf("admin")
    )

    @Suppress("UNCHECKED_CAST")
    private val data: MutableList<UserRepositoryMockItem> = dataset["user"] as MutableList<UserRepositoryMockItem>

    override fun init() {
        data.add(admin)
    }


    override suspend fun create(username: String, password: String, mail: String) {
        if (data.find { it.user.username == username } != null)
            throw ServiceException("Cannot create user '$username'")
        if (data.find { it.user.mail == mail } != null)
            throw ServiceException("Cannot create user '$username', mail '$mail' already exist")

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

    override suspend fun updateUser(user: User) {
        data.find{ it.user.username == user.username }?.let{
            it.user = User(user.username, user.mail, user.avatar, user.description, user.balance)
        }
    }
}
