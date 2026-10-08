package org.example.gemswap

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.example.gemswap.presentation.navigation.AppNavHost

@Composable
fun App() {
    MaterialTheme {
        AppNavHost()
    }
}