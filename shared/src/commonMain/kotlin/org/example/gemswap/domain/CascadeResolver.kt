package org.example.gemswap.domain

import kotlin.random.Random

class CascadeResolver(
    private val idGenerator: TileIdGenerator,
    private val random: Random = Random.Default
) {
    fun removeMatches(board: Board, matched: Set<Position>): Board {
        val newCells = board.cells.map { it.toMutableList() }
        for (position in matched) {
            newCells[position.row][position.col] = null
        }
        return board.copy(cells = newCells)
    }

    fun applyGravity(board: Board): Board {
        val newCells = MutableList(board.rows) { MutableList<Tile?>(board.cols) { null } }

        for (col in 0 until board.cols) {
            val fallingTiles = mutableListOf<Tile>()
            for (row in 0 until board.rows) {
                board.cells[row][col]?.let { fallingTiles += it }
            }
            val startRow = board.rows - fallingTiles.size
            for (i in fallingTiles.indices) {
                newCells[startRow + i][col] = fallingTiles[i]
            }
        }

        return board.copy(cells = newCells)
    }

    fun refill(board: Board): Board {
        val newCells = board.cells.map { it.toMutableList() }
        for (col in 0 until board.cols) {
            for (row in 0 until board.rows) {
                if (newCells[row][col] == null) {
                    newCells[row][col] = Tile(id = idGenerator.next(), color = TileColor.entries.random(random))
                }
            }
        }
        return board.copy(cells = newCells)
    }

    fun resolveCascade(board: Board, matched: Set<Position>): Board {
        val cleared = removeMatches(board, matched)
        val fallen = applyGravity(cleared)
        return refill(fallen)
    }
}