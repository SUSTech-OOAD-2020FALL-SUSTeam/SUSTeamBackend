package susteam.repository.impl

import io.vertx.ext.sql.SQLClient
import io.vertx.ext.sql.SQLOperations
import io.vertx.ext.sql.TransactionIsolation
import io.vertx.kotlin.ext.sql.getConnectionAwait
import io.vertx.kotlin.ext.sql.setAutoCommitAwait
import io.vertx.kotlin.ext.sql.setTransactionIsolationAwait
import susteam.repository.RepositoryProvider
import susteam.repository.Transaction

class RepositoryProviderImpl<T>(
    private val database: SQLClient,
    private val supplier: suspend (SQLOperations) -> T
) : RepositoryProvider<T> {

    override suspend fun transaction(): Transaction {
        val connection = database.getConnectionAwait()
        connection.setTransactionIsolationAwait(TransactionIsolation.SERIALIZABLE)
        connection.setAutoCommitAwait(false)
        return TransactionImpl(connection)
    }

    override suspend fun provide(transaction: Transaction): T {
        transaction as TransactionImpl
        return supplier(transaction.connection)
    }

}