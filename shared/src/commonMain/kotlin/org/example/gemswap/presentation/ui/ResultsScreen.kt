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
fun ResultsScreen(
    score: Int,
    onPlayAgainClicked: () -> Unit,
    onHomeClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Игра окончена", style = MaterialTheme.typography.headlineMedium)
        Text("Счёт: $score", modifier = Modifier.padding(top = 16.dp))
        Button(onClick = onPlayAgainClicked, modifier = Modifier.padding(top = 24.dp)) {
            Text("Играть снова")
        }
        Button(onClick = onHomeClicked, modifier = Modifier.padding(top = 8.dp)) {
            Text("На главный")
        }
    }
}