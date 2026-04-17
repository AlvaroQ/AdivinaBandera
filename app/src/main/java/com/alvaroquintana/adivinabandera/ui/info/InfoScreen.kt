package com.alvaroquintana.adivinabandera.ui.info

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.ui.common.LocalAnimatedContentScope
import com.alvaroquintana.adivinabandera.ui.common.LocalDetailBackAction
import com.alvaroquintana.adivinabandera.ui.common.LocalDetailOpenState
import com.alvaroquintana.adivinabandera.ui.common.LocalSharedTransitionScope
import com.alvaroquintana.adivinabandera.ui.common.rememberReducedMotion
import com.alvaroquintana.adivinabandera.ui.components.EmptyState
import com.alvaroquintana.adivinabandera.ui.components.LoadingState
import com.alvaroquintana.adivinabandera.ui.info.components.EmptySelectionPlaceholder
import com.alvaroquintana.adivinabandera.ui.info.components.InfoDetailContent
import com.alvaroquintana.adivinabandera.ui.info.components.InfoListContent
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.adivinabandera.ui.theme.isExpanded
import com.alvaroquintana.adivinabandera.ui.theme.MotionTokens
import com.alvaroquintana.domain.Country
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alvaroquintana.adivinabandera.R

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun InfoScreen(viewModel: InfoViewModel) {
    val windowSize = LocalWindowSizeClass.current
    var selectedAlpha2 by rememberSaveable { mutableStateOf<String?>(null) }
    var currentPage by rememberSaveable { mutableIntStateOf(0) }

    val detailOpen = LocalDetailOpenState.current
    val backAction = LocalDetailBackAction.current
    val reducedMotion = rememberReducedMotion()

    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val isLoading = progress is InfoViewModel.UiModel.Loading &&
            (progress as InfoViewModel.UiModel.Loading).show

    val countryList by viewModel.countryList.collectAsStateWithLifecycle()

    LaunchedEffect(selectedAlpha2) {
        detailOpen.value = selectedAlpha2 != null
        backAction.value = if (selectedAlpha2 != null) {
            { selectedAlpha2 = null }
        } else null
    }
    DisposableEffect(Unit) { onDispose { backAction.value = null } }
    BackHandler(enabled = selectedAlpha2 != null && !windowSize.isExpanded) { selectedAlpha2 = null }

    val countryByAlpha3 = remember(countryList) {
        countryList.associateBy { it.alpha3Code.uppercase() }
    }

    val selectedCountry = remember(countryList, selectedAlpha2) {
        selectedAlpha2?.let { a -> countryList.firstOrNull { it.alpha2Code.equals(a, ignoreCase = true) } }
    }

    when {
        isLoading && countryList.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getBackgroundGradient())
            ) { LoadingState() }
        }

        countryList.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(getBackgroundGradient())
            ) {
                EmptyState(message = stringResource(R.string.info_title))
            }
        }

        windowSize.isExpanded -> {
            InfoExpandedLayout(
                countryList = countryList,
                selectedCountry = selectedCountry,
                countryByAlpha3 = countryByAlpha3,
                onCountryClick = { selectedAlpha2 = it.alpha2Code },
                onClearSelection = { selectedAlpha2 = null },
                currentPage = currentPage,
                onLoadMore = { next ->
                    currentPage = next
                    viewModel.loadMorePrideList(next)
                }
            )
        }

        else -> {
            InfoCompactLayout(
                countryList = countryList,
                selectedCountry = selectedCountry,
                countryByAlpha3 = countryByAlpha3,
                reducedMotion = reducedMotion,
                onCountryClick = { selectedAlpha2 = it.alpha2Code },
                onClearSelection = { selectedAlpha2 = null },
                currentPage = currentPage,
                onLoadMore = { next ->
                    currentPage = next
                    viewModel.loadMorePrideList(next)
                }
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun InfoCompactLayout(
    countryList: List<Country>,
    selectedCountry: Country?,
    countryByAlpha3: Map<String, Country>,
    reducedMotion: Boolean,
    onCountryClick: (Country) -> Unit,
    onClearSelection: () -> Unit,
    currentPage: Int,
    onLoadMore: (Int) -> Unit
) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this@SharedTransitionLayout) {
            AnimatedContent(
                targetState = selectedCountry,
                label = "country-detail-transform",
                transitionSpec = {
                    if (reducedMotion) {
                        fadeIn(tween(80)) togetherWith fadeOut(tween(80))
                    } else {
                        (fadeIn(
                            animationSpec = tween(
                                durationMillis = MotionTokens.DurationLong2,
                                easing = MotionTokens.Emphasized
                            )
                        ) togetherWith fadeOut(
                            animationSpec = tween(
                                durationMillis = MotionTokens.DurationShort3,
                                easing = MotionTokens.EmphasizedAccelerate
                            )
                        ))
                            .using(SizeTransform(clip = false))
                    }
                },
                contentKey = { it?.alpha2Code }
            ) { country ->
                CompositionLocalProvider(LocalAnimatedContentScope provides this@AnimatedContent) {
                    if (country == null) {
                        InfoListContent(
                            countries = countryList,
                            onCountryClick = onCountryClick,
                            currentPage = currentPage,
                            onLoadMore = onLoadMore
                        )
                    } else {
                        InfoDetailContent(
                            country = country,
                            countryByAlpha3 = countryByAlpha3,
                            onBack = onClearSelection
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoExpandedLayout(
    countryList: List<Country>,
    selectedCountry: Country?,
    countryByAlpha3: Map<String, Country>,
    onCountryClick: (Country) -> Unit,
    onClearSelection: () -> Unit,
    currentPage: Int,
    onLoadMore: (Int) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxHeight()
        ) {
            InfoListContent(
                countries = countryList,
                onCountryClick = onCountryClick,
                currentPage = currentPage,
                onLoadMore = onLoadMore,
                forceStack = true
            )
        }

        VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.fillMaxHeight()
        )

        Box(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxHeight()
        ) {
            Crossfade(
                targetState = selectedCountry,
                animationSpec = tween(180),
                label = "expanded-detail-crossfade"
            ) { country ->
                if (country != null) {
                    InfoDetailContent(
                        country = country,
                        countryByAlpha3 = countryByAlpha3,
                        onBack = onClearSelection,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    EmptySelectionPlaceholder(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}
