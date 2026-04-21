package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.info.utils.formatCoordinates
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavyLight

@Composable
fun CoordinatesRow(
    latlng: List<Double>,
    modifier: Modifier = Modifier
) {
    InfoDetailRow(
        icon = Icons.Rounded.PinDrop,
        iconTint = GeoNavyLight,
        label = stringResource(R.string.coordinates),
        value = formatCoordinates(latlng),
        modifier = modifier
    )
}
