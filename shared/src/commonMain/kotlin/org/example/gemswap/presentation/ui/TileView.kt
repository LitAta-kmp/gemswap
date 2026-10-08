package org.example.gemswap.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.gemswap.domain.Tile
import org.example.gemswap.domain.TileColor
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

enum class TileStyle { NORMAL, BATTLE }
@Composable
fun TileView(tile: Tile, style: TileStyle = TileStyle.NORMAL, modifier: Modifier = Modifier) {
    when {
        style == TileStyle.BATTLE && tile.color == TileColor.RED -> {
            Box(modifier = modifier.padding(3.dp), contentAlignment = Alignment.Center) {
                Text(text = "💀", fontSize = 28.sp)
            }
        }
        style == TileStyle.BATTLE && tile.color == TileColor.PURPLE -> {
            Box(modifier = modifier.padding(3.dp), contentAlignment = Alignment.Center) {
                Text(text = "⊕", fontSize = 28.sp, color = tile.color.toComposeColor())
            }
        }
        else -> {
            val color = tile.color.toComposeColor()
            Canvas(modifier = modifier.padding(3.dp)) {
                when (tile.color) {
                    TileColor.RED -> drawCircle(color = color, radius = size.minDimension / 2)
                    TileColor.GREEN -> drawRoundRect(color = color, cornerRadius = CornerRadius(size.width * 0.25f))
                    TileColor.BLUE -> drawDiamond(color)
                    TileColor.YELLOW -> drawTriangle(color)
                    TileColor.PURPLE -> drawStar(color)
                }
            }
        }
    }
}

fun TileColor.toComposeColor(): Color = when (this) {
    TileColor.RED -> Color(0xFFE53935)
    TileColor.GREEN -> Color(0xFF43A047)
    TileColor.BLUE -> Color(0xFF1E88E5)
    TileColor.YELLOW -> Color(0xFFFDD835)
    TileColor.PURPLE -> Color(0xFF8E24AA)
}

private fun DrawScope.drawDiamond(color: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w / 2, 0f)
        lineTo(w, h / 2)
        lineTo(w / 2, h)
        lineTo(0f, h / 2)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawTriangle(color: Color) {
    val w = size.width
    val h = size.height
    val path = Path().apply {
        moveTo(w / 2, 0f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(path, color)
}

private fun DrawScope.drawStar(color: Color, points: Int = 5) {
    val outerRadius = size.minDimension / 2
    val innerRadius = outerRadius * 0.45f
    val centerX = size.width / 2
    val centerY = size.height / 2
    val angleStep = PI / points

    val path = Path()
    for (i in 0 until points * 2) {
        val radius = if (i % 2 == 0) outerRadius else innerRadius
        val angle = i * angleStep - PI / 2
        val x = centerX + (radius * cos(angle)).toFloat()
        val y = centerY + (radius * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color)
}