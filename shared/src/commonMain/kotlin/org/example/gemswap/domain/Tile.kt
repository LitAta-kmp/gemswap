package org.example.gemswap.domain

enum class TileColor {
    RED, GREEN, BLUE, YELLOW, PURPLE
}

data class Tile(
    val id: Long,
    val color: TileColor
)