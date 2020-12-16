package susteam.record

interface RecordRepository {
    suspend fun insertRecord(
        gameId: Int,
        username: String,
        score: Int
    ): Int


    suspend fun getRankRecord(
        gameId: Int,
        rankNum: Int
    ): List<Record>

    suspend fun getUserScoreMax(
        username: String,
        gameId: Int
    ): Record?

}