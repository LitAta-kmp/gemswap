package org.example.gemswap.presentation.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.example.gemswap.domain.Board
import org.example.gemswap.domain.Position
import org.example.gemswap.domain.ShakeAxis
import org.example.gemswap.domain.shakeAxis

@Composable
fun GameBoardView(
    board: Board,
    shakeTrigger: Int,
    shakePositions: Set<Position>,
    onSwapRequested: (Position, Position) -> Unit,
    tileStyle: TileStyle = TileStyle.NORMAL,
    modifier: Modifier = Modifier
) {
    val boardAspectRatio = if (board.rows > 0 && board.cols > 0) {
        board.cols.toFloat() / board.rows.toFloat()
    } else {
        1f  // доска ещё не сгенерирована (самый первый кадр до StartGame) — просто квадрат-заглушка
    }

    BoxWithConstraints(
        modifier = modifier.aspectRatio(boardAspectRatio, matchHeightConstraintsFirst = true)
    ) {
        val tileSizeDp = maxHeight / board.rows   // было: maxWidth / board.cols
        val density = LocalDensity.current
        val tileSizePx = with(density) { tileSizeDp.toPx() }
        val boardShakeOffset = rememberShakeOffset(shakeTrigger)
        val shakeAxis = shakePositions.shakeAxis()

        Box(Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridColor = Color.Gray.copy(alpha = 0.4f)
                val strokeWidth = 1.dp.toPx()

                for (col in 0..board.cols) {
                    val x = tileSizePx * col
                    drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth)
                }
                for (row in 0..board.rows) {
                    val y = tileSizePx * row
                    drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth)
                }
            }

            for (row in 0 until board.rows) {
                for (col in 0 until board.cols) {
                    val tile = board.cells[row][col] ?: continue
                    key(tile.id) {
                        var dragOffset by remember { mutableStateOf(Offset.Zero) }

                        val isShaking = Position(row, col) in shakePositions
                        val appliedShake = if (isShaking) boardShakeOffset else 0.dp

                        val shakeX = if (isShaking && shakeAxis == ShakeAxis.HORIZONTAL) boardShakeOffset else 0.dp
                        val shakeY = if (isShaking && shakeAxis == ShakeAxis.VERTICAL) boardShakeOffset else 0.dp

                        val targetX = tileSizeDp * col + shakeX
                        val targetY = tileSizeDp * row + shakeY

                        val animatedX by animateDpAsState(targetValue = targetX, animationSpec = tween(200))
                        val animatedY by animateDpAsState(targetValue = targetY, animationSpec = tween(200))

                        TileView(
                            tile = tile,
                            style = tileStyle,
                            modifier = Modifier
                                .size(tileSizeDp)
                                .offset(x = animatedX, y = animatedY)
                                .pointerInput(tile.id) {
                                    detectDragGestures(
                                        onDragStart = { dragOffset = Offset.Zero },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount
                                        },
                                        onDragEnd = {
                                            resolveSwapTarget(row, col, dragOffset, tileSizePx, board)
                                                ?.let { target -> onSwapRequested(Position(row, col), target) }
                                            dragOffset = Offset.Zero
                                        }
                                    )
                                }
                        )
                    }
                }
            }
        }
    }
}

private fun resolveSwapTarget(
    row: Int,
    col: Int,
    dragOffset: Offset,
    tileSizePx: Float,
    board: Board
): Position? {
    val threshold = tileSizePx / 3
    val absX = kotlin.math.abs(dragOffset.x)
    val absY = kotlin.math.abs(dragOffset.y)

    if (absX < threshold && absY < threshold) return null

    val target = if (absX > absY) {
        Position(row, col + if (dragOffset.x > 0) 1 else -1)
    } else {
        Position(row + if (dragOffset.y > 0) 1 else -1, col)
    }

    return target.takeIf { board.isInBounds(it) }
}