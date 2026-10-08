package org.example.gemswap.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.example.gemswap.domain.GameRules
import org.example.gemswap.domain.TileColor
import org.example.gemswap.presentation.*
import org.example.gemswap.domain.Position

@Composable
fun BattleScreen(
    onHomeClicked: () -> Unit,
    viewModel: BattleViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var shakeTrigger by remember { mutableStateOf(0) }
    var shakePositions by remember { mutableStateOf(emptySet<Position>()) }
    var bonusText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onIntent(BattleIntent.StartBattle)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is BattleSideEffect.InvalidSwap -> {
                    shakePositions = setOf(effect.from, effect.to)
                    shakeTrigger++
                }
                is BattleSideEffect.BonusTurn -> {
                    bonusText = "Bonus turn!"
                    delay(800)
                    bonusText = null
                }
                is BattleSideEffect.AbilityActivated -> {
                    bonusText = if (effect.who == BattleTurn.PLAYER) "You cleared skulls!" else "NPC cleared skulls!"
                    delay(800)
                    bonusText = null
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Turn: ${if (state.currentTurn == BattleTurn.PLAYER) "You" else "NPC"}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = "Time: ${state.turnTimeRemainingSeconds}",
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = bonusText ?: " ",
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (state.currentTurn == BattleTurn.PLAYER) 2.dp else 0.dp,
                            color = Color.Green,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp)
                ) {
                    Text("You: ${state.playerHp}")
                    LinearProgressIndicator(progress = { state.playerHp / GameRules.BATTLE_STARTING_HP.toFloat() })
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = if (state.currentTurn == BattleTurn.NPC) 2.dp else 0.dp,
                            color = Color.Red,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp)
                ) {
                    Text("NPC: ${state.npcHp}")
                    LinearProgressIndicator(progress = { state.npcHp / GameRules.BATTLE_STARTING_HP.toFloat() })
                }
            }

            Row(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- Левая панель: игрок ---
                Row(modifier = Modifier.weight(0.2f), verticalAlignment = Alignment.CenterVertically) {
                    val playerGreenCount = state.playerColorCounts[TileColor.GREEN] ?: 0
                    val canActivate = playerGreenCount >= GameRules.BATTLE_ABILITY_GREEN_THRESHOLD &&
                            state.currentTurn == BattleTurn.PLAYER

                    Button(
                        onClick = { viewModel.onIntent(BattleIntent.ActivateSkullClearAbility) },
                        enabled = canActivate,
                        modifier = Modifier.size(48.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("💀✕", fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Column(horizontalAlignment = Alignment.End) {
                        TileColor.entries.filter { it != TileColor.RED }.forEach { color ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color.toComposeColor()))
                                Text(
                                    text = "${state.playerColorCounts[color] ?: 0}",
                                    modifier = Modifier.padding(start = 4.dp).width(24.dp),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }
                }

                // --- Поле ---
                Box(
                    modifier = Modifier.weight(0.6f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoardView(
                        board = state.board,
                        shakeTrigger = shakeTrigger,
                        shakePositions = shakePositions,
                        onSwapRequested = { from, to ->
                            viewModel.onIntent(BattleIntent.PlayerSwapRequested(from, to))
                        },
                        tileStyle = TileStyle.BATTLE,
                        modifier = Modifier.fillMaxHeight()
                    )
                }

                // --- Правая панель: NPC ---
                Row(modifier = Modifier.weight(0.2f), verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.Start) {
                        TileColor.entries.filter { it != TileColor.RED }.forEach { color ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color.toComposeColor()))
                                Text(
                                    text = "${state.npcColorCounts[color] ?: 0}",
                                    modifier = Modifier.padding(start = 4.dp).width(24.dp),
                                    textAlign = TextAlign.Start
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    val npcGreenCount = state.npcColorCounts[TileColor.GREEN] ?: 0
                    val npcReady = npcGreenCount >= GameRules.BATTLE_ABILITY_GREEN_THRESHOLD

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (npcReady) Color(0xFF8E24AA) else Color.Gray.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💀✕", fontSize = 14.sp)
                    }
                }
            }
        }

        if (state.status == BattleStatus.PlayerWon || state.status == BattleStatus.NpcWon) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.status == BattleStatus.PlayerWon) "You Win!" else "You Lose!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Button(
                        onClick = { viewModel.onIntent(BattleIntent.StartBattle) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Играть снова")
                    }
                    Button(
                        onClick = onHomeClicked,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("На главный")
                    }
                }
            }
        }
    }
}