package susteam.repository.impl

import io.vertx.ext.sql.SQLConnection
import io.vertx.kotlin.ext.sql.commitAwait
import io.vertx.kotlin.ext.sql.rollbackAwait
import susteam.repository.Transaction

class TransactionImpl(val connection: SQLConnection) : Transaction {

    override suspend fun commit() {
        connection.commitAwait()
    }

    override suspend fun rollback() {
        connection.rollbackAwait()
    }

    override fun close() {
        connection.close()
    }
}