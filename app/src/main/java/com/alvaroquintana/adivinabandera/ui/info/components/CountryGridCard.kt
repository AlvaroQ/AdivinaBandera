package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.common.LocalAnimatedContentScope
import com.alvaroquintana.adivinabandera.ui.common.LocalSharedTransitionScope
import com.alvaroquintana.adivinabandera.ui.composables.FlagImage
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.ElevationTokens
import com.alvaroquintana.domain.Country

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun CountryGridCard(
    country: Country,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalAnimatedContentScope.current

    val flagDescription = stringResource(R.string.flag_of, country.name)
    val cardCd = stringResource(R.string.go_to_country_detail, country.name)

    val surfaceModifier = if (sharedScope != null && animScope != null) {
        with(sharedScope) {
            modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState("country-bounds-${country.alpha2Code}"),
                    animatedVisibilityScope = animScope
                )
                .clickable(onClick = onClick)
                .semantics { contentDescription = cardCd }
        }
    } else {
        modifier
            .clickable(onClick = onClick)
            .semantics { contentDescription = cardCd }
    }

    Card(
        modifier = surfaceModifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = if (selected) {
            androidx.compose.foundation.BorderStroke(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            null
        },
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.Level1)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 2f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                val flagModifier = if (sharedScope != null && animScope != null) {
                    with(sharedScope) {
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(3f / 2f)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState("country-flag-shared-${country.alpha2Code}"),
                                animatedVisibilityScope = animScope,
                                boundsTransform = { _, _ -> androidx.compose.animation.core.tween(durationMillis = 500) }
                            )
                    }
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 2f)
                }

                FlagImage(
                    base64 = country.icon,
                    url = country.flag,
                    contentDescription = flagDescription,
                    modifier = flagModifier,
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = country.name,
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (country.capital.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = country.capital,
                            fontFamily = DynaPuffSemiCondensedFamily,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
