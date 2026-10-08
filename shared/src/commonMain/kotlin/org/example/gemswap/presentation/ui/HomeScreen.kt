package org.example.gemswap.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onPlayClicked: () -> Unit,
    onHistoryClicked: () -> Unit,
    onBattleClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("GemSwap", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onPlayClicked, modifier = Modifier.padding(top = 24.dp)) {
            Text("Играть")
        }
        Button(onClick = onBattleClicked, modifier = Modifier.padding(top = 8.dp)) {
            Text("Бой с NPC")
        }
        Button(onClick = onHistoryClicked, modifier = Modifier.padding(top = 8.dp)) {
            Text("История")
        }
    }
}