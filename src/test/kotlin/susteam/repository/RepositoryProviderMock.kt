package susteam.repository

class RepositoryProviderMock<T : RepositoryMock>(
    val origin: T,
    val supplier: (Map<String, MutableList<*>>) -> T
) : RepositoryProvider<T> {

    override suspend fun transaction(): Transaction {
        return TransactionMock()
    }

    override suspend fun provide(transaction: Transaction): T {
        transaction as TransactionMock
        transaction.addOrigin(origin)
        return supplier(transaction.uncommitted)
    }

}