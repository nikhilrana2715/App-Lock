package com.applock.protectedapps.ui.lockscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinPadView(
    inputBuffer: String,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onOkClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // PIN Dots Display
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            repeat(4) { index ->
                val isFilled = index < inputBuffer.length
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                )
            }
        }

        // Numeric Keypad Grid (3x4) with DEL button to the left of 0 and OK button to the right of 0
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("DEL", "0", "OK")
        )

        keys.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                row.forEach { key ->
                    KeypadButton(
                        key = key,
                        onDigitClick = onDigitClick,
                        onDeleteClick = onDeleteClick,
                        onOkClick = onOkClick
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    onDigitClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onOkClick: (() -> Unit)? = null
) {
    val isEnabled = when (key) {
        "OK" -> onOkClick != null
        "DEL" -> true
        "" -> false
        else -> true
    }

    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(
                if (key == "OK") MaterialTheme.colorScheme.primaryContainer
                else if (key.isNotEmpty()) MaterialTheme.colorScheme.surfaceVariant
                else Color.Transparent
            )
            .clickable(enabled = isEnabled) {
                when (key) {
                    "OK" -> onOkClick?.invoke()
                    "DEL" -> onDeleteClick()
                    else -> if (key.isNotEmpty()) onDigitClick(key)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        when (key) {
            "OK" -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "OK Submit",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }
            "DEL" -> {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            else -> {
                if (key.isNotEmpty()) {
                    Text(
                        text = key,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
