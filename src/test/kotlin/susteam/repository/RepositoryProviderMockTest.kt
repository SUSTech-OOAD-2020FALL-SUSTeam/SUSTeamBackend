package susteam.repository

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import susteam.repository.impl.RepositoryMock
import susteam.repository.impl.RepositoryProviderMock

class UserServiceTest : StringSpec() {

    class IntRepositoryMock(
        override val dataset: Map<String, MutableList<*>> = mapOf(
            "int" to mutableListOf<Int>()
        )
    ) : RepositoryMock {

        @Suppress("UNCHECKED_CAST")
        val data: MutableList<Int> = dataset["int"] as MutableList<Int>

        fun getAll() = data.toList()
        fun add(int: Int) = data.add(int)
        fun remove(int: Int) = data.remove(int)
        operator fun contains(int: Int): Boolean = int in data

        override fun init() {
            data.addAll(listOf(1, 2, 3, 4, 5))
        }

    }

    init {
        val intOrigin = IntRepositoryMock().apply { init() }
        val provider = RepositoryProviderMock(intOrigin, ::IntRepositoryMock)

        "test transaction" {
            intOrigin.getAll() shouldBe listOf(1, 2, 3, 4, 5)

            provider.transaction { transaction, repository ->
                repository.add(6)

                repository.getAll() shouldBe listOf(1, 2, 3, 4, 5, 6)
                intOrigin.getAll() shouldBe listOf(1, 2, 3, 4, 5)

                transaction.commit()

                repository.getAll() shouldBe listOf(1, 2, 3, 4, 5, 6)
                intOrigin.getAll() shouldBe listOf(1, 2, 3, 4, 5, 6)

                repository.add(7)
                repository.remove(1)

                repository.getAll() shouldBe listOf(2, 3, 4, 5, 6, 7)
                intOrigin.getAll() shouldBe listOf(1, 2, 3, 4, 5, 6)

                transaction.rollback()

                repository.getAll() shouldBe listOf(1, 2, 3, 4, 5, 6)
                intOrigin.getAll() shouldBe listOf(1, 2, 3, 4, 5, 6)
            }

        }

    }

}