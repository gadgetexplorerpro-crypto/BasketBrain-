package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BasketBrainLightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = SageGreenLight,
    onPrimaryContainer = ForestGreenDark,
    secondary = SageGreenSecondary,
    onSecondary = TextPrimaryLight,
    secondaryContainer = SageGreenLight,
    onSecondaryContainer = ForestGreenDark,
    tertiary = CitrusOrangeAccent,
    onTertiary = TextPrimaryLight,
    tertiaryContainer = CitrusOrangeSoft,
    onTertiaryContainer = Color(0xFF4E3000),
    background = WarmOffWhiteBg,
    onBackground = TextPrimaryLight,
    surface = WarmSurfaceCard,
    onSurface = TextPrimaryLight,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    error = BudgetDangerRed,
    onError = Color.White
)

private val BasketBrainDarkColorScheme = darkColorScheme(
    primary = ForestGreenPrimaryDarkTheme,
    onPrimary = CharcoalGreenBg,
    primaryContainer = ForestGreenPrimary,
    onPrimaryContainer = Color.White,
    secondary = SageGreenSecondaryDarkTheme,
    onSecondary = CharcoalGreenBg,
    secondaryContainer = CharcoalSurfaceVariant,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = CitrusOrangeAccent,
    onTertiary = CharcoalGreenBg,
    tertiaryContainer = Color(0xFF4D3409),
    onTertiaryContainer = CitrusOrangeSoft,
    background = CharcoalGreenBg,
    onBackground = TextPrimaryDark,
    surface = CharcoalSurfaceCard,
    onSurface = TextPrimaryDark,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextSecondaryDark,
    error = Color(0xFFEF5350),
    onError = CharcoalGreenBg
)

val BasketBrainShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BasketBrainTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) BasketBrainDarkColorScheme else BasketBrainLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = BasketBrainShapes,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BasketBrainTheme(darkTheme = darkTheme, content = content)
}
