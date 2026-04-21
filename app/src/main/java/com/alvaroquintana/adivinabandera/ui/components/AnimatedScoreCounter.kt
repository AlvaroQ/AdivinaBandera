package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.ui.animation.AnimationSpecs
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffCondensedFamily
import kotlinx.coroutines.launch

@Composable
fun AnimatedScoreCounter(
    targetScore: Int,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    var displayScore by remember { mutableIntStateOf(0) }

    LaunchedEffect(targetScore) {
        animate(
            initialValue = displayScore.toFloat(),
            targetValue = targetScore.toFloat(),
            animationSpec = AnimationSpecs.ScoreCountSpec
        ) { value, _ ->
            displayScore = value.toInt()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$displayScore",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = DynaPuffCondensedFamily,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun PointsPopup(
    points: Int,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    val offsetY = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(visible) {
        launch {
            offsetY.animateTo(
                -24f,
                AnimationSpecs.PointsPopupOffsetSpec
            )
        }
        launch {
            alpha.animateTo(
                0f,
                AnimationSpecs.PointsPopupFadeSpec
            )
        }
    }

    Text(
        text = "+$points",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .offset(y = offsetY.value.dp)
            .alpha(alpha.value)
    )
}
