package susteam.achievement.impl

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.achievement.*
import java.sql.SQLIntegrityConstraintViolationException

class AchievementRepositoryImpl @Inject constructor(private val database: JDBCClient) : AchievementRepository {
    override suspend fun getAchievement(gameId: Int, achievementName: String): Achievement? {
        return database.querySingleWithParamsAwait(
            """
                SELECT
                       game_id gameId,
                       achievement_id achievementId,
                       achievement_name achievementName,
                       description,
                       achieve_count achieveCount
                FROM achievement
                WHERE game_id = ? and achievement_name = ?;
            """.trimIndent(),
            jsonArrayOf(gameId, achievementName)
        )?.let {
            Achievement(
                it.getInteger(0), it.getInteger(1), it.getString(2),
                it.getString(3), it.getInteger(4)
            )
        }
    }

    override suspend fun addAchievement(
        gameId: Int,
        achievementName: String,
        description: String,
        achieveCount: Int
    ): Int {
        try {
            return database.updateWithParamsAwait(
                """INSERT INTO achievement (game_id, achievement_name, description, achieve_count) VALUES (?,?,?,?);""",
                jsonArrayOf(gameId, achievementName, description, achieveCount)
            ).keys.getInteger(0)
        } catch (e: SQLIntegrityConstraintViolationException) {
            val message = e.message ?: throw e

            if (message.contains("FOREIGN KEY (`game_id`)")) {
                throw ServiceException("Cannot create achievement '$achievementName' for game '$gameId', game '$gameId' do not exist")
            } else if (message.contains("achievementName")) {
                throw ServiceException(
                    "Cannot create achievement '$achievementName', achievement name already exist",
                    e
                )
            } else {
                throw e
            }
        }
    }

    override suspend fun getAllAchievement(gameId: Int): List<Achievement> {
        return database.queryWithParamsAwait(
            """
                SELECT
                       game_id gameId,
                       achievement_id achievementId,
                       achievement_name achievementName,
                       description,
                       achieve_count achieveCount
                FROM achievement
                WHERE game_id = ?;
            """.trimIndent(),
            jsonArrayOf(gameId)
        ).rows.map { it.toAchievement() }
    }

    override suspend fun changeUserAchievementProcess(
        username: String,
        achievementId: Int,
        rateOfProcess: Int,
        finished: Boolean
    ): Boolean {
        return database.updateWithParamsAwait(
            """UPDATE user_achievement_progress SET rate_of_process = ?, finished = ? WHERE username = ? and achievement_id = ?;""",
            jsonArrayOf(rateOfProcess, finished, username, achievementId)
        ).updated == 1
    }

    override suspend fun insertUserAchievementProcess(
        username: String,
        achievementId: Int,
        rateOfProcess: Int,
        finished: Boolean
    ): Boolean {
        try {
            database.updateWithParamsAwait(
                """INSERT INTO user_achievement_progress (username, achievement_id, rate_of_process, finished) VALUES (?,?,?,?);""",
                jsonArrayOf(username, achievementId, rateOfProcess, finished)
            )
            return true
        } catch (e: SQLIntegrityConstraintViolationException) {
            return false
        }
    }

    override suspend fun getUserAchievementProcess(username: String, achievementId: Int): UserAchievementProcess? {
        return database.querySingleWithParamsAwait(
            """SELECT username, achievement_id, rate_of_process, finished
                    FROM user_achievement_progress
                    WHERE username = ?
                    and achievement_id = ?;""",
            jsonArrayOf(username, achievementId)
        )?.let {
            UserAchievementProcess(
                it.getString(0), it.getInteger(1), it.getInteger(2), it.getBoolean(3)
            )
        }
    }

    override suspend fun getValuedAchievementProcess(username: String): List<ValuedAchievementProcess> {
        return database.queryWithParamsAwait(
            """
                SELECT username,
                       name             gameName,
                       achievement_name achievementName,
                       rate_of_process  rateOfProcess,
                       achieve_count    achievementCount
                FROM user_achievement_progress
                         join achievement a on user_achievement_progress.achievement_id = a.achievement_id
                         join game g on a.game_id = g.game_id
                WHERE username = ?
                  and rate_of_process > 0;
            """.trimIndent(),
            jsonArrayOf(username)
        ).rows.map { it.toValuedAchievementProcess() }
    }

}
