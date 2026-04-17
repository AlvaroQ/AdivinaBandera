package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffCondensedFamily

@Composable
fun GameStatusRow(
    stageLabel: String,
    score: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stageLabel,
            fontFamily = DynaPuffCondensedFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        val animatedScore by animateIntAsState(
            targetValue = score,
            animationSpec = tween(500, easing = FastOutSlowInEasing),
            label = "scoreAnimation"
        )
        Text(
            text = "$animatedScore pts",
            fontFamily = DynaPuffCondensedFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
