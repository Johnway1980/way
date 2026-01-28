package com.alphadoer.trader.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alphadoer.trader.domain.model.settings.AppearanceSettings

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    onPrimary = Color.Black,
    secondary = Color(0xFF80CBC4),
    background = Color(0xFF0A0E12),
    onBackground = Color(0xFFE3F2FD),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE3F2FD)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0288D1),
    onPrimary = Color.White,
    secondary = Color(0xFF00897B),
    background = Color(0xFFF5F5F7),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827)
)

@Composable
fun AlphaDoerTheme(
    appearanceSettings: AppearanceSettings? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = when (appearanceSettings?.themeMode) {
        AppearanceSettings.ThemeMode.DARK -> true
        AppearanceSettings.ThemeMode.LIGHT -> false
        AppearanceSettings.ThemeMode.AUTO,
        null -> isSystemInDarkTheme()
    }
    
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}

