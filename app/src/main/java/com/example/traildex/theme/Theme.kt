package com.example.traildex.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val RetroColorScheme = lightColorScheme(
    primary = SagePrimary,
    onPrimary = SurfaceWhite,
    primaryContainer = SagePrimaryContainer,
    onPrimaryContainer = CharcoalText,
    secondary = SoftTeal,
    onSecondary = SurfaceWhite,
    secondaryContainer = SoftTealLight,
    onSecondaryContainer = CharcoalText,
    tertiary = WarmBerry,
    onTertiary = SurfaceWhite,
    tertiaryContainer = WarmBerryLight,
    onTertiaryContainer = CharcoalText,
    background = SurfaceCream,
    onBackground = CharcoalText,
    surface = SurfaceCream,
    onSurface = CharcoalText,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = CharcoalMuted,
    outline = CharcoalOutline
)

@Composable
fun TrailDexTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = RetroColorScheme,
        typography = Typography,
        content = content
    )
}
