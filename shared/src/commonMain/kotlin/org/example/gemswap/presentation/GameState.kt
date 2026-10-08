package org.example.gemswap.presentation

import org.example.gemswap.domain.Board
import org.example.gemswap.domain.GameRules
import org.example.gemswap.domain.TileColor

enum class GameStatus { Idle, Playing, Finished }

data class GameState(
    val board: Board,
    val score: Int = 0,
    val colorCounts: Map<TileColor, Int> = emptyMap(),
    val timeRemainingSeconds: Int = GameRules.ROUND_DURATION_SECONDS,
    val status: GameStatus = GameStatus.Idle
)