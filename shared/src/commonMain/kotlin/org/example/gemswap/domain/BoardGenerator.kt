package org.example.gemswap.domain

import kotlin.random.Random

class BoardGenerator(
    private val idGenerator: TileIdGenerator,
    private val random: Random = Random.Default
) {

    fun generateBoard(rows: Int, cols: Int): Board {
        val cells: MutableList<MutableList<Tile?>> =
            MutableList(rows) { MutableList(cols) { null } }

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                cells[row][col] = Tile(
                    id = idGenerator.next(),
                    color = pickColorAvoidingMatch(cells, row, col)
                )
            }
        }

        return Board(rows, cols, cells)
    }

    private fun pickColorAvoidingMatch(
        cells: List<List<Tile?>>,
        row: Int,
        col: Int
    ): TileColor {
        val forbidden = mutableSetOf<TileColor>()

        // если слева уже два подряд одного цвета — этот цвет запрещён
        if (col >= 2) {
            val left1 = cells[row][col - 1]?.color
            val left2 = cells[row][col - 2]?.color
            if (left1 != null && left1 == left2) forbidden += left1
        }

        // если сверху уже два подряд одного цвета — этот цвет тоже запрещён
        if (row >= 2) {
            val up1 = cells[row - 1][col]?.color
            val up2 = cells[row - 2][col]?.color
            if (up1 != null && up1 == up2) forbidden += up1
        }

        val allowed = TileColor.entries.filter { it !in forbidden }
        return allowed.random(random)
    }

    fun generatePlayableBoard(rows: Int, cols: Int, swapValidator: SwapValidator): Board {
        var board = generateBoard(rows, cols)
        var attempts = 0
        while (!swapValidator.hasAnyValidMove(board) && attempts < MAX_REGENERATE_ATTEMPTS) {
            board = generateBoard(rows, cols)
            attempts++
        }
        return board
    }

    companion object {
        private const val MAX_REGENERATE_ATTEMPTS = 10
    }
}