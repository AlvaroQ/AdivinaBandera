package com.alvaroquintana.adivinabandera.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import com.alvaroquintana.adivinabandera.ui.theme.MotionTokens
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Grid3x3
import androidx.compose.material.icons.rounded.Grid4x4
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.ui.common.LocalDetailBackAction
import com.alvaroquintana.adivinabandera.ui.common.LocalDetailOpenState
import com.alvaroquintana.adivinabandera.ui.common.LocalInfoGridColumns
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
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass
import com.alvaroquintana.adivinabandera.ui.theme.isExpanded
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.toRouteString
import kotlinx.coroutines.launch
import dev.zacsweers.metrox.viewmodel.metroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(rootNavController: NavHostController) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pagerState = rememberPagerState(pageCount = { MainDestinations.entries.size })
    val scope = rememberCoroutineScope()
    val windowSize = LocalWindowSizeClass.current
    val isDetailOpen = remember { mutableStateOf(false) }
    val filtersExpanded = remember { mutableStateOf(false) }
    val infoTopBarTitle = remember { mutableStateOf<String?>(null) }
    val infoGridColumns = rememberSaveable { mutableIntStateOf(2) }
    val detailBackAction = remember { mutableStateOf<(() -> Unit)?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val lastBackPressAt = remember { mutableLongStateOf(0L) }
    val reducedMotion = rememberReducedMotion()
    val exitWindowMs = 2_000L

    val switchToTab: (Int) -> Unit = { idx ->
        scope.launch {
            detailBackAction.value = null
            isDetailOpen.value = false
            filtersExpanded.value = false
            infoTopBarTitle.value = null

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

    val handleBackPress: () -> Unit = {
        when {
            detailBackAction.value != null -> detailBackAction.value?.invoke()
            pagerState.currentPage != MainDestinations.Select.ordinal -> {
                switchToTab(MainDestinations.Select.ordinal)
            }
            else -> {
                val now = System.currentTimeMillis()
                if (now - lastBackPressAt.longValue <= exitWindowMs) {
                    (context as? androidx.activity.ComponentActivity)?.finish()
                } else {
                    lastBackPressAt.longValue = now
                    scope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.back_press_again_to_exit)
                        )
                    }
                }
            }
        }
    }

    BackHandler(enabled = true, onBack = handleBackPress)

    val currentDestination = MainDestinations.entries[pagerState.currentPage]
    val hideTopBarOnSelectHome =
        currentDestination == MainDestinations.Select && !isDetailOpen.value
    val topBarTitle = when (currentDestination) {
        MainDestinations.Info -> infoTopBarTitle.value ?: stringResource(R.string.info_title)
        MainDestinations.Ranking -> stringResource(R.string.ranking_screen_title)
        MainDestinations.Profile -> stringResource(R.string.profile_title)
        MainDestinations.Select -> stringResource(R.string.play)
    }

    CompositionLocalProvider(
        LocalDetailOpenState provides isDetailOpen,
        LocalInfoGridColumns provides infoGridColumns,
        LocalInfoFiltersExpanded provides filtersExpanded,
        LocalDetailBackAction provides detailBackAction
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            topBar = {
                if (!hideTopBarOnSelectHome) {
                    MainTopBar(
                        title = topBarTitle,
                        scrollBehavior = scrollBehavior,
                        navigationIcon = {
                            IconButton(onClick = handleBackPress) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
                                )
                            }
                        },
                        actions = {
                            if (!isDetailOpen.value) {
                                when (MainDestinations.entries[pagerState.currentPage]) {
                                    MainDestinations.Info -> {
                                        if (!windowSize.isExpanded) {
                                            val nextColumns = when (infoGridColumns.intValue) {
                                                2 -> 3
                                                3 -> 4
                                                else -> 2
                                            }
                                            IconButton(
                                                onClick = { infoGridColumns.intValue = nextColumns }
                                            ) {
                                                Icon(
                                                    imageVector = when (nextColumns) {
                                                        3 -> Icons.Rounded.Grid3x3
                                                        4 -> Icons.Rounded.Grid4x4
                                                        else -> Icons.Rounded.GridView
                                                    },
                                                    contentDescription = stringResource(
                                                        R.string.change_countries_grid_to,
                                                        nextColumns,
                                                        nextColumns
                                                    )
                                                )
                                            }
                                        }
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
                }
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
                        val viewModel: SelectViewModel = metroViewModel()
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
                                val ids = viewModel.state.value.weakSpotCountryIds
                                if (ids.isNotEmpty()) {
                                    rootNavController.navigate(Practice(ids))
                                }
                            }
                        )
                    }

                    MainDestinations.Info -> {
                        val viewModel: InfoViewModel = metroViewModel()
                        InfoScreen(
                            viewModel = viewModel,
                            onTopBarTitleChange = { selectedName ->
                                infoTopBarTitle.value = selectedName
                            }
                        )
                    }

                    MainDestinations.Ranking -> {
                        val viewModel: RankingViewModel = metroViewModel()
                        RankingScreen(viewModel = viewModel)
                    }

                    MainDestinations.Profile -> {
                        val viewModel: ProfileViewModel = metroViewModel()
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
