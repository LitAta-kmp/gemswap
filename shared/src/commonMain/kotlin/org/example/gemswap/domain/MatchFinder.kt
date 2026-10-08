package org.example.gemswap.domain

data class MatchRun(val positions: List<Position>, val color: TileColor)

class MatchFinder {

    fun findMatchRuns(board: Board): List<MatchRun> {
        return findHorizontalRuns(board) + findVerticalRuns(board)
    }

    fun findMatches(board: Board): Set<Position> {
        return findMatchRuns(board).flatMap { it.positions }.toSet()
    }

    private fun findHorizontalRuns(board: Board): List<MatchRun> {
        val result = mutableListOf<MatchRun>()
        for (row in 0 until board.rows) {
            var runStart = 0
            for (col in 1..board.cols) {
                val startColor = board.tileAt(Position(row, runStart))?.color
                val sameAsRunStart = col < board.cols &&
                        board.tileAt(Position(row, col))?.color == startColor &&
                        startColor != null

                if (!sameAsRunStart) {
                    val runLength = col - runStart
                    if (runLength >= 3 && startColor != null) {
                        val positions = (runStart until col).map { c -> Position(row, c) }
                        result += MatchRun(positions, startColor)
                    }
                    runStart = col
                }
            }
        }
        return result
    }

    private fun findVerticalRuns(board: Board): List<MatchRun> {
        val result = mutableListOf<MatchRun>()
        for (col in 0 until board.cols) {
            var runStart = 0
            for (row in 1..board.rows) {
                val startColor = board.tileAt(Position(runStart, col))?.color
                val sameAsRunStart = row < board.rows &&
                        board.tileAt(Position(row, col))?.color == startColor &&
                        startColor != null

                if (!sameAsRunStart) {
                    val runLength = row - runStart
                    if (runLength >= 3 && startColor != null) {
                        val positions = (runStart until row).map { r -> Position(r, col) }
                        result += MatchRun(positions, startColor)
                    }
                    runStart = row
                }
            }
        }
        return result
    }
}