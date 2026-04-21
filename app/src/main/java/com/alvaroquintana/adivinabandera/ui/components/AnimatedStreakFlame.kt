package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp

/**
 * Animated flame that visually scales and pulses based on the current streak.
 *
 * Tiers:
 *  - 0    : invisible (no rendering)
 *  - 1–2  : single static flame
 *  - 3–6  : single flame, slow pulse
 *  - 7–13 : double flame, medium pulse
 *  - 14–29: triple flame, fast pulse
 *  - 30+  : rainbow + flame, fastest pulse
 */
@Composable
fun AnimatedStreakFlame(
    currentStreak: Int,
    modifier: Modifier = Modifier
) {
    if (currentStreak < 1) return

    val baseScale = when {
        currentStreak < 3 -> 1f
        currentStreak < 7 -> 1.1f
        currentStreak < 14 -> 1.2f
        else -> 1.3f
    }

    val pulseDuration = when {
        currentStreak < 3 -> 0
        currentStreak < 7 -> 2000
        currentStreak < 14 -> 1500
        else -> 1000
    }

    val pulseScale = if (pulseDuration > 0) {
        val infiniteTransition = rememberInfiniteTransition(label = "flame")
        val animated by infiniteTransition.animateFloat(
            initialValue = baseScale * 0.9f,
            targetValue = baseScale * 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(pulseDuration, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flamePulse"
        )
        animated
    } else {
        baseScale
    }

    val flameText = when {
        currentStreak < 7 -> "\uD83D\uDD25"
        currentStreak < 14 -> "\uD83D\uDD25\uD83D\uDD25"
        currentStreak < 30 -> "\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25"
        else -> "\uD83C\uDF08\uD83D\uDD25"
    }

    Text(
        text = flameText,
        fontSize = 22.sp,
        modifier = modifier.graphicsLayer {
            scaleX = pulseScale
            scaleY = pulseScale
        }
    )
}
