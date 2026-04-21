package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.ui.animation.NavTransitions
import com.alvaroquintana.adivinabandera.ui.common.LocalAnimatedContentScope
import com.alvaroquintana.adivinabandera.ui.common.LocalSharedTransitionScope
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.adivinabandera.ui.theme.isExpanded
import com.alvaroquintana.domain.Country

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun InfoDetailContent(
    country: Country,
    countryByAlpha3: Map<String, Country>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animScope = LocalAnimatedContentScope.current
    val windowSize = LocalWindowSizeClass.current

    Column(modifier = modifier.fillMaxSize().background(getBackgroundGradient())) {

        if (animScope != null) {
            with(animScope) {
                DetailBody(
                    country = country,
                    countryByAlpha3 = countryByAlpha3,
                    isExpanded = windowSize.isExpanded,
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .animateEnterExit(
                            enter = NavTransitions.enterTransition,
                            exit = NavTransitions.fadeExitTransition
                        )
                )
            }
        } else {
            DetailBody(
                country = country,
                countryByAlpha3 = countryByAlpha3,
                isExpanded = windowSize.isExpanded,
                modifier = Modifier.fillMaxSize().clipToBounds()
            )
        }
    }
}

@Composable
private fun DetailBody(
    country: Country,
    countryByAlpha3: Map<String, Country>,
    isExpanded: Boolean,
    modifier: Modifier = Modifier
) {
    if (isExpanded) {
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier
        ) {
            item { CountryHeroCard(country, showFlag = true, modifier = Modifier.fillMaxWidth()) }
            item { AtAGlanceGrid(country) }
            item { GeographySection(country) }
            item {
                NeighborsSection(
                    borders = country.borders,
                    countryLookup = { code -> countryByAlpha3[code.uppercase()] },
                    onNeighborClick = {}
                )
            }
            item { SocietyEconomySection(country) }
            item { TranslationsSection(country.translations) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    } else {
        // Compact: Track scroll position to show/hide hero flag
        val lazyListState = rememberLazyListState()
        val showHeroFlag by remember {
            derivedStateOf {
                lazyListState.firstVisibleItemIndex == 0 &&
                lazyListState.firstVisibleItemScrollOffset < 100
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = modifier,
            state = lazyListState
        ) {
            item { CountryHeroCard(country, showFlag = showHeroFlag) }
            item { AtAGlanceGrid(country) }
            item { GeographySection(country) }
            item {
                NeighborsSection(
                    borders = country.borders,
                    countryLookup = { code -> countryByAlpha3[code.uppercase()] },
                    onNeighborClick = {}
                )
            }
            item { SocietyEconomySection(country) }
            item { TranslationsSection(country.translations) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
