package com.weighttracker.app.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.PrimaryContainer,
    onPrimaryContainer = AppColors.OnPrimaryContainer,
    secondary = AppColors.Secondary,
    onSecondary = AppColors.OnSecondary,
    secondaryContainer = AppColors.SecondaryContainer,
    onSecondaryContainer = AppColors.OnSecondaryContainer,
    tertiary = AppColors.Tertiary,
    tertiaryContainer = AppColors.TertiaryContainer,
    background = AppColors.Background,
    onBackground = AppColors.OnBackground,
    surface = AppColors.Surface,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurface = AppColors.OnSurface,
    onSurfaceVariant = AppColors.OnSurfaceVariant,
    outline = AppColors.Outline,
    outlineVariant = AppColors.OutlineVariant,
    error = AppColors.Error,
    onError = AppColors.OnError,
    errorContainer = AppColors.ErrorContainer,
    surfaceContainerLowest = AppColors.SurfaceContainerLowest,
    surfaceContainerLow = AppColors.SurfaceContainerLow,
    surfaceContainer = AppColors.SurfaceContainer,
    surfaceContainerHigh = AppColors.SurfaceContainerHigh,
    surfaceContainerHighest = AppColors.SurfaceContainerHighest
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkAppColors.Primary,
    onPrimary = DarkAppColors.OnPrimary,
    primaryContainer = DarkAppColors.PrimaryContainer,
    onPrimaryContainer = DarkAppColors.OnPrimaryContainer,
    secondary = DarkAppColors.Primary,
    background = DarkAppColors.Background,
    onBackground = DarkAppColors.OnBackground,
    surface = DarkAppColors.Surface,
    surfaceVariant = DarkAppColors.SurfaceVariant,
    onSurface = DarkAppColors.OnSurface,
    onSurfaceVariant = DarkAppColors.OnSurfaceVariant,
    surfaceContainerLowest = DarkAppColors.SurfaceContainerLowest,
    surfaceContainerLow = DarkAppColors.SurfaceContainerLow,
    surfaceContainer = DarkAppColors.SurfaceContainer,
    surfaceContainerHigh = DarkAppColors.SurfaceContainerHigh,
    surfaceContainerHighest = DarkAppColors.SurfaceContainerHighest
)

@Composable
fun WeightTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
