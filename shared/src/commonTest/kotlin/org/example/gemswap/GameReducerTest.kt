package org.example.gemswap.presentation

import org.example.gemswap.domain.*
import kotlin.test.Test
import kotlin.test.assertEquals

class GameReducerTest {

    private val reducer = GameReducer(
        boardGenerator = BoardGenerator(TileIdGenerator()),
        swapValidator = SwapValidator(),
        matchFinder = MatchFinder(),
        cascadeResolver = CascadeResolver(TileIdGenerator())
    )

    @Test
    fun timeTicked_reachesZero_finishesGame() {
        val board = Board(rows = 1, cols = 1, cells = listOf(listOf(null)))
        val state = GameState(board = board, timeRemainingSeconds = 1, status = GameStatus.Playing)

        val result = reducer.reduce(state, GameIntent.TimeTicked)

        assertEquals(GameStatus.Finished, result.state.status)
        assertEquals(0, result.state.timeRemainingSeconds)
    }
}