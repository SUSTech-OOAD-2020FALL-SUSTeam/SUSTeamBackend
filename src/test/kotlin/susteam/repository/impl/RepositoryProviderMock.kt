package susteam.repository.impl

import susteam.repository.RepositoryProvider
import susteam.repository.Transaction

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