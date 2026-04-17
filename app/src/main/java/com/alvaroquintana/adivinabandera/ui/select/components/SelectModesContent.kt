package com.alvaroquintana.adivinabandera.ui.select.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.common.LocalAnimatedContentScope
import com.alvaroquintana.adivinabandera.ui.common.LocalSharedTransitionScope
import com.alvaroquintana.adivinabandera.ui.select.SelectUiState
import com.alvaroquintana.adivinabandera.ui.theme.DarkGeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.DarkSurface
import com.alvaroquintana.adivinabandera.ui.theme.GeoAmber
import com.alvaroquintana.adivinabandera.ui.theme.GeoAmberLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.GeoForest
import com.alvaroquintana.adivinabandera.ui.theme.GeoForestLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoGold
import com.alvaroquintana.adivinabandera.ui.theme.GeoGoldLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavy
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavyLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoPurple
import com.alvaroquintana.adivinabandera.ui.theme.GeoPurpleLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoTeal
import com.alvaroquintana.adivinabandera.ui.theme.GeoTealLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoTextMuted
import com.alvaroquintana.adivinabandera.ui.theme.GeoTextSecondary
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme
import com.alvaroquintana.domain.GameMode

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SelectModesContent(
    uiState: SelectUiState,
    onNavigateToGame: () -> Unit,
    onNavigateToCapitalByFlag: () -> Unit,
    onNavigateToRegionalSubdivisions: () -> Unit,
    onNavigateToCurrencyDetective: () -> Unit,
    onNavigateToPopulationChallenge: () -> Unit,
    onNavigateToWorldMix: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) DarkSurface else Color.White
    val borderColor = if (isDark) DarkGeoBorder else GeoBorder
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = if (isDark) Color(0xFF636E80) else GeoTextMuted
    val textSecondary = if (isDark) Color(0xFF636E80) else GeoTextSecondary
    val chevronColor = if (isDark) DarkGeoBorder else Color(0xFFC0C8D4)

    val cardTheme = CardTheme(cardBg, borderColor, textPrimary, textMuted, chevronColor, isDark)

    val currencyDesc = uiState.gameModeDescriptors.find { it.mode is GameMode.CurrencyDetective }
    val populationDesc = uiState.gameModeDescriptors.find { it.mode is GameMode.PopulationChallenge }
    val worldMixDesc = uiState.gameModeDescriptors.find { it.mode is GameMode.WorldMix }
    val currentLevel = uiState.currentLevel
    val unlockedRegions = uiState.unlockedRegionalCount
    val totalRegions = uiState.totalRegionalCount

    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalAnimatedContentScope.current

    // Fallback when outside SharedTransitionLayout (previews, etc.)
    if (sharedScope == null || animScope == null) {
        ModesContentLayout(
            modifier = modifier,
            textSecondary = textSecondary,
            cardTheme = cardTheme,
            currentLevel = currentLevel,
            unlockedRegions = unlockedRegions,
            totalRegions = totalRegions,
            currencyDesc = currencyDesc,
            populationDesc = populationDesc,
            worldMixDesc = worldMixDesc,
            onNavigateToGame = onNavigateToGame,
            onNavigateToCapitalByFlag = onNavigateToCapitalByFlag,
            onNavigateToRegionalSubdivisions = onNavigateToRegionalSubdivisions,
            onNavigateToCurrencyDetective = onNavigateToCurrencyDetective,
            onNavigateToPopulationChallenge = onNavigateToPopulationChallenge,
            onNavigateToWorldMix = onNavigateToWorldMix,
            sharedBoundsModifier = Modifier
        )
        return
    }

    val animatedBodyModifier = with(animScope) {
        Modifier.animateEnterExit(
            enter = slideInVertically(
                animationSpec = tween(400, delayMillis = 50, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ) + fadeIn(tween(300, delayMillis = 100))
        )
    }

    with(sharedScope) {
        val sharedBoundsModifier = Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "select-modes-bounds"),
            animatedVisibilityScope = animScope
        )

        ModesContentLayout(
            modifier = modifier,
            textSecondary = textSecondary,
            cardTheme = cardTheme,
            currentLevel = currentLevel,
            unlockedRegions = unlockedRegions,
            totalRegions = totalRegions,
            currencyDesc = currencyDesc,
            populationDesc = populationDesc,
            worldMixDesc = worldMixDesc,
            onNavigateToGame = onNavigateToGame,
            onNavigateToCapitalByFlag = onNavigateToCapitalByFlag,
            onNavigateToRegionalSubdivisions = onNavigateToRegionalSubdivisions,
            onNavigateToCurrencyDetective = onNavigateToCurrencyDetective,
            onNavigateToPopulationChallenge = onNavigateToPopulationChallenge,
            onNavigateToWorldMix = onNavigateToWorldMix,
            sharedBoundsModifier = sharedBoundsModifier,
            animatedBodyModifier = animatedBodyModifier
        )
    }
}

