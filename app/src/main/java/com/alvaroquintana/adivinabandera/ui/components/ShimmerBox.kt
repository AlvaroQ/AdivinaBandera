package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

/**
 * Placeholder animado (shimmer) para usarse mientras se carga un dato.
 * El [modifier] define el tamaño — el caller debe darle las mismas dimensiones
 * que el contenido final para evitar layout jumps.
 */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val baseColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val highlightColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.20f)

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(shimmerTranslate - 300f, 0f),
        end = Offset(shimmerTranslate, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(brush)
    )
}

