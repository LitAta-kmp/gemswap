package org.example.gemswap.presentation

import org.example.gemswap.domain.Position

sealed interface GameIntent {
    data object StartGame : GameIntent
    data class SwapRequested(val from: Position, val to: Position) : GameIntent
    data object TimeTicked : GameIntent
}