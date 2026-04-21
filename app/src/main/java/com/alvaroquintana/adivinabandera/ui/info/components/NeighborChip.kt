package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.composables.FlagImage
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.domain.Country

@Composable
fun NeighborChip(
    code: String,
    neighbor: Country?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayName = neighbor?.name ?: code
    val description = if (neighbor != null) {
        stringResource(R.string.go_to_country_detail, displayName)
    } else {
        stringResource(R.string.neighboring_country_code, code)
    }

    Surface(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clickable(enabled = neighbor != null, onClick = onClick)
            .semantics { contentDescription = description },
        shape = RoundedCornerShape(24.dp),
        color = if (neighbor != null) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (neighbor != null) {
                Box(
                    modifier = Modifier
                        .size(width = 28.dp, height = 20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    FlagImage(
                        base64 = neighbor.icon,
                        url = neighbor.flag,
                        contentDescription = displayName,
                        modifier = Modifier.size(width = 28.dp, height = 20.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Text(
                    text = displayName,
                    fontFamily = DynaPuffSemiCondensedFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            } else {
                Text(
                    text = code,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
