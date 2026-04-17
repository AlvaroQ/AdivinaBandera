package com.alvaroquintana.adivinabandera.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 elevation levels.
 * Source: https://m3.material.io/styles/elevation/tokens
 *
 * In M3, elevation is expressed as both a shadow AND a tonal overlay on surface.
 * Use with Card(elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.Level1)).
 */
object ElevationTokens {
    val Level0: Dp = 0.dp
    val Level1: Dp = 1.dp
    val Level2: Dp = 3.dp
    val Level3: Dp = 6.dp
    val Level4: Dp = 8.dp
    val Level5: Dp = 12.dp
}
