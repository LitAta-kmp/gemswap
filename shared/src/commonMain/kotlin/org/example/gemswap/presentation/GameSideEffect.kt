package org.example.gemswap.presentation

import org.example.gemswap.domain.Position

sealed interface GameSideEffect {
    data class InvalidSwap(val from: Position, val to: Position) : GameSideEffect
    data class ComboTriggered(val waveIndex: Int) : GameSideEffect
}