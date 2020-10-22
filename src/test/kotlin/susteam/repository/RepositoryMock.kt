package susteam.repository

interface RepositoryMock {

    val dataset: Map<String, MutableList<*>>

    fun init()

}