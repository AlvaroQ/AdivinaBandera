package com.alvaroquintana.adivinabandera.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Replica la logica de AdivinaBanderaTheme para saber si estamos en modo oscuro.
 * Usa LocalThemeMode (preferencia de la app) en lugar de isSystemInDarkTheme() directamente,
 * de modo que la eleccion de tema de la app siempre se respeta.
 */
@Composable
@ReadOnlyComposable
fun isAppInDarkTheme(): Boolean {
    val themeMode = LocalThemeMode.current
    return when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT  -> false
        ThemeMode.DARK   -> true
    }
}

// ── Light theme gradients ────────────────────────────────────────────────────
object LightGradients {
    val backgroundGradient: Brush
        @Composable
        get() = Brush.verticalGradient(
            colors = listOf(
                GameWhite,
                GameWhite
            )
        )

    val cardGradient: Brush
        @Composable
        get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFFFFF),
                Color(0xFFF5F7FF)
            )
        )

    val surfaceGradient: Brush
        @Composable
        get() = Brush.verticalGradient(
            colors = listOf(
                Color(0xFFE8EDF5),
                Color(0xFFDDE3EE)
            )
        )

    val primaryGradient: Brush
        @Composable
        get() = Brush.linearGradient(
            colors = listOf(
                GameBlue,
                Color(0xFF283593)
            )
        )

    val heroGradient: Brush
        @Composable
        get() = Brush.verticalGradient(
            colors = listOf(
                GameCream,
                Color(0xFFE3E8F8)
            )
        )
}

// ── Dark theme gradients ─────────────────────────────────────────────────────
// De azul-negro profundo (top) a marino oscuro con calor (bottom)
object DarkGradients {
    val backgroundGradient: Brush
        @Composable
        get() = Brush.verticalGradient(
            colors = listOf(
                DarkSurface,
                DarkSurface
            )
        )

    val cardGradient: Brush
        @Composable
        get() = Brush.linearGradient(
            colors = listOf(
                DarkSurface,             // #0E1628
                Color(0xFF0A1020)        // borde inferior mas oscuro
            )
        )

    val surfaceGradient: Brush
        @Composable
        get() = Brush.verticalGradient(
            colors = listOf(
                DarkSurfaceVar,          // #162035
                Color(0xFF111A2C)        // mas profundo abajo
            )
        )

    val primaryGradient: Brush
        @Composable
        get() = Brush.linearGradient(
            colors = listOf(
                Color(0xFFADB8F0),
                Color(0xFF8A9FE8)
            )
        )

    val heroGradient: Brush
        @Composable
        get() = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF060A14),       // negro azulado — parte superior
                Color(0xFF101C30)        // marino oscuro — sugiere profundidad abajo
            )
        )
}

@Composable
fun getBackgroundGradient(): Brush =
    if (isAppInDarkTheme()) DarkGradients.backgroundGradient else LightGradients.backgroundGradient

@Composable
fun getCardGradient(): Brush =
    if (isAppInDarkTheme()) DarkGradients.cardGradient else LightGradients.cardGradient

@Composable
fun getSurfaceGradient(): Brush =
    if (isAppInDarkTheme()) DarkGradients.surfaceGradient else LightGradients.surfaceGradient

@Composable
fun getPrimaryGradient(): Brush =
    if (isAppInDarkTheme()) DarkGradients.primaryGradient else LightGradients.primaryGradient

@Composable
fun getHeroGradient(): Brush =
    if (isAppInDarkTheme()) DarkGradients.heroGradient else LightGradients.heroGradient
