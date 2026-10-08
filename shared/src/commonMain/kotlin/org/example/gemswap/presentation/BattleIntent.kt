package org.example.gemswap.presentation

import org.example.gemswap.domain.Position

sealed interface BattleIntent {
    data object StartBattle : BattleIntent
    data class PlayerSwapRequested(val from: Position, val to: Position) : BattleIntent
    data object TurnTimeTicked : BattleIntent
    data object ActivateSkullClearAbility : BattleIntent
}