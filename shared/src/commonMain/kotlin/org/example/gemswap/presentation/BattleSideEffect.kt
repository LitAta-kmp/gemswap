package org.example.gemswap.presentation

import org.example.gemswap.domain.Position

sealed interface BattleSideEffect {
    data class InvalidSwap(val from: Position, val to: Position) : BattleSideEffect
    data class BonusTurn(val who: BattleTurn) : BattleSideEffect
    data class AbilityActivated(val who: BattleTurn) : BattleSideEffect
}
