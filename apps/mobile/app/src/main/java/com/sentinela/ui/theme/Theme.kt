package com.sentinela.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = SentinelaColors.Navy,
    onPrimary = Color.White,
    primaryContainer = SentinelaColors.NavyDeep,
    onPrimaryContainer = SentinelaColors.OnDark,
    secondary = SentinelaColors.FlameOrange,
    onSecondary = Color.White,
    secondaryContainer = SentinelaColors.FlameAmber,
    onSecondaryContainer = SentinelaColors.Navy,
    tertiary = SentinelaColors.FlameRed,
    onTertiary = Color.White,
    background = SentinelaColors.Background,
    onBackground = SentinelaColors.Text,
    surface = SentinelaColors.Surface,
    onSurface = SentinelaColors.Text,
    surfaceVariant = Color(0xFFEEF0F4),
    onSurfaceVariant = SentinelaColors.TextMuted,
    error = SentinelaColors.FlameRed,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = SentinelaColors.OnDark,
    onPrimary = SentinelaColors.NavyDeep,
    primaryContainer = SentinelaColors.Navy,
    onPrimaryContainer = SentinelaColors.OnDark,
    secondary = SentinelaColors.FlameOrange,
    onSecondary = Color.White,
    secondaryContainer = SentinelaColors.FlameRed,
    onSecondaryContainer = Color.White,
    tertiary = SentinelaColors.FlameAmber,
    onTertiary = SentinelaColors.NavyDeep,
    background = SentinelaColors.NavyDeep,
    onBackground = SentinelaColors.OnDark,
    surface = SentinelaColors.Navy,
    onSurface = SentinelaColors.OnDark,
    surfaceVariant = Color(0xFF24304A),
    onSurfaceVariant = Color(0xFFB8C0D0),
    error = SentinelaColors.FlameRed,
    onError = Color.White,
)

@Composable
fun SentinelaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
