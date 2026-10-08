package org.example.gemswap.domain

class SwapValidator(
    private val matchFinder: MatchFinder = MatchFinder()
) {

    fun isValidSwap(board: Board, from: Position, to: Position): Boolean {
        if (!board.isInBounds(from) || !board.isInBounds(to)) return false
        if (!areNeighbors(from, to)) return false

        val swappedBoard = board.swapped(from, to)
        return matchFinder.findMatches(swappedBoard).isNotEmpty()
    }

    private fun areNeighbors(a: Position, b: Position): Boolean {
        val rowDiff = kotlin.math.abs(a.row - b.row)
        val colDiff = kotlin.math.abs(a.col - b.col)
        return (rowDiff == 1 && colDiff == 0) || (rowDiff == 0 && colDiff == 1)
    }

    fun hasAnyValidMove(board: Board): Boolean {
        return board.allAdjacentPositionPairs().any { (from, to) -> isValidSwap(board, from, to) }
    }
}