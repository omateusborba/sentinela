package com.sentinela.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF3B82F6),
    secondary = Color(0xFF22C55E),
    background = Color(0xFF0F1419),
    surface = Color(0xFF1A2332),
    onBackground = Color(0xFFE8EDF4),
    onSurface = Color(0xFFE8EDF4),
    error = Color(0xFFEF4444),
)

@Composable
fun SentinelaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content,
    )
}
