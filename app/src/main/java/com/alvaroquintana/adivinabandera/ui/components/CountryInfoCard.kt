package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.composables.FlagImage
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.domain.Country
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CountryInfoCard(
    country: Country,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // Flag image
            FlagImage(
                base64 = country.icon,
                url = country.flag,
                contentDescription = country.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Country name
                val localizedName = runCatching {
                    Locale.Builder().setRegion(country.alpha2Code).build().displayCountry
                }.getOrElse { country.name }

                Text(
                    text = localizedName.ifBlank { country.name },
                    fontFamily = DynaPuffCondensedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Region
                if (country.region.isNotBlank()) {
                    InfoRow(label = "Region", value = country.region)
                }

                // Capital
                if (country.capital.isNotBlank()) {
                    InfoRow(label = "Capital", value = country.capital)
                }

                // Population
                if (country.population > 0) {
                    val formattedPop = NumberFormat.getNumberInstance(Locale.getDefault())
                        .format(country.population)
                    InfoRow(label = "Population", value = formattedPop)
                }

                // Area
                if (country.area > 0) {
                    val formattedArea = NumberFormat.getNumberInstance(Locale.getDefault())
                        .format(country.area)
                    InfoRow(label = "Area", value = "$formattedArea km²")
                }

                // Currencies
                if (country.currencies.isNotEmpty()) {
                    val currencyText = country.currencies.joinToString(", ") { currency ->
                        buildString {
                            append(currency.name)
                            if (currency.symbol.isNotBlank()) append(" (${currency.symbol})")
                        }
                    }
                    InfoRow(label = "Currency", value = currencyText)
                }

                // Languages
                if (country.languages.isNotEmpty()) {
                    val languageText = country.languages.joinToString(", ") { it.name }
                    InfoRow(label = "Language", value = languageText)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = DynaPuffSemiCondensedFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.38f)
        )
        Text(
            text = value,
            fontFamily = DynaPuffSemiCondensedFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.62f)
        )
    }
}
