package com.alvaroquintana.adivinabandera.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing

/**
 * Material 3 Expressive motion tokens.
 * Source: https://m3.material.io/styles/motion/easing-and-duration/tokens-specs
 *
 * Usage:
 *   tween(durationMillis = MotionTokens.DurationMedium2, easing = MotionTokens.EmphasizedDecelerate)
 */
object MotionTokens {

    // Duration tokens (ms) ─────────────────────────────────────────────────────
    const val DurationShort1 = 50
    const val DurationShort2 = 100
    const val DurationShort3 = 150
    const val DurationShort4 = 200

    const val DurationMedium1 = 250
    const val DurationMedium2 = 300
    const val DurationMedium3 = 350
    const val DurationMedium4 = 400

    const val DurationLong1 = 450
    const val DurationLong2 = 500
    const val DurationLong3 = 550
    const val DurationLong4 = 600

    const val DurationExtraLong1 = 700
    const val DurationExtraLong2 = 800
    const val DurationExtraLong3 = 900
    const val DurationExtraLong4 = 1000

    // Easing tokens ────────────────────────────────────────────────────────────
    /** Emphasized — default for most on-screen transitions. */
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Emphasized Decelerate — incoming elements (enters). */
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Emphasized Accelerate — outgoing elements (exits). */
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    /** Standard — simple utilitarian motion. */
    val Standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    /** Standard Decelerate. */
    val StandardDecelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f)

    /** Standard Accelerate. */
    val StandardAccelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f)

    /** Linear — for continuous effects (progress, carousels). */
    val Linear: Easing = LinearEasing
}
