package org.example.gemswap.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.gemswap.domain.GameRound
import org.example.gemswap.domain.GameRules
import org.example.gemswap.domain.HistoryRepository

class SqlDelightHistoryRepository(
    database: GemSwapDatabase
) : HistoryRepository {

    private val queries = database.gameHistoryQueries

    override suspend fun saveRound(score: Int, playedAt: String) {
        queries.insertRound(score = score.toLong(), playedAt = playedAt)
        queries.trimToTopN(limit = GameRules.MAX_HISTORY_ENTRIES.toLong())
    }

    override fun observeAllRounds(): Flow<List<GameRound>> {
        return queries.selectAllRounds()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { GameRound(score = it.score.toInt(), playedAt = it.playedAt) } }
    }

    override suspend fun getBestScore(): Int? {
        return queries.selectBestScore().executeAsOne().MAX?.toInt()
    }
}