package com.speeky.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily

enum class SpeekyPalette {
    DARK,
    VIOLET,
    OCEAN,
    SAKURA
}

private val DarkPalette = darkColorScheme(
    primary = Color(0xFF8C7CFF),
    secondary = Color(0xFF3B4152),
    tertiary = Color(0xFFFFD56A),
    background = Color(0xFF070A14),
    surface = Color(0xFF101420),
    surfaceVariant = Color(0xFF151B2A),
    onPrimary = Color(0xFF090B13),
    onBackground = Color(0xFFF4F6FF),
    onSurface = Color(0xFFF4F6FF),
    onSurfaceVariant = Color(0xFFB2B7C7)
)

private val VioletPalette = darkColorScheme(
    primary = Color(0xFF7C69FF),
    secondary = Color(0xFF313758),
    tertiary = Color(0xFFFDE68A),
    background = Color(0xFF060915),
    surface = Color(0xFF0F1323),
    surfaceVariant = Color(0xFF171D31),
    onPrimary = Color(0xFF090B13),
    onBackground = Color(0xFFF5F5FF),
    onSurface = Color(0xFFF5F5FF),
    onSurfaceVariant = Color(0xFFBDC2D9)
)

private val OceanPalette = darkColorScheme(
    primary = Color(0xFF4FB8FF),
    secondary = Color(0xFF1D4257),
    tertiary = Color(0xFF8AF8FF),
    background = Color(0xFF05111A),
    surface = Color(0xFF0A1B26),
    surfaceVariant = Color(0xFF132A38),
    onPrimary = Color(0xFF04131C),
    onBackground = Color(0xFFEAFBFF),
    onSurface = Color(0xFFEAFBFF),
    onSurfaceVariant = Color(0xFFAFD8E6)
)

private val SakuraPalette = darkColorScheme(
    primary = Color(0xFFFF7FB6),
    secondary = Color(0xFF51324A),
    tertiary = Color(0xFFFFD3E8),
    background = Color(0xFF140812),
    surface = Color(0xFF23111F),
    surfaceVariant = Color(0xFF311A2A),
    onPrimary = Color(0xFF180811),
    onBackground = Color(0xFFFFF2F8),
    onSurface = Color(0xFFFFF2F8),
    onSurfaceVariant = Color(0xFFF0BED5)
)

private val SpeekyTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif),
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif)
)

@Composable
fun SpeekyTheme(
    palette: SpeekyPalette,
    content: @Composable () -> Unit
) {
    val colors = when (palette) {
        SpeekyPalette.DARK -> DarkPalette
        SpeekyPalette.VIOLET -> VioletPalette
        SpeekyPalette.OCEAN -> OceanPalette
        SpeekyPalette.SAKURA -> SakuraPalette
    }

    MaterialTheme(
        colorScheme = colors,
        typography = SpeekyTypography,
        content = content
    )
}
