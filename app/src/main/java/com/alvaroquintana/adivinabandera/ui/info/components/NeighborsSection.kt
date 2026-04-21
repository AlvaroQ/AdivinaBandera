package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.domain.Country

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NeighborsSection(
    borders: List<String>,
    countryLookup: (String) -> Country?,
    onNeighborClick: (Country) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(
        modifier = modifier,
        title = stringResource(R.string.borders)
    ) {
        if (borders.isEmpty()) {
            Text(
                text = stringResource(R.string.no_neighbors),
                fontFamily = DynaPuffSemiCondensedFamily,
                fontStyle = FontStyle.Italic,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                borders.forEach { code ->
                    val neighbor = countryLookup(code)
                    NeighborChip(
                        code = code,
                        neighbor = neighbor,
                        onClick = { neighbor?.let { onNeighborClick(it) } }
                    )
                }
            }
        }
    }
}
