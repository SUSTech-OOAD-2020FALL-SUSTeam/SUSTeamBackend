package susteam.achievement

interface AchievementRepository {
    suspend fun getAchievement(
        gameId: Int,
        achievementName: String
    ): Achievement?

    suspend fun addAchievement(
        gameId: Int,
        achievementName: String,
        description: String,
        achieveCount: Int
    ): Int

    suspend fun getAllAchievement(
        gameId: Int
    ): List<Achievement>

    suspend fun changeUserAchievementProcess(
        username: String,
        achievementId: Int,
        rateOfProcess: Int,
        finished: Boolean
    ): Boolean

    suspend fun insertUserAchievementProcess(
        username: String,
        achievementId: Int,
        rateOfProcess: Int,
        finished: Boolean
    ): Boolean

    suspend fun getUserAchievementProcess(
            username: String,
            achievementId: Int
    ): UserAchievementProcess?

    suspend fun getValuedAchievementProcess(
            username: String
    ): List<ValuedAchievementProcess>

}
