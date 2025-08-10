package com.enchantreader.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB69DF8),
    secondary = Color(0xFF89D1FF),
    tertiary = Color(0xFF80EEC0)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF6D5BD0),
    secondary = Color(0xFF1FA2FF),
    tertiary = Color(0xFF22E1A1)
)

@Composable
fun EnchantReaderTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
