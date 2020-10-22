package susteam.repository.impl

interface RepositoryMock {

    val dataset: Map<String, MutableList<*>>

    fun init()

}