@Composable
private fun ModesContentLayout(
    modifier: Modifier,
    textSecondary: Color,
    cardTheme: CardTheme,
    currentLevel: Int,
    unlockedRegions: Int,
    totalRegions: Int,
    currencyDesc: com.alvaroquintana.domain.GameModeDescriptor?,
    populationDesc: com.alvaroquintana.domain.GameModeDescriptor?,
    worldMixDesc: com.alvaroquintana.domain.GameModeDescriptor?,
    onNavigateToGame: () -> Unit,
    onNavigateToCapitalByFlag: () -> Unit,
    onNavigateToRegionalSubdivisions: () -> Unit,
    onNavigateToCurrencyDetective: () -> Unit,
    onNavigateToPopulationChallenge: () -> Unit,
    onNavigateToWorldMix: () -> Unit,
    sharedBoundsModifier: Modifier,
    animatedBodyModifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .then(sharedBoundsModifier)
    ) {
        // Scrollable body
        Column(
            modifier = Modifier
                .clipToBounds()
                .then(animatedBodyModifier)
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.game_modes_label),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = textSecondary,
                letterSpacing = 1.5.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            GameModeCard(
                title = stringResource(R.string.mode_classic_title),
                subtitle = stringResource(R.string.mode_classic_subtitle),
                icon = Icons.Rounded.Flag,
                iconGradient = Brush.verticalGradient(listOf(GeoNavy, GeoNavyLight)),
                theme = cardTheme,
                onClick = onNavigateToGame
            )
            Spacer(modifier = Modifier.height(10.dp))
            GameModeCard(
                title = stringResource(R.string.play_capital_by_flag),
                subtitle = stringResource(R.string.mode_capital_flag_subtitle),
                icon = Icons.Rounded.LocationCity,
                iconGradient = Brush.verticalGradient(listOf(GeoForest, GeoForestLight)),
                theme = cardTheme,
                onClick = onNavigateToCapitalByFlag
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Entry point al chain regional (6 modos)
            GameModeCard(
                title = stringResource(R.string.play_regional_subdivisions),
                subtitle = stringResource(
                    R.string.regional_subdivisions_progress,
                    unlockedRegions,
                    totalRegions
                ),
                icon = Icons.Rounded.Map,
                iconGradient = Brush.verticalGradient(listOf(GeoAmber, GeoAmberLight)),
                theme = cardTheme,
                onClick = onNavigateToRegionalSubdivisions
            )
            Spacer(modifier = Modifier.height(10.dp))
            GameModeCard(
                title = stringResource(R.string.play_currency_detective),
                subtitle = stringResource(R.string.mode_currency_subtitle),
                icon = Icons.Rounded.Paid,
                iconGradient = Brush.verticalGradient(listOf(GeoPurple, GeoPurpleLight)),
                theme = cardTheme,
                onClick = onNavigateToCurrencyDetective,
                isLocked = !(currencyDesc?.isUnlocked ?: true),
                isNearUnlock = currencyDesc?.isNearUnlock ?: false,
                unlockProgress = currencyDesc?.unlockProgress ?: 1f,
                unlockLevel = currencyDesc?.unlockLevel ?: 5,
                currentLevel = currentLevel,
                accentColor = GeoPurpleLight
            )
            Spacer(modifier = Modifier.height(10.dp))
            GameModeCard(
                title = stringResource(R.string.play_population_challenge),
                subtitle = stringResource(R.string.mode_population_subtitle),
                icon = Icons.Rounded.BarChart,
                iconGradient = Brush.verticalGradient(listOf(GeoTeal, GeoTealLight)),
                theme = cardTheme,
                onClick = onNavigateToPopulationChallenge,
                isLocked = !(populationDesc?.isUnlocked ?: true),
                isNearUnlock = populationDesc?.isNearUnlock ?: false,
                unlockProgress = populationDesc?.unlockProgress ?: 1f,
                unlockLevel = populationDesc?.unlockLevel ?: 10,
                currentLevel = currentLevel,
                accentColor = GeoTealLight
            )
            Spacer(modifier = Modifier.height(10.dp))
            GameModeCard(
                title = stringResource(R.string.play_world_mix),
                subtitle = stringResource(R.string.mode_world_mix_subtitle),
                icon = Icons.Rounded.AutoAwesome,
                iconGradient = Brush.verticalGradient(listOf(GeoGold, GeoGoldLight)),
                theme = cardTheme,
                onClick = onNavigateToWorldMix,
                isLocked = !(worldMixDesc?.isUnlocked ?: false),
                isNearUnlock = worldMixDesc?.isNearUnlock ?: false,
                unlockProgress = worldMixDesc?.unlockProgress ?: 0f,
                unlockLevel = worldMixDesc?.unlockLevel ?: 15,
                currentLevel = currentLevel,
                accentColor = GeoGoldLight
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
