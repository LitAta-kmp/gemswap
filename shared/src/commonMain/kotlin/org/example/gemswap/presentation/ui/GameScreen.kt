package org.example.gemswap.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.example.gemswap.domain.Position
import org.example.gemswap.domain.TileColor
import org.koin.compose.viewmodel.koinViewModel
import org.example.gemswap.presentation.GameIntent
import org.example.gemswap.presentation.GameSideEffect
import org.example.gemswap.presentation.GameStatus
import org.example.gemswap.presentation.GameViewModel

@Composable
fun GameScreen(
    onGameFinished: (Int) -> Unit,
    viewModel: GameViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var shakeTrigger by remember { mutableStateOf(0) }
    var shakePositions by remember { mutableStateOf(emptySet<Position>()) }
    var comboText by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.onIntent(GameIntent.StartGame)
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffects.collect { effect ->
            when (effect) {
                is GameSideEffect.InvalidSwap -> {
                    shakePositions = setOf(effect.from, effect.to)
                    shakeTrigger++
                }
                is GameSideEffect.ComboTriggered -> {
                    comboText = "Combo x${effect.waveIndex}!"
                    delay(800)
                    comboText = null
                }
            }
        }
    }

    LaunchedEffect(state.status) {
        if (state.status == GameStatus.Finished) {
            onGameFinished(state.score)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Score: ${state.score}",
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Text(
                text = "Time: ${state.timeRemainingSeconds}",
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.2f)) {
                Text(
                    text = comboText ?: " ",
                    modifier = Modifier.alpha(if (comboText != null) 1f else 0f)
                )
                TileColor.entries.forEach { color ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color.toComposeColor()))
                        Text(text = " ${state.colorCounts[color] ?: 0}", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }

            Box(modifier = Modifier.weight(0.6f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                GameBoardView(
                    board = state.board,
                    shakeTrigger = shakeTrigger,
                    shakePositions = shakePositions,
                    onSwapRequested = { from, to -> viewModel.onIntent(GameIntent.SwapRequested(from, to)) },
                    modifier = Modifier.fillMaxHeight()
                )
            }

            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}