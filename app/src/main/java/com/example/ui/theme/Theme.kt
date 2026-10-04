package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = NeonSkyBlue,
    onPrimary = GlassTextPrimary,
    secondary = NeonTeal,
    background = GlassDeepBackground,
    onBackground = GlassTextPrimary,
    surface = GlassCardBackground,
    onSurface = GlassTextPrimary,
    surfaceVariant = GlassHeaderBackground,
    onSurfaceVariant = GlassTextSecondary,
    outline = GlassCardBorder,
    tertiary = NeonElectricBlue
)

private val LightColorScheme = lightColorScheme(
    primary = NeonDeepBlue,
    onPrimary = GlassTextPrimary,
    secondary = NeonTeal,
    background = GlassLightBackground,
    onBackground = GlassTextPrimary,
    surface = GlassLightCard,
    onSurface = GlassTextPrimary,
    surfaceVariant = GlassLightBorder,
    onSurfaceVariant = GlassTextSecondary,
    outline = GlassLightBorder,
    tertiary = NeonSkyBlue
)

@Composable
fun HabitFlowTheme(
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



