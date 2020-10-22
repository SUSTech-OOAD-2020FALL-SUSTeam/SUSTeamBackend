package susteam.repository

import java.io.Closeable

interface Transaction : Closeable {

    suspend fun commit()
    suspend fun rollback()

}