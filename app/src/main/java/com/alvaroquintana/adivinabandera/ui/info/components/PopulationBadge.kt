package com.alvaroquintana.adivinabandera.ui.info.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.People
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.info.utils.formatPopulationShort
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily

@Composable
fun PopulationBadge(
    population: Int,
    modifier: Modifier = Modifier
) {
    if (population <= 0) return
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xCC0E1628),
        contentColor = Color(0xFFD0DAEA)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.People,
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = " " + formatPopulationShort(population),
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
    }
}
