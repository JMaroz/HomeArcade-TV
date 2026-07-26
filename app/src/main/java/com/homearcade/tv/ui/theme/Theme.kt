package com.homearcade.tv.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.MaterialTheme

private val DarkColors = darkColorScheme(
    primary = Pink,
    onPrimary = White,
    primaryContainer = PinkDark,
    secondary = Pink.copy(alpha = 0.7f),
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = SurfaceVariant,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = White.copy(alpha = 0.6f),
    error = Red,
    onError = White,
    outline = White.copy(alpha = 0.12f)
)

@Composable
fun HomeArcadeTVTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = HomeArcadeTypography,
        content = content
    )
}
