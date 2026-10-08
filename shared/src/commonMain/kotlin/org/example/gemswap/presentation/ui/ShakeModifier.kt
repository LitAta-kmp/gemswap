package org.example.gemswap.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun rememberShakeOffset(trigger: Int): Dp {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger == 0) return@LaunchedEffect
        launch {
            val shakeDistance = 12f
            offsetX.animateTo(shakeDistance, tween(50))
            offsetX.animateTo(-shakeDistance, tween(50))
            offsetX.animateTo(shakeDistance, tween(50))
            offsetX.animateTo(0f, tween(50))
        }
    }

    return offsetX.value.dp
}