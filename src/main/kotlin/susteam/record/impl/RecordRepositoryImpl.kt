package susteam.record.impl

import com.google.inject.Inject
import io.vertx.ext.jdbc.JDBCClient
import io.vertx.kotlin.core.json.jsonArrayOf
import io.vertx.kotlin.ext.jdbc.querySingleWithParamsAwait
import io.vertx.kotlin.ext.sql.queryWithParamsAwait
import io.vertx.kotlin.ext.sql.updateWithParamsAwait
import susteam.ServiceException
import susteam.record.Record
import susteam.record.RecordRepository
import susteam.record.toRecord
import java.sql.SQLIntegrityConstraintViolationException

class RecordRepositoryImpl @Inject constructor(private val database: JDBCClient) : RecordRepository {
    override suspend fun insertRecord(gameId: Int, username: String, score: Int): Int {
        return try {
            database.updateWithParamsAwait(
                """INSERT INTO `record` (game_id, username, score) VALUES (?,?,?);""",
                jsonArrayOf(gameId, username, score)
            ).keys.getInteger(0)
        } catch (e: SQLIntegrityConstraintViolationException) {
            val message = e.message ?: throw e
            if (message.contains("FOREIGN KEY (`game_id`)")) {
                throw ServiceException("Cannot create record for game '$gameId', game '$gameId' do not exist")
            } else {
                throw e
            }
        }
    }

    override suspend fun getRankRecord(gameId: Int, rankNum: Int): List<Record> {
        return database.queryWithParamsAwait(
            """
                SELECT
                    record_id recordId,
                    username,
                    game_id gameId,
                    score
                FROM `record`
                WHERE game_id = ?
                order by score desc
                limit ?;
            """.trimIndent(),
            jsonArrayOf(gameId, rankNum)
        ).rows.map { it.toRecord() }
    }

    override suspend fun getUserScoreMax(username: String, gameId: Int): Record? {
        return database.querySingleWithParamsAwait(
            """SELECT recordId, gameId, username, score FROM
                        (SELECT
                            record_id recordId,
                            username,
                            game_id gameId,
                            score score,
                        max(score) OVER () max_score
                        FROM `record`
                        WHERE game_id = ? AND username = ?
                        ) sub
                    WHERE sub.score = sub.max_score
                    limit 1
                    """,
            jsonArrayOf(gameId, username))?.let {
            Record(
                it.getInteger(0), it.getInteger(1), it.getString(2), it.getInteger(3)
            )
        }
    }

}