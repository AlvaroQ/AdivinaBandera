package com.alvaroquintana.adivinabandera.ui.animation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import com.alvaroquintana.adivinabandera.ui.theme.MotionTokens

/**
 * Navigation transitions following Material 3 Expressive motion patterns.
 *
 * Pattern reference: https://m3.material.io/styles/motion/transitions/transition-patterns
 * - **Shared axis X**: lateral navigation (forward/back) between peers.
 * - **Shared axis Z**: hierarchical navigation (push/pop) — scale + fade.
 * - **Container transform**: element-to-container morph (e.g. card → detail).
 * - **Fade through**: transitions between unrelated content (e.g. result screen).
 */
object NavTransitions {

    // ── Shared Axis X (default forward navigation) ─────────────────────────────
    val enterTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth / 10 },
        animationSpec = tween(
            durationMillis = MotionTokens.DurationLong2,
            easing = MotionTokens.EmphasizedDecelerate
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = MotionTokens.DurationMedium2,
            delayMillis = MotionTokens.DurationShort2,
            easing = MotionTokens.Emphasized
        )
    )

    val exitTransition: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 10 },
        animationSpec = tween(
            durationMillis = MotionTokens.DurationLong2,
            easing = MotionTokens.EmphasizedAccelerate
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = MotionTokens.DurationShort4,
            easing = MotionTokens.EmphasizedAccelerate
        )
    )

    val popEnterTransition: EnterTransition = slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 10 },
        animationSpec = tween(
            durationMillis = MotionTokens.DurationLong2,
            easing = MotionTokens.EmphasizedDecelerate
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = MotionTokens.DurationMedium2,
            delayMillis = MotionTokens.DurationShort2,
            easing = MotionTokens.Emphasized
        )
    )

    val popExitTransition: ExitTransition = slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth / 10 },
        animationSpec = tween(
            durationMillis = MotionTokens.DurationLong2,
            easing = MotionTokens.EmphasizedAccelerate
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = MotionTokens.DurationShort4,
            easing = MotionTokens.EmphasizedAccelerate
        )
    )

    // ── Container transform (Result / hero morph) ──────────────────────────────
    val resultEnterTransition: EnterTransition = scaleIn(
        initialScale = 0.88f,
        animationSpec = tween(
            durationMillis = MotionTokens.DurationExtraLong1,
            easing = MotionTokens.EmphasizedDecelerate
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = MotionTokens.DurationLong2,
            easing = MotionTokens.Emphasized
        )
    )

    val resultExitTransition: ExitTransition = scaleOut(
        targetScale = 0.92f,
        animationSpec = tween(
            durationMillis = MotionTokens.DurationLong1,
            easing = MotionTokens.EmphasizedAccelerate
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = MotionTokens.DurationMedium1,
            easing = MotionTokens.EmphasizedAccelerate
        )
    )

    // ── Fade through (unrelated content) ───────────────────────────────────────
    val fadeEnterTransition: EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = MotionTokens.DurationMedium2,
            delayMillis = MotionTokens.DurationShort3,
            easing = MotionTokens.Emphasized
        )
    )

    val fadeExitTransition: ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = MotionTokens.DurationShort3,
            easing = MotionTokens.EmphasizedAccelerate
        )
    )
}
