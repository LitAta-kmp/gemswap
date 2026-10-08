package org.example.gemswap.domain

import kotlinx.coroutines.flow.Flow

data class GameRound(
    val score: Int,
    val playedAt: String
)

interface HistoryRepository {
    suspend fun saveRound(score: Int, playedAt: String)
    fun observeAllRounds(): Flow<List<GameRound>>
    suspend fun getBestScore(): Int?
}