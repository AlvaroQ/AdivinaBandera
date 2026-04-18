package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.DarkAccent
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GameGreen
import com.alvaroquintana.domain.StreakState

/**
 * Widget compacto que muestra el estado actual de la racha diaria del jugador.
 *
 * Se usa en SelectScreen como elemento visual prominente.
 * Stateless: recibe datos del ViewModel y no maneja estado interno.
 */
@Composable
fun StreakWidget(
    streakState: StreakState,
    isAtRisk: Boolean,
    hasPlayedToday: Boolean,
    modifier: Modifier = Modifier
) {
    // Animacion de pulso para el borde cuando la racha esta en riesgo
    val riskBorderAlpha by if (isAtRisk) {
        val transition = rememberInfiniteTransition(label = "streak_risk")
        transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "risk_pulse"
        )
    } else {
        val transition = rememberInfiniteTransition(label = "streak_idle")
        transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "idle_border"
        )
    }

    val borderColor = when {
        hasPlayedToday -> GameGreen
        isAtRisk -> GameGold.copy(alpha = riskBorderAlpha)
        else -> DarkAccent.copy(alpha = 0.5f)
    }

    val glowColor = when {
        hasPlayedToday -> GameGreen.copy(alpha = 0.25f)
        isAtRisk -> GameGold.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(
                width = if (isAtRisk) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Fila superior: icono de fuego + dias + estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Icono de fuego + contador de dias
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        GameGold.copy(alpha = 0.25f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .drawBehind {
                                drawCircle(
                                    color = GameGold.copy(alpha = 0.12f),
                                    radius = size.minDimension / 1.5f
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = null,
                            tint = if (streakState.currentStreak > 0) GameGold
                            else Color.Black.copy(alpha = 0.4f),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = formatStreakDayCount(streakState.currentStreak),
                            fontFamily = DynaPuffFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (streakState.currentStreak > 0) GameGold
                            else Color.Black.copy(alpha = 0.5f)
                        )
                        Text(
                            text = stringResource(R.string.streak_widget_current_label),
                            fontFamily = DynaPuffSemiCondensedFamily,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }

                // Estado: ya jugo hoy / en riesgo / freeze tokens
                Column(horizontalAlignment = Alignment.End) {
                    when {
                        hasPlayedToday -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GameGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = stringResource(R.string.streak_widget_played_today),
                                    fontFamily = DynaPuffSemiCondensedFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GameGreen
                                )
                            }
                        }
                        isAtRisk -> {
                            Text(
                                text = stringResource(R.string.streak_widget_at_risk),
                                fontFamily = DynaPuffSemiCondensedFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = GameGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        else -> {
                            Text(
                                text = stringResource(R.string.streak_widget_play_today),
                                fontFamily = DynaPuffSemiCondensedFamily,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Freeze tokens si tiene
                    if (streakState.freezeTokens > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(minOf(streakState.freezeTokens, 3)) {
                                Text(
                                    text = "❄",
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 1.dp)
                                )
                            }
                            if (streakState.freezeTokens > 3) {
                                Text(
                                    text = stringResource(
                                        R.string.streak_widget_extra_tokens,
                                        streakState.freezeTokens - 3
                                    ),
                                    fontFamily = DynaPuffSemiCondensedFamily,
                                    style = MaterialTheme.typography.labelSmall,
                                     color = Color.Black.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Ciclo de 7 dias — puntos de progreso
            CycleProgressDots(
                cycleDay = streakState.cycleDay,
                hasPlayedToday = hasPlayedToday
            )

            // Mejor racha si es > 0
            if (streakState.bestStreak > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.streak_widget_best_streak,
                        formatStreakDayCount(streakState.bestStreak)
                    ),
                    fontFamily = DynaPuffSemiCondensedFamily,
                    style = MaterialTheme.typography.labelSmall,
                    color = GameGold.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun formatStreakDayCount(dayCount: Int): String = stringResource(
    id = if (dayCount == 1) R.string.streak_day_count_singular
    else R.string.streak_day_count_plural,
    formatArgs = arrayOf(dayCount)
)

/**
 * Fila de 7 puntos que visualiza el progreso del ciclo semanal.
 * Los dias completados se muestran rellenos, el dia actual resaltado,
 * y los dias restantes como contorno.
 */
@Composable
private fun CycleProgressDots(
    cycleDay: Int,
    hasPlayedToday: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (day in 1..7) {
            val isCompleted = day < cycleDay || (day == cycleDay && hasPlayedToday)
            val isCurrent = day == cycleDay && !hasPlayedToday
            val dotColor = when {
                isCompleted -> GameGold
                isCurrent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            }

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted || isCurrent) dotColor
                        else Color.Transparent
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (isCompleted) dotColor
                        else if (isCurrent) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .then(
                        if (isCurrent) Modifier.drawBehind {
                            drawCircle(
                                color = dotColor.copy(alpha = 0.3f),
                                radius = size.minDimension
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}
