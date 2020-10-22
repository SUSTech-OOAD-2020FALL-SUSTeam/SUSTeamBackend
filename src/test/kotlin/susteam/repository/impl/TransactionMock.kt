package susteam.repository.impl

import susteam.repository.Transaction

class TransactionMock : Transaction {

    val origins = mutableListOf<RepositoryMock>()

    val uncommitted: MutableMap<String, MutableList<*>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override suspend fun commit() {
        origins.forEach {
            val dataset = it.dataset
            uncommitted.forEach { (key, value) ->
                if (dataset.containsKey(key)) {
                    dataset.getValue(key).clear()
                    (dataset.getValue(key) as MutableList<Any>).addAll(value as MutableList<Any>)
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun rollback() {
        origins.forEach {
            val dataset = it.dataset
            dataset.forEach { (key, value) ->
                uncommitted[key]!!.clear()
                (uncommitted.getValue(key) as MutableList<Any>).addAll(value as MutableList<Any>)
            }
        }
    }

    override fun close() {}

    @Suppress("UNCHECKED_CAST")
    fun addOrigin(origin: RepositoryMock) {
        if (!origins.contains(origin)) {
            origins.add(origin)
            val dataset = origin.dataset
            dataset.forEach { (key, value) ->
                uncommitted[key] = mutableListOf<Any>()
                (uncommitted.getValue(key) as MutableList<Any>).addAll(value as MutableList<Any>)
            }
        }
    }

}