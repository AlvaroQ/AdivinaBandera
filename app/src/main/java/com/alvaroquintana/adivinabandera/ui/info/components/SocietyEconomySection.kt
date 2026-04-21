package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.GeoAmberLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoPurpleLight
import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.Currency
import com.alvaroquintana.domain.Language

@Composable
fun SocietyEconomySection(
    country: Country,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier,
        title = androidx.compose.ui.res.stringResource(R.string.society_economy_section)
    ) {
        country.currencies.forEach { currency ->
            CurrencyRow(currency)
        }
        country.languages.forEach { lang ->
            LanguageRow(lang)
        }
        if (country.demonym.isNotBlank()) {
            InfoDetailRow(
                icon = Icons.Rounded.People,
                iconTint = GeoAmberLight,
                label = androidx.compose.ui.res.stringResource(R.string.demonym),
                value = country.demonym
            )
        }
        country.gini?.let { gini ->
            Spacer(modifier = Modifier.size(4.dp))
            GiniIndicator(gini = gini)
        }
    }
}

@Composable
private fun CurrencyRow(currency: Currency) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = GeoPurpleLight.copy(alpha = 0.15f)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (currency.symbol.isNotBlank()) {
                    Text(
                        text = currency.symbol,
                        fontFamily = DynaPuffSemiCondensedFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = GeoPurpleLight
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.Payments,
                        contentDescription = null,
                        tint = GeoPurpleLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = currency.name.ifBlank { currency.code },
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (currency.code.isNotBlank()) {
                Text(
                    text = currency.code,
                    fontFamily = DynaPuffSemiCondensedFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LanguageRow(language: Language) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(10.dp),
            color = GeoAmberLight.copy(alpha = 0.15f)
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Translate,
                    contentDescription = null,
                    tint = GeoAmberLight,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = language.name,
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (language.nativeName.isNotBlank() && language.nativeName != language.name) {
                Text(
                    text = language.nativeName,
                    fontFamily = DynaPuffSemiCondensedFamily,
                    fontStyle = FontStyle.Italic,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

