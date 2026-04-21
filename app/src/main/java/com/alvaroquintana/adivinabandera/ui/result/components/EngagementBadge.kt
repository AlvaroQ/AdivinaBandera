package com.alvaroquintana.adivinabandera.ui.result.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.result.EngagementLevel
import com.alvaroquintana.adivinabandera.ui.result.ResultUiState
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.PillShape
import kotlinx.coroutines.delay

// Badge motivacional que aparece suavemente 600ms despues del render inicial.
// No muestra nada si es KEEP_TRYING sin diferencia de puntos (primera partida).
@Composable
internal fun EngagementBadge(
    uiState: ResultUiState,
    gamePoints: Int
) {
    if (gamePoints == 0) return

    var showBadge by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(600L)
        showBadge = true
    }

    AnimatedVisibility(
        visible = showBadge,
        enter = scaleIn() + fadeIn(animationSpec = tween(400))
    ) {
        val text = when (uiState.engagementLevel) {
            EngagementLevel.NEW_WORLD_RECORD ->
                stringResource(R.string.result_engagement_world_record, uiState.pointsDifference)
            EngagementLevel.NEW_PERSONAL_BEST ->
                stringResource(R.string.result_engagement_personal_best, uiState.pointsDifference)
            EngagementLevel.SO_CLOSE ->
                stringResource(R.string.result_engagement_so_close, uiState.pointsDifference)
            EngagementLevel.KEEP_TRYING ->
                stringResource(R.string.result_engagement_keep_trying)
        }

        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}
