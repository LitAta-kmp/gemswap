package org.example.gemswap.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.example.gemswap.domain.GameRound
import org.example.gemswap.presentation.HistoryViewModel

@Composable
fun HistoryScreen(
    onBackClicked: () -> Unit,
    viewModel: HistoryViewModel = koinViewModel()
) {
    val rounds by viewModel.rounds.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История") },
                navigationIcon = {
                    TextButton(onClick = onBackClicked) { Text("←") }
                }
            )
        }
    ) { padding ->
        if (rounds.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Пока нет сыгранных раундов")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(rounds) { round -> RoundRow(round) }
            }
        }
    }
}

@Composable
private fun RoundRow(round: GameRound) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(round.playedAt)
        Text("Счёт: ${round.score}")
    }
}