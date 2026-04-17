package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.composables.FlagImage
import com.alvaroquintana.adivinabandera.ui.common.LocalAnimatedContentScope
import com.alvaroquintana.adivinabandera.ui.common.LocalSharedTransitionScope
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass
import com.alvaroquintana.adivinabandera.ui.theme.isExpanded
import com.alvaroquintana.adivinabandera.ui.theme.isMedium
import com.alvaroquintana.adivinabandera.ui.theme.MotionTokens
import com.alvaroquintana.domain.Country

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CountryHeroCard(
    country: Country,
    modifier: Modifier = Modifier,
    showFlag: Boolean = true
) {
    val windowSize = LocalWindowSizeClass.current
    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalAnimatedContentScope.current

    val flagHeight = when {
        windowSize.isExpanded -> 320.dp
        windowSize.isMedium -> 260.dp
        else -> 200.dp
    }
    val nameSize = when {
        windowSize.isExpanded -> 30.sp
        windowSize.isMedium -> 26.sp
        else -> 22.sp
    }

    val flagDescription = stringResource(R.string.flag_of, country.name)
    val showFlagAtRight = showFlag && windowSize.isExpanded

    Column(modifier = modifier.fillMaxWidth()) {
        if (showFlag && !showFlagAtRight) {
            AnimatedVisibility(
                visible = showFlag,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(flagHeight)
                        .semantics { contentDescription = flagDescription },
                    contentAlignment = Alignment.Center
                ) {
                    val flagModifier = if (sharedScope != null && animScope != null) {
                        with(sharedScope) {
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState("country-flag-shared-${country.alpha2Code}"),
                                    animatedVisibilityScope = animScope,
                                    boundsTransform = { _, _ -> androidx.compose.animation.core.tween(durationMillis = MotionTokens.DurationExtraLong1, easing = MotionTokens.Emphasized) }
                                )
                        }
                    } else {
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    }

                    FlagImage(
                        base64 = country.icon,
                        url = country.flag,
                        contentDescription = flagDescription,
                        modifier = flagModifier,
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        if (showFlagAtRight) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                HeroTextBlock(
                    country = country,
                    nameSize = nameSize,
                    modifier = Modifier.weight(0.6f)
                )
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .aspectRatio(4f / 3f)
                        .semantics { contentDescription = flagDescription },
                    contentAlignment = Alignment.Center
                ) {
                    val flagModifier = if (sharedScope != null && animScope != null) {
                        with(sharedScope) {
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .sharedElement(
                                    sharedContentState = rememberSharedContentState("country-flag-shared-${country.alpha2Code}"),
                                    animatedVisibilityScope = animScope,
                                    boundsTransform = { _, _ -> androidx.compose.animation.core.tween(durationMillis = MotionTokens.DurationExtraLong1, easing = MotionTokens.Emphasized) }
                                )
                        }
                    } else {
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    }

                    FlagImage(
                        base64 = country.icon,
                        url = country.flag,
                        contentDescription = flagDescription,
                        modifier = flagModifier,
                        contentScale = ContentScale.Fit
                    )
                }
            }
        } else {
            HeroTextBlock(
                country = country,
                nameSize = nameSize,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun HeroTextBlock(
    country: Country,
    nameSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = country.name,
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = nameSize,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (country.nativeName.isNotBlank() && country.nativeName != country.name) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = country.nativeName,
                        fontFamily = DynaPuffSemiCondensedFamily,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (country.demonym.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${stringResource(R.string.demonym)}: ${country.demonym}",
                        fontFamily = DynaPuffSemiCondensedFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AlphaCodeBadge(code = country.alpha2Code)
                AlphaCodeBadge(
                    code = country.alpha3Code,
                    background = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        if (country.region.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                val location = buildString {
                    append(country.region)
                    if (country.subregion.isNotBlank()) append(" › ${country.subregion}")
                }
                Text(
                    text = location,
                    fontFamily = DynaPuffSemiCondensedFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
