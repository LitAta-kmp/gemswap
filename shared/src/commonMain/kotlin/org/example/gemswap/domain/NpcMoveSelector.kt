package org.example.gemswap.domain

class NpcMoveSelector(
    private val swapValidator: SwapValidator,
    private val matchFinder: MatchFinder
) {

    fun selectBestMove(board: Board, npcHp: Int): Pair<Position, Position>? {
        val candidates = board.allAdjacentPositionPairs()
            .filter { (from, to) -> swapValidator.isValidSwap(board, from, to) }

        if (candidates.isEmpty()) return null

        data class ScoredMove(val move: Pair<Position, Position>, val redCount: Int, val healCount: Int)

        val scored = candidates.map { move ->
            val runs = matchFinder.findMatchRuns(board.swapped(move.first, move.second))
            ScoredMove(
                move = move,
                redCount = runs.filter { it.color == TileColor.RED }.sumOf { it.positions.size },
                healCount = runs.filter { it.color == TileColor.PURPLE }.sumOf { it.positions.size }
            )
        }

        val hasAnyRedOption = scored.any { it.redCount > 0 }
        val npcNeedsHeal = npcHp < GameRules.BATTLE_STARTING_HP

        return when {
            hasAnyRedOption -> scored.maxByOrNull { it.redCount }!!.move
            npcNeedsHeal -> scored.maxByOrNull { it.healCount }!!.move
            else -> scored.maxByOrNull { it.redCount + it.healCount }!!.move
        }
    }
}