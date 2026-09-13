package com.applock.protectedapps.ui.lockscreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

@Composable
fun PatternView(
    onPatternComplete: (String) -> Unit,
    hidePath: Boolean = false,
    pathColor: Color = Color(0xFF2196F3),
    dotColor: Color = Color.Gray,
    selectedDotColor: Color = Color(0xFF2196F3),
    modifier: Modifier = Modifier
) {
    var selectedDots by remember { mutableStateOf(listOf<Int>()) }
    var currentTouchPosition by remember { mutableStateOf<Offset?>(null) }
    var dotCenters by remember { mutableStateOf(mapOf<Int, Offset>()) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selectedDots = emptyList()
                        currentTouchPosition = offset
                        val touched = findDotIndex(offset, dotCenters, radius = 60f)
                        if (touched != null) {
                            selectedDots = listOf(touched)
                        }
                    },
                    onDrag = { change, _ ->
                        currentTouchPosition = change.position
                        val touched = findDotIndex(change.position, dotCenters, radius = 60f)
                        if (touched != null && !selectedDots.contains(touched)) {
                            selectedDots = selectedDots + touched
                        }
                    },
                    onDragEnd = {
                        if (selectedDots.size >= 4) {
                            val patternString = selectedDots.joinToString("")
                            onPatternComplete(patternString)
                        }
                        selectedDots = emptyList()
                        currentTouchPosition = null
                    },
                    onDragCancel = {
                        selectedDots = emptyList()
                        currentTouchPosition = null
                    }
                )
            }
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / 4f
        val stepY = height / 4f

        val centers = mutableMapOf<Int, Offset>()
        var index = 0
        for (row in 1..3) {
            for (col in 1..3) {
                val center = Offset(col * stepX, row * stepY)
                centers[index] = center
                index++
            }
        }
        dotCenters = centers

        // Draw Pattern Lines
        if (!hidePath && selectedDots.size > 1) {
            for (i in 0 until selectedDots.size - 1) {
                val start = dotCenters[selectedDots[i]]
                val end = dotCenters[selectedDots[i + 1]]
                if (start != null && end != null) {
                    drawLine(
                        color = pathColor,
                        start = start,
                        end = end,
                        strokeWidth = 12f,
                        cap = StrokeCap.Round
                    )
                }
            }
            // Line to current touch position
            val lastDot = selectedDots.lastOrNull()
            if (lastDot != null && currentTouchPosition != null) {
                val start = dotCenters[lastDot]
                if (start != null) {
                    drawLine(
                        color = pathColor,
                        start = start,
                        end = currentTouchPosition!!,
                        strokeWidth = 8f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Draw Dots
        for (i in 0..8) {
            val center = dotCenters[i] ?: continue
            val isSelected = selectedDots.contains(i)

            // Outer ring if selected
            if (isSelected && !hidePath) {
                drawCircle(
                    color = selectedDotColor.copy(alpha = 0.3f),
                    radius = 48f,
                    center = center
                )
            }

            drawCircle(
                color = if (isSelected && !hidePath) selectedDotColor else dotColor,
                radius = 18f,
                center = center
            )
        }
    }
}

private fun findDotIndex(position: Offset, centers: Map<Int, Offset>, radius: Float): Int? {
    for ((index, center) in centers) {
        val dist = hypot(position.x - center.x, position.y - center.y)
        if (dist <= radius) {
            return index
        }
    }
    return null
}
