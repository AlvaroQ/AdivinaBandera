package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.GeoForestLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavyLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoTealLight
import com.alvaroquintana.domain.Country

@Composable
fun GeographySection(
    country: Country,
    modifier: Modifier = Modifier
) {
    val notExist = stringResource(R.string.not_exist)

    SectionCard(
        modifier = modifier,
        title = stringResource(R.string.geography_section)
    ) {
        if (country.subregion.isNotBlank()) {
            InfoDetailRow(
                icon = Icons.Rounded.Explore,
                iconTint = GeoNavyLight,
                label = stringResource(R.string.subregion),
                value = country.subregion
            )
        }
        if (country.latlng.size >= 2) {
            CoordinatesRow(latlng = country.latlng)
        }
        if (country.timezones.isNotEmpty()) {
            InfoDetailRow(
                icon = Icons.Rounded.Schedule,
                iconTint = GeoTealLight,
                label = stringResource(R.string.timezones),
                value = country.timezones.joinToString(", ")
            )
        }
        if (country.callingCodes.isNotEmpty()) {
            InfoDetailRow(
                icon = Icons.Rounded.Call,
                iconTint = GeoForestLight,
                label = stringResource(R.string.calling_code_label),
                value = country.callingCodes.joinToString(", ") { "+$it" }
            )
        }
        if (country.topLevelDomain.isNotEmpty()) {
            InfoDetailRow(
                icon = Icons.Rounded.Language,
                iconTint = GeoNavyLight,
                label = stringResource(R.string.top_level_domain),
                value = country.topLevelDomain.joinToString(", ").ifBlank { notExist }
            )
        }
    }
}
