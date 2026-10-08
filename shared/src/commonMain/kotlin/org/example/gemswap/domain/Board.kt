package org.example.gemswap.domain

data class Position(val row: Int, val col: Int)

data class Board(
    val rows: Int,
    val cols: Int,
    val cells: List<List<Tile?>>
) {
    fun tileAt(position: Position): Tile? =
        cells.getOrNull(position.row)?.getOrNull(position.col)

    fun isInBounds(position: Position): Boolean =
        position.row in 0 until rows && position.col in 0 until cols
}

fun Board.neighborsOf(position: Position): List<Position> {
    val candidates = listOf(
        position.copy(row = position.row - 1),
        position.copy(row = position.row + 1),
        position.copy(col = position.col - 1),
        position.copy(col = position.col + 1)
    )
    return candidates.filter { isInBounds(it) }
}

fun Board.swapped(a: Position, b: Position): Board {
    val newCells = cells.map { it.toMutableList() }
    val tileA = newCells[a.row][a.col]
    val tileB = newCells[b.row][b.col]
    newCells[a.row][a.col] = tileB
    newCells[b.row][b.col] = tileA
    return copy(cells = newCells)
}

fun Board.allAdjacentPositionPairs(): List<Pair<Position, Position>> {
    val pairs = mutableListOf<Pair<Position, Position>>()
    for (row in 0 until rows) {
        for (col in 0 until cols) {
            val position = Position(row, col)
            val right = Position(row, col + 1)
            val down = Position(row + 1, col)
            if (isInBounds(right)) pairs += position to right
            if (isInBounds(down)) pairs += position to down
        }
    }
    return pairs
}

enum class ShakeAxis { HORIZONTAL, VERTICAL }

fun Set<Position>.shakeAxis(): ShakeAxis {
    val positions = toList()
    if (positions.size != 2) return ShakeAxis.HORIZONTAL
    return if (positions[0].row != positions[1].row) ShakeAxis.VERTICAL else ShakeAxis.HORIZONTAL
}