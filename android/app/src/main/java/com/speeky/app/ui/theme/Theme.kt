package com.speeky.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF7C6CF2),
    onPrimary = Color(0xFF0B0818),
    secondary = Color(0xFF5E9BFF),
    background = Color(0xFF000000),
    surface = Color(0xFF0A0C12),
    surfaceVariant = Color(0xFF171A24),
    onBackground = Color.White,
    onSurface = Color.White,
    outline = Color(0x22FFFFFF),
)

@Composable
fun SpeekyTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, content = content)
}
