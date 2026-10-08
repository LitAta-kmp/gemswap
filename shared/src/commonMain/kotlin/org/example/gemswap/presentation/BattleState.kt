package org.example.gemswap.presentation

import org.example.gemswap.domain.Board
import org.example.gemswap.domain.GameRules
import org.example.gemswap.domain.TileColor

enum class BattleTurn { PLAYER, NPC }
enum class BattleStatus { Idle, InProgress, PlayerWon, NpcWon }

data class BattleState(
    val board: Board,
    val playerHp: Int = GameRules.BATTLE_STARTING_HP,
    val npcHp: Int = GameRules.BATTLE_STARTING_HP,
    val currentTurn: BattleTurn = BattleTurn.PLAYER,
    val turnTimeRemainingSeconds: Int = GameRules.BATTLE_TURN_DURATION_SECONDS,
    val playerColorCounts: Map<TileColor, Int> = emptyMap(),
    val npcColorCounts: Map<TileColor, Int> = emptyMap(),
    val status: BattleStatus = BattleStatus.Idle
)