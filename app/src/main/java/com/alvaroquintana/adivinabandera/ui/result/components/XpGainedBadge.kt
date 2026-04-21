package com.alvaroquintana.adivinabandera.ui.result.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.result.ResultUiState
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.PillShape
import kotlinx.coroutines.delay

// Badge animado que muestra el XP ganado y el desglose al final de la partida.
// Aparece 900ms despues del render inicial para no competir con el badge de engagement.
@Composable
internal fun XpGainedBadge(uiState: ResultUiState) {
    if (uiState.xpGained <= 0) return

    var showXp by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.xpGained) {
        delay(900L)
        showXp = true
    }

    AnimatedVisibility(
        visible = showXp,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(400))
    ) {
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "+${uiState.xpGained} XP",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                val breakdown = uiState.xpBreakdown
                if (breakdown != null) {
                    Text(
                        text = buildString {
                            append("Base: +${breakdown.base}")
                            if (breakdown.streakBonus > 0) append(" | Racha: +${breakdown.streakBonus}")
                            if (breakdown.perfectBonus > 0) append(" | Perfecto: +${breakdown.perfectBonus}")
                            if (breakdown.winBonus > 0) append(" | Bonus: +${breakdown.winBonus}")
                        },
                        fontFamily = DynaPuffSemiCondensedFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
