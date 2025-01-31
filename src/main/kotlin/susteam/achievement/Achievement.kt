package susteam.achievement

import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.jsonObjectOf

data class Achievement(
    val gameId: Int,
    val achievementId: Int,
    val achievementName: String,
    val description: String,
    val achieveCount: Int
)

fun Achievement.toJson(): JsonObject = jsonObjectOf(
    "gameId" to gameId,
    "achievementId" to achievementId,
    "achievementName" to achievementName,
    "description" to description,
    "achieveCount" to achieveCount
)

fun JsonObject.toAchievement(): Achievement = Achievement(
    getInteger("gameId"),
    getInteger("achievementId"),
    getString("achievementName"),
    getString("description"),
    getInteger("achieveCount")
)

data class UserAchievementProcess(
    val username: String,
    val achievementId: Int,
    val rateOfProcess: Int,
    val finished: Boolean
)

fun UserAchievementProcess.toJson(): JsonObject = jsonObjectOf(
    "username" to username,
    "achievementId" to achievementId,
    "rateOfProcess" to rateOfProcess,
    "finished" to finished
)

fun JsonObject.toUserAchievementProcess(): UserAchievementProcess = UserAchievementProcess(
    getString("username"),
    getInteger("achievementId"),
    getInteger("rateOfProcess"),
    getBoolean("finished")
)


data class ValuedAchievementProcess(
        val username: String,
        val gameName: String,
        val achievementName: String,
        val rateOfProcess: Int,
        val achievementCount: Int,
)


fun ValuedAchievementProcess.toJson(): JsonObject = jsonObjectOf(
        "username" to username,
        "gameName" to gameName,
        "achievementName" to achievementName,
        "rateOfProcess" to rateOfProcess,
        "achievementCount" to achievementCount
)


fun JsonObject.toValuedAchievementProcess(): ValuedAchievementProcess = ValuedAchievementProcess(
        getString("username"),
        getString("gameName"),
        getString("achievementName"),
        getInteger("rateOfProcess"),
        getInteger("achievementCount")
)

