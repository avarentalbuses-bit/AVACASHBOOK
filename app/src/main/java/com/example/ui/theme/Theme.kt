package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF00390E),
    primaryContainer = AvaGreenDark,
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = AvaGoldAccent,
    onSecondary = Color(0xFF3E2723),
    secondaryContainer = Color(0xFF5D4037),
    tertiary = Color(0xFF80CBC4),
    background = Color(0xFF111512),
    surface = Color(0xFF181E19),
    surfaceVariant = Color(0xFF222B24),
    onBackground = Color(0xFFE2E7E3),
    onSurface = Color(0xFFE2E7E3),
    outline = Color(0xFF435247)
)

private val LightColorScheme = lightColorScheme(
    primary = AvaGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = AvaGreenLight,
    onPrimaryContainer = AvaGreenDark,
    secondary = AvaGoldDark,
    onSecondary = Color.White,
    secondaryContainer = AvaGoldLight,
    onSecondaryContainer = Color(0xFF4E3600),
    tertiary = AvaGreenSecondary,
    background = AvaBackground,
    surface = AvaSurface,
    surfaceVariant = Color(0xFFEEF3EF),
    onBackground = AvaTextPrimary,
    onSurface = AvaTextPrimary,
    onSurfaceVariant = AvaTextSecondary,
    outline = AvaBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

