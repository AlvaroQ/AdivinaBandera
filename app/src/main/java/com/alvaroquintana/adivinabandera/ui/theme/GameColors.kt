package com.alvaroquintana.adivinabandera.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class GameColors(
    val correctAnswer: Color,
    val correctContainer: Color,
    val onCorrectContainer: Color,
    val wrongAnswer: Color,
    val wrongContainer: Color,
    val onWrongContainer: Color,
    val streakGold: Color,
    val streakGoldContainer: Color,
    val timerWarning: Color,
    val timerCritical: Color
)

val LightGameColors = GameColors(
    correctAnswer      = GameGreen,
    correctContainer   = Color(0xFFD4EFDF),
    onCorrectContainer = Color(0xFF1B5E20),
    wrongAnswer        = GameAnswerRed,
    wrongContainer     = Color(0xFFFFE0E0),
    onWrongContainer   = Color(0xFFB71C1C),
    streakGold         = GameGold,
    streakGoldContainer = Color(0xFFFFF3CD),
    timerWarning       = Color(0xFF1565C0),   // azul advertencia
    timerCritical      = GameAnswerRed
)

val DarkGameColors = GameColors(
    correctAnswer      = Color(0xFF7DDFCA),
    correctContainer   = Color(0xFF003D2D),
    onCorrectContainer = Color(0xFFC8F7E8),
    wrongAnswer        = Color(0xFFFF6B6B),
    wrongContainer     = Color(0xFF5C0000),
    onWrongContainer   = Color(0xFFFFE0E0),
    streakGold         = Color(0xFFFFD54F),
    streakGoldContainer = Color(0xFF5C4400),
    timerWarning       = Color(0xFF82B1FF),   // azul claro advertencia oscuro
    timerCritical      = Color(0xFFFF6B6B)
)

val LocalGameColors = staticCompositionLocalOf { LightGameColors }
