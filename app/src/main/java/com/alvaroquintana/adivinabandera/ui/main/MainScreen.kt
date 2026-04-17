package com.alvaroquintana.adivinabandera.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import com.alvaroquintana.adivinabandera.ui.theme.MotionTokens
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.ui.common.LocalDetailBackAction
import com.alvaroquintana.adivinabandera.ui.common.LocalDetailOpenState
import com.alvaroquintana.adivinabandera.ui.common.LocalInfoFiltersExpanded
import com.alvaroquintana.adivinabandera.ui.common.rememberReducedMotion
import com.alvaroquintana.adivinabandera.ui.info.InfoScreen
import com.alvaroquintana.adivinabandera.ui.info.InfoViewModel
import com.alvaroquintana.adivinabandera.ui.main.components.MainBottomBar
import com.alvaroquintana.adivinabandera.ui.main.components.MainTopBar
import com.alvaroquintana.adivinabandera.ui.navigation.Game
import com.alvaroquintana.adivinabandera.ui.navigation.MainDestinations
import com.alvaroquintana.adivinabandera.ui.navigation.Practice
import com.alvaroquintana.adivinabandera.ui.navigation.Settings
import com.alvaroquintana.adivinabandera.ui.navigation.Shop
import com.alvaroquintana.adivinabandera.ui.navigation.XpLeaderboard
import com.alvaroquintana.adivinabandera.ui.profile.ProfileScreen
import com.alvaroquintana.adivinabandera.ui.profile.ProfileViewModel
import com.alvaroquintana.adivinabandera.ui.ranking.RankingScreen
import com.alvaroquintana.adivinabandera.ui.ranking.RankingViewModel
import com.alvaroquintana.adivinabandera.ui.select.SelectScreen
import com.alvaroquintana.adivinabandera.ui.select.SelectViewModel
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.toRouteString
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rootNavController: NavHostController) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pagerState = rememberPagerState(pageCount = { MainDestinations.entries.size })
    val scope = rememberCoroutineScope()
    val isDetailOpen = remember { mutableStateOf(false) }
    val filtersExpanded = remember { mutableStateOf(false) }
    val detailBackAction = remember { mutableStateOf<(() -> Unit)?>(null) }
    val reducedMotion = rememberReducedMotion()

    val switchToTab: (Int) -> Unit = { idx ->
        scope.launch {
            detailBackAction.value = null
            isDetailOpen.value = false
            filtersExpanded.value = false

            if (reducedMotion) {
                pagerState.scrollToPage(idx)
            } else {
                pagerState.animateScrollToPage(
                    idx,
                    animationSpec = tween(
                        durationMillis = MotionTokens.DurationLong2,
                        easing = MotionTokens.Emphasized
                    )
                )
            }
        }
    }

    BackHandler(
        enabled = detailBackAction.value != null ||
            pagerState.currentPage != MainDestinations.Select.ordinal
    ) {
        if (pagerState.currentPage != MainDestinations.Select.ordinal) {
            switchToTab(MainDestinations.Select.ordinal)
        } else {
            detailBackAction.value?.invoke()
        }
    }

    CompositionLocalProvider(
        LocalDetailOpenState provides isDetailOpen,
        LocalInfoFiltersExpanded provides filtersExpanded,
        LocalDetailBackAction provides detailBackAction
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                MainTopBar(
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        detailBackAction.value?.let { onBack ->
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        }
                    },
                    actions = {
                        if (!isDetailOpen.value) {
                            when (MainDestinations.entries[pagerState.currentPage]) {
                                MainDestinations.Info -> {
                                    IconButton(onClick = { filtersExpanded.value = !filtersExpanded.value }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = stringResource(R.string.filter),
                                            tint = if (filtersExpanded.value) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                LocalContentColor.current
                                            }
                                        )
                                    }
                                }
                                else -> {
                                    IconButton(onClick = { rootNavController.navigate(Settings) }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Settings,
                                            contentDescription = stringResource(R.string.settings)
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                MainBottomBar(
                    selectedIndex = pagerState.currentPage,
                    onTabSelected = switchToTab
                )
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                userScrollEnabled = !isDetailOpen.value,
                beyondViewportPageCount = 1,
                key = { it },
                modifier = Modifier.padding(innerPadding)
            ) { page ->
                when (MainDestinations.entries[page]) {
                    MainDestinations.Select -> {
                        val viewModel: SelectViewModel = koinViewModel()
                        SelectScreen(
                            viewModel = viewModel,
                            onNavigateToGame = {
                                Analytics.analyticsClicked("btn_play")
                                rootNavController.navigate(Game(GameMode.Classic.toRouteString()))
                            },
                            onNavigateToCapitalByFlag = {
                                Analytics.analyticsClicked("btn_play_capital_flag")
                                rootNavController.navigate(Game(GameMode.CapitalByFlag.toRouteString()))
                            },
                            onNavigateToRegionalMode = { mode ->
                                Analytics.analyticsClicked("btn_play_${mode.toRouteString()}")
                                rootNavController.navigate(Game(mode.toRouteString()))
                            },
                            onNavigateToCurrencyDetective = {
                                Analytics.analyticsClicked("btn_play_currency_detective")
                                rootNavController.navigate(Game(GameMode.CurrencyDetective.toRouteString()))
                            },
                            onNavigateToPopulationChallenge = {
                                Analytics.analyticsClicked(Analytics.BTN_PLAY_POPULATION_CHALLENGE)
                                rootNavController.navigate(Game(GameMode.PopulationChallenge.toRouteString()))
                            },
                            onNavigateToWorldMix = {
                                Analytics.analyticsClicked(Analytics.BTN_PLAY_WORLD_MIX)
                                rootNavController.navigate(Game(GameMode.WorldMix.toRouteString()))
                            },
                            onNavigateToLearn = {
                                Analytics.analyticsClicked("btn_learn")
                                switchToTab(MainDestinations.Info.ordinal)
                            },
                            onNavigateToRanking = {
                                Analytics.analyticsClicked("btn_ranking")
                                switchToTab(MainDestinations.Ranking.ordinal)
                            },
                            onNavigateToProfile = {
                                Analytics.analyticsClicked("btn_profile")
                                switchToTab(MainDestinations.Profile.ordinal)
                            },
                            onNavigateToShop = {
                                Analytics.analyticsClicked("btn_shop")
                                rootNavController.navigate(Shop)
                            },
                            onNavigateToPractice = {
                                val ids = viewModel.uiState.value.weakSpotCountryIds
                                if (ids.isNotEmpty()) {
                                    rootNavController.navigate(Practice(ids))
                                }
                            }
                        )
                    }

                    MainDestinations.Info -> {
                        val viewModel: InfoViewModel = koinViewModel()
                        InfoScreen(viewModel = viewModel)
                    }

                    MainDestinations.Ranking -> {
                        val viewModel: RankingViewModel = koinViewModel()
                        RankingScreen(viewModel = viewModel)
                    }

                    MainDestinations.Profile -> {
                        val viewModel: ProfileViewModel = koinViewModel()
                        ProfileScreen(
                            viewModel = viewModel,
                            onBack = { /* no-op dentro del pager */ },
                            onNavigateToXpLeaderboard = {
                                rootNavController.navigate(XpLeaderboard)
                            }
                        )
                    }
                }
            }
        }
    }
}
