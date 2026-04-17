package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.info.utils.computeDensity
import com.alvaroquintana.adivinabandera.ui.info.utils.formatArea
import com.alvaroquintana.adivinabandera.ui.info.utils.formatPopulationFull
import com.alvaroquintana.adivinabandera.ui.theme.GeoAmberLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoForestLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavyLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoPurpleLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoTealLight
import com.alvaroquintana.domain.Country

@Composable
fun AtAGlanceGrid(
    country: Country,
    modifier: Modifier = Modifier
) {
    val notExist = stringResource(R.string.not_exist)
    val areaUnit = stringResource(R.string.area_unit)
    val densityUnit = stringResource(R.string.density_unit)

    val capital = country.capital.ifBlank { notExist }

    val populationValue = formatPopulationFull(country.population)

    val areaValue = if (country.area > 0) "${formatArea(country.area)} $areaUnit" else formatArea(country.area)

    val densityValue = if (country.population > 0 && country.area > 0) {
        "${computeDensity(country.population, country.area)} $densityUnit"
    } else {
        computeDensity(country.population, country.area)
    }

    val currencyValue = country.currencies.firstOrNull()?.let { cur ->
        val code = cur.code.ifBlank { cur.name }
        val extra = when {
            cur.symbol.isNotBlank() && cur.name.isNotBlank() && cur.code.isNotBlank() -> "${cur.symbol} ${cur.name}"
            cur.symbol.isNotBlank() && cur.code.isNotBlank() -> cur.symbol
            cur.name.isNotBlank() && cur.code.isNotBlank() -> cur.name
            else -> ""
        }
        if (extra.isNotBlank()) "$code $extra" else code
    } ?: notExist

    val languagesValue = if (country.languages.isNotEmpty()) {
        val main = country.languages.first().name
        if (country.languages.size > 1) "$main +${country.languages.size - 1}" else main
    } else notExist

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Rounded.LocationOn,
                iconTint = GeoForestLight,
                label = stringResource(R.string.capital_label),
                value = capital,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatCard(
                icon = Icons.Rounded.People,
                iconTint = GeoTealLight,
                label = stringResource(R.string.population_label),
                value = populationValue,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Rounded.Landscape,
                iconTint = GeoNavyLight,
                label = stringResource(R.string.area_label),
                value = areaValue,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatCard(
                icon = Icons.Rounded.GridView,
                iconTint = GeoNavyLight,
                label = stringResource(R.string.population_density),
                value = densityValue,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                icon = Icons.Rounded.Payments,
                iconTint = GeoPurpleLight,
                label = stringResource(R.string.currency_label),
                value = currencyValue,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            StatCard(
                icon = Icons.Rounded.Translate,
                iconTint = GeoAmberLight,
                label = stringResource(R.string.languages_label),
                value = languagesValue,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
        }
    }
}
