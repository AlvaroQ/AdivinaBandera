package com.alvaroquintana.adivinabandera.ui.select

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.ui.common.LocalAnimatedContentScope
import com.alvaroquintana.adivinabandera.ui.common.LocalDetailBackAction
import com.alvaroquintana.adivinabandera.ui.common.LocalDetailOpenState
import com.alvaroquintana.adivinabandera.ui.common.LocalSharedTransitionScope
import com.alvaroquintana.adivinabandera.ui.common.rememberReducedMotion
import com.alvaroquintana.adivinabandera.ui.select.components.RegionalSubdivisionsContent
import com.alvaroquintana.adivinabandera.ui.select.components.SelectHomeContent
import com.alvaroquintana.adivinabandera.ui.select.components.SelectModesContent
import com.alvaroquintana.domain.GameMode

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SelectScreen(
    viewModel: SelectViewModel,
    onNavigateToGame: () -> Unit,
    onNavigateToCapitalByFlag: () -> Unit,
    onNavigateToRegionalMode: (GameMode) -> Unit,
    onNavigateToCurrencyDetective: () -> Unit,
    onNavigateToPopulationChallenge: () -> Unit,
    onNavigateToWorldMix: () -> Unit,
    onNavigateToLearn: () -> Unit,
    onNavigateToRanking: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToPractice: () -> Unit = {}
) {
    var modesExpanded by rememberSaveable { mutableStateOf(false) }
    var regionalExpanded by rememberSaveable { mutableStateOf(false) }
    val detailOpen = LocalDetailOpenState.current
    val backAction = LocalDetailBackAction.current
    val reducedMotion = rememberReducedMotion()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(modesExpanded, regionalExpanded) {
        detailOpen.value = modesExpanded || regionalExpanded
        backAction.value = when {
            regionalExpanded -> { { regionalExpanded = false } }
            modesExpanded -> { { modesExpanded = false } }
            else -> null
        }
    }
    DisposableEffect(Unit) { onDispose { backAction.value = null } }
    BackHandler(enabled = modesExpanded || regionalExpanded) {
        when {
            regionalExpanded -> regionalExpanded = false
            modesExpanded -> modesExpanded = false
        }
    }

    // Al cerrar el panel regional, refresca por si el jugador volvió de una partida regional.
    LaunchedEffect(regionalExpanded) {
        if (regionalExpanded) viewModel.refreshRegionalProgression()
    }

    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
            // Target state compuesto: 0 = home, 1 = modes, 2 = regional
            val target = when {
                regionalExpanded -> 2
                modesExpanded -> 1
                else -> 0
            }
            AnimatedContent(
                targetState = target,
                label = "select-modes-transform",
                transitionSpec = {
                    if (reducedMotion) {
                        fadeIn(tween(80)) togetherWith fadeOut(tween(80))
                    } else {
                        (fadeIn(tween(300)) togetherWith fadeOut(tween(150)))
                            .using(SizeTransform(clip = false))
                    }
                }
            ) { state ->
                CompositionLocalProvider(LocalAnimatedContentScope provides this@AnimatedContent) {
                    when (state) {
                        0 -> SelectHomeContent(
                            viewModel = viewModel,
                            uiState = uiState,
                            onNavigateToGame = onNavigateToGame,
                            onModesClick = { modesExpanded = true },
                            onNavigateToLearn = onNavigateToLearn,
                            onNavigateToRanking = onNavigateToRanking,
                            onNavigateToProfile = onNavigateToProfile,
                            onNavigateToShop = onNavigateToShop,
                            onNavigateToPractice = onNavigateToPractice
                        )
                        1 -> SelectModesContent(
                            uiState = uiState,
                            onNavigateToGame = onNavigateToGame,
                            onNavigateToCapitalByFlag = onNavigateToCapitalByFlag,
                            onNavigateToRegionalSubdivisions = { regionalExpanded = true },
                            onNavigateToCurrencyDetective = onNavigateToCurrencyDetective,
                            onNavigateToPopulationChallenge = onNavigateToPopulationChallenge,
                            onNavigateToWorldMix = onNavigateToWorldMix
                        )
                        else -> RegionalSubdivisionsContent(
                            descriptors = uiState.regionalModeDescriptors,
                            onNavigateToRegion = { mode -> onNavigateToRegionalMode(mode) }
                        )
                    }
                }
            }
        }
    }
}
