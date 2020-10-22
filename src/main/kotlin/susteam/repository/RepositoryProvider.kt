package susteam.repository

interface RepositoryProvider<T> {

    suspend fun transaction(): Transaction

    suspend fun provide(transaction: Transaction): T

    suspend fun <R> transaction(block: suspend (Transaction, T) -> R): R {
        return transaction().use { transaction ->
            val repository = provide(transaction)
            block(transaction, repository)
        }
    }

}