package com.alvaroquintana.adivinabandera.ui.profile.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.alvaroquintana.adivinabandera.ui.theme.GameBronze
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GameSilver
import com.alvaroquintana.adivinabandera.ui.theme.GeoAmberLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoForestLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoftLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoPurpleLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoTealLight

/**
 * Tier identifier derived from level — used by UI to decorate the hero and chips.
 */
enum class LevelTier(val displayName: String) {
    Novato("Novato"),
    Explorador("Explorador"),
    Entusiasta("Entusiasta"),
    Conocedor("Conocedor"),
    Experto("Experto"),
    Maestro("Maestro"),
    GranMaestro("Gran Maestro"),
    Leyenda("Leyenda");

    companion object {
        fun fromLevel(level: Int): LevelTier = when {
            level <= 5  -> Novato
            level <= 10 -> Explorador
            level <= 15 -> Entusiasta
            level <= 20 -> Conocedor
            level <= 30 -> Experto
            level <= 40 -> Maestro
            level <= 50 -> GranMaestro
            else        -> Leyenda
        }
    }
}

/**
 * Primary decorative color for a given tier.
 * Used for the XP ring, level badge and title chip.
 */
@Composable
fun LevelTier.accentColor(): Color = when (this) {
    LevelTier.Novato      -> GeoTealLight
    LevelTier.Explorador  -> GeoForestLight
    LevelTier.Entusiasta  -> GeoNavySoftLight
    LevelTier.Conocedor   -> GeoPurpleLight
    LevelTier.Experto     -> GeoAmberLight
    LevelTier.Maestro     -> GameBronze
    LevelTier.GranMaestro -> GameSilver
    LevelTier.Leyenda     -> GameGold
}
