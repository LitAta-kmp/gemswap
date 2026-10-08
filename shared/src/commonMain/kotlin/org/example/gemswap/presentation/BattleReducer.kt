package org.example.gemswap.presentation

import org.example.gemswap.domain.*

class BattleReducer(
    private val boardGenerator: BoardGenerator,
    private val swapValidator: SwapValidator,
    private val cascadeResolver: CascadeResolver
) {
    fun reduce(state: BattleState, intent: BattleIntent): BattleState = when (intent) {

        is BattleIntent.StartBattle -> BattleState(
            board = boardGenerator.generatePlayableBoard(GameRules.GRID_ROWS, GameRules.GRID_COLS, swapValidator),
            status = BattleStatus.InProgress
        )

        is BattleIntent.TurnTimeTicked -> {
            if (state.status != BattleStatus.InProgress) {
                state
            } else {
                val newTime = state.turnTimeRemainingSeconds - 1
                if (newTime <= 0) {
                    state.copy(
                        currentTurn = state.currentTurn.opposite(),
                        turnTimeRemainingSeconds = GameRules.BATTLE_TURN_DURATION_SECONDS
                    )
                } else {
                    state.copy(turnTimeRemainingSeconds = newTime)
                }
            }
        }

        is BattleIntent.ActivateSkullClearAbility -> {
            val greenCount = state.playerColorCounts[TileColor.GREEN] ?: 0
            if (state.status != BattleStatus.InProgress ||
                state.currentTurn != BattleTurn.PLAYER ||
                greenCount < GameRules.BATTLE_ABILITY_GREEN_THRESHOLD
            ) {
                state
            } else {
                val redTilesCleared = state.board.cells.flatten().count { it?.color == TileColor.RED }
                val clearedCells = state.board.cells.map { row ->
                    row.map { tile -> if (tile?.color == TileColor.RED) null else tile }
                }
                val clearedBoard = state.board.copy(cells = clearedCells)
                val fallen = cascadeResolver.applyGravity(clearedBoard)
                val refilled = cascadeResolver.refill(fallen)

                val newCounts = state.playerColorCounts.toMutableMap().apply {
                    this[TileColor.GREEN] = greenCount - GameRules.BATTLE_ABILITY_GREEN_THRESHOLD
                }
                val damage = redTilesCleared * GameRules.BATTLE_DAMAGE_PER_RED_TILE
                state.copy(
                    board = refilled,
                    playerColorCounts = newCounts,
                    npcHp = (state.npcHp - damage).coerceAtLeast(0)
                )
            }
        }

        is BattleIntent.PlayerSwapRequested -> state // реальная обработка — в BattleViewModel
    }
}

private fun BattleTurn.opposite(): BattleTurn = when (this) {
    BattleTurn.PLAYER -> BattleTurn.NPC
    BattleTurn.NPC -> BattleTurn.PLAYER
}