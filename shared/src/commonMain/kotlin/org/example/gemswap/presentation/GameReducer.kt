package org.example.gemswap.presentation

import org.example.gemswap.domain.*

data class ReduceResult(
    val state: GameState,
    val sideEffect: GameSideEffect? = null
)

class GameReducer(
    private val boardGenerator: BoardGenerator,
    private val swapValidator: SwapValidator,
    private val matchFinder: MatchFinder,
    private val cascadeResolver: CascadeResolver
) {
    fun reduce(state: GameState, intent: GameIntent): ReduceResult = when (intent) {

        is GameIntent.StartGame -> {
            if (state.status == GameStatus.Playing) {
                ReduceResult(state)
            } else {
                ReduceResult(
                    state = GameState(
                        board = boardGenerator.generatePlayableBoard(
                            GameRules.GRID_ROWS,
                            GameRules.GRID_COLS,
                            swapValidator
                        ),
                        score = 0,
                        timeRemainingSeconds = GameRules.ROUND_DURATION_SECONDS,
                        status = GameStatus.Playing
                    )
                )
            }
        }

        is GameIntent.SwapRequested -> {
            if (state.status != GameStatus.Playing) {
                ReduceResult(state)
            } else if (!swapValidator.isValidSwap(state.board, intent.from, intent.to)) {
                ReduceResult(state, GameSideEffect.InvalidSwap(intent.from, intent.to))
            }else {
                val swappedBoard = state.board.swapped(intent.from, intent.to)
                val matched = matchFinder.findMatches(swappedBoard)
                val resolvedBoard = cascadeResolver.resolveCascade(swappedBoard, matched)

                ReduceResult(
                    state = state.copy(
                        board = resolvedBoard,
                        score = state.score + matched.size * GameRules.POINTS_PER_TILE
                    )
                )
            }
        }

        is GameIntent.TimeTicked -> {
            if (state.status != GameStatus.Playing) {
                ReduceResult(state)
            } else {
                val newTime = state.timeRemainingSeconds - 1
                if (newTime <= 0) {
                    ReduceResult(state.copy(timeRemainingSeconds = 0, status = GameStatus.Finished))
                } else {
                    ReduceResult(state.copy(timeRemainingSeconds = newTime))
                }
            }
        }
    }
}