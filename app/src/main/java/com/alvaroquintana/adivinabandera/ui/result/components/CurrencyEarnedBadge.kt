package com.alvaroquintana.adivinabandera.ui.result.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.PillShape
import kotlinx.coroutines.delay

// Badge que muestra monedas y/o gemas ganadas en la partida.
@Composable
internal fun CurrencyEarnedBadge(coinsEarned: Int, gemsEarned: Int) {
    var showCurrency by remember { mutableStateOf(false) }

    LaunchedEffect(coinsEarned, gemsEarned) {
        delay(1100L)
        showCurrency = true
    }

    AnimatedVisibility(
        visible = showCurrency,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(400))
    ) {
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (coinsEarned > 0) {
                    Text(
                        text = "+$coinsEarned 🪙",
                        fontFamily = DynaPuffFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                if (gemsEarned > 0) {
                    Text(
                        text = "+$gemsEarned 💎",
                        fontFamily = DynaPuffFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
