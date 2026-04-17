package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Forest
import androidx.compose.material.icons.rounded.Landscape
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Sailing
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily

data class RegionOption(
    val key: String?,
    val labelRes: Int,
    val icon: ImageVector
)

val DefaultRegionOptions: List<RegionOption> = listOf(
    RegionOption(null, R.string.region_all, Icons.Rounded.FilterList),
    RegionOption("Africa", R.string.region_africa, Icons.Rounded.WbSunny),
    RegionOption("Americas", R.string.region_americas, Icons.Rounded.Forest),
    RegionOption("Asia", R.string.region_asia, Icons.Rounded.Landscape),
    RegionOption("Europe", R.string.region_europe, Icons.Rounded.Public),
    RegionOption("Oceania", R.string.region_oceania, Icons.Rounded.Sailing),
    RegionOption("Polar", R.string.region_polar, Icons.Rounded.AcUnit)
)

@Composable
fun RegionFilterChip(
    option: RegionOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val label = stringResource(option.labelRes)
    val statusLabel = stringResource(if (selected) R.string.selected else R.string.not_selected)
    val cd = stringResource(R.string.region_filter_cd, label, statusLabel)

    FilterChip(
        modifier = modifier.semantics { contentDescription = cd },
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}
