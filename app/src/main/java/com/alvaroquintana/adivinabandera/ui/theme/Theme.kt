package com.alvaroquintana.adivinabandera.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode { SYSTEM, LIGHT, DARK }

val LocalThemeMode = staticCompositionLocalOf { ThemeMode.SYSTEM }

private val LightColorScheme = lightColorScheme(
    primary                = md_theme_light_primary,
    onPrimary              = md_theme_light_onPrimary,
    primaryContainer       = md_theme_light_primaryContainer,
    onPrimaryContainer     = md_theme_light_onPrimaryContainer,
    secondary              = md_theme_light_secondary,
    onSecondary            = md_theme_light_onSecondary,
    secondaryContainer     = md_theme_light_secondaryContainer,
    onSecondaryContainer   = md_theme_light_onSecondaryContainer,
    tertiary               = md_theme_light_tertiary,
    onTertiary             = md_theme_light_onTertiary,
    tertiaryContainer      = md_theme_light_tertiaryContainer,
    onTertiaryContainer    = md_theme_light_onTertiaryContainer,
    error                  = md_theme_light_error,
    onError                = md_theme_light_onError,
    errorContainer         = md_theme_light_errorContainer,
    onErrorContainer       = md_theme_light_onErrorContainer,
    background             = md_theme_light_background,
    onBackground           = md_theme_light_onBackground,
    surface                = md_theme_light_surface,
    onSurface              = md_theme_light_onSurface,
    surfaceVariant         = md_theme_light_surfaceVariant,
    onSurfaceVariant       = md_theme_light_onSurfaceVariant,
    outline                = md_theme_light_outline,
    outlineVariant         = md_theme_light_outlineVariant,
    inverseSurface         = md_theme_light_inverseSurface,
    inverseOnSurface       = md_theme_light_inverseOnSurface,
    inversePrimary         = md_theme_light_inversePrimary,
    surfaceTint            = md_theme_light_surfaceTint,
    scrim                  = md_theme_light_scrim
)

private val DarkColorScheme = darkColorScheme(
    primary                = md_theme_dark_primary,
    onPrimary              = md_theme_dark_onPrimary,
    primaryContainer       = md_theme_dark_primaryContainer,
    onPrimaryContainer     = md_theme_dark_onPrimaryContainer,
    secondary              = md_theme_dark_secondary,
    onSecondary            = md_theme_dark_onSecondary,
    secondaryContainer     = md_theme_dark_secondaryContainer,
    onSecondaryContainer   = md_theme_dark_onSecondaryContainer,
    tertiary               = md_theme_dark_tertiary,
    onTertiary             = md_theme_dark_onTertiary,
    tertiaryContainer      = md_theme_dark_tertiaryContainer,
    onTertiaryContainer    = md_theme_dark_onTertiaryContainer,
    error                  = md_theme_dark_error,
    onError                = md_theme_dark_onError,
    errorContainer         = md_theme_dark_errorContainer,
    onErrorContainer       = md_theme_dark_onErrorContainer,
    background             = md_theme_dark_background,
    onBackground           = md_theme_dark_onBackground,
    surface                = md_theme_dark_surface,
    onSurface              = md_theme_dark_onSurface,
    surfaceVariant         = md_theme_dark_surfaceVariant,
    onSurfaceVariant       = md_theme_dark_onSurfaceVariant,
    surfaceContainerLowest  = DarkSurfaceContainerLowest,
    surfaceContainerLow     = DarkSurfaceContainerLow,
    surfaceContainer        = DarkSurfaceContainer,
    surfaceContainerHigh    = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline                = md_theme_dark_outline,
    outlineVariant         = md_theme_dark_outlineVariant,
    inverseSurface         = md_theme_dark_inverseSurface,
    inverseOnSurface       = md_theme_dark_inverseOnSurface,
    inversePrimary         = md_theme_dark_inversePrimary,
    surfaceTint            = md_theme_dark_surfaceTint,
    scrim                  = md_theme_dark_scrim
)

@Composable
fun AdivinaBanderaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    windowSizeClass: WindowSizeClass? = null,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
    }

    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val gameColors  = if (darkTheme) DarkGameColors  else LightGameColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    val resolvedWindowSizeClass = windowSizeClass ?: rememberWindowSizeClass()

    CompositionLocalProvider(
        LocalGameColors     provides gameColors,
        LocalThemeMode      provides themeMode,
        LocalWindowSizeClass provides resolvedWindowSizeClass
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = AdivinaTypography,
            shapes      = AdivinaShapes,
            content     = content
        )
    }
}

/**
 * Convenience accessor for game-specific colors.
 * Usage: AdivinaBanderaTheme.gameColors.correctAnswer
 */
object AdivinaBanderaTheme {
    val gameColors: GameColors
        @Composable
        get() = LocalGameColors.current
}
