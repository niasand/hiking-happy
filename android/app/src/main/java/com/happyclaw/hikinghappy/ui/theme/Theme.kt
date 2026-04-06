package com.happyclaw.hikinghappy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = HHColors.AccentAltitude,
    onPrimary = HHColors.TextInverse,
    primaryContainer = HHColors.AccentAltitudeDim,
    onPrimaryContainer = Color(0xFF1A5C30),

    secondary = HHColors.AccentSpeed,
    onSecondary = HHColors.TextInverse,
    secondaryContainer = HHColors.AccentSpeedDim,
    onSecondaryContainer = Color(0xFF1A3C52),

    tertiary = HHColors.Warning,
    onTertiary = HHColors.TextInverse,

    error = HHColors.Error,
    onError = HHColors.TextInverse,

    background = HHColors.Background,
    onBackground = HHColors.TextPrimary,

    surface = HHColors.Surface,
    onSurface = HHColors.TextPrimary,
    onSurfaceVariant = HHColors.TextSecondary,

    outline = HHColors.BorderStandard,
    outlineVariant = HHColors.BorderSubtle,

    surfaceVariant = HHColors.SurfaceElevated,
    surfaceContainer = HHColors.SurfaceElevated,
    surfaceContainerLow = HHColors.Background,
)

@Composable
fun HikingHappyTheme(content: @Composable () -> Unit) {
    // Force light theme -- outdoor readability per DESIGN.md
    val colorScheme = LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = HHColors.Surface.toArgb()
            window.navigationBarColor = HHColors.Surface.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = true
            insetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HHTypography,
        content = content
    )
}
