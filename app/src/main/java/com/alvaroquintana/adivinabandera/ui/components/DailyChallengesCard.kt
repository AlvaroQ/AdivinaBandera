package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GameGreen
import com.alvaroquintana.adivinabandera.ui.theme.GameAnswerRed
import com.alvaroquintana.domain.challenge.ChallengeDifficulty
import com.alvaroquintana.domain.challenge.ChallengeReward
import com.alvaroquintana.domain.challenge.DailyChallenge
import com.alvaroquintana.domain.challenge.DailyChallengeState

/**
 * Tarjeta de desafios diarios para SelectScreen.
 *
 * Muestra los 3 desafios diarios y el desafio semanal opcional.
 * Es completamente stateless: recibe DailyChallengeState del ViewModel.
 * Usa el sistema de colores de AdivinaBandera (Material3 + GameColors).
 */
@Composable
fun DailyChallengesCard(
    challengeState: DailyChallengeState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Header
            ChallengesHeader(
                date = challengeState.date,
                allCompleted = challengeState.allDailyCompleted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Desafios diarios
            if (challengeState.challenges.isEmpty()) {
                Text(
                    text = stringResource(R.string.loading_challenges),
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = DynaPuffFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                challengeState.challenges.forEachIndexed { index, challenge ->
                    ChallengeRow(challenge = challenge)
                    if (index < challengeState.challenges.lastIndex) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }

            // Banner todos completados
            if (challengeState.allDailyCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                AllCompletedBanner()
            }

            // Desafio semanal
            challengeState.weeklyChallenge?.let { weekly ->
                Spacer(modifier = Modifier.height(12.dp))
                WeeklyChallengeSection(challenge = weekly)
            }
        }
    }
}

@Composable
private fun ChallengesHeader(date: String, allCompleted: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.daily_challenges_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            fontFamily = DynaPuffFamily,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (allCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = GameGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = if (date.isNotEmpty()) formatDisplayDate(date) else "",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = DynaPuffSemiCondensedFamily,
                color = if (allCompleted) GameGreen else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChallengeRow(challenge: DailyChallenge) {
    val difficultyColor = difficultyColor(challenge.difficulty)
    val progressFraction = if (challenge.targetValue > 0) {
        (challenge.currentProgress.toFloat() / challenge.targetValue.toFloat()).coerceIn(0f, 1f)
    } else 0f

    var animatedProgress by remember(challenge.id) { mutableFloatStateOf(0f) }
    val progressAnimation by animateFloatAsState(
        targetValue = animatedProgress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "challenge_progress_${challenge.id}"
    )

    LaunchedEffect(progressFraction) {
        animatedProgress = progressFraction
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Badge de dificultad
            DifficultyBadge(difficulty = challenge.difficulty, color = difficultyColor)

            // Descripcion del desafio
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = DynaPuffSemiCondensedFamily,
                color = if (challenge.isCompleted)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2
            )

            // Estado: check o recompensa
            if (challenge.isCompleted) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(GameGreen.copy(alpha = 0.15f))
                        .border(1.dp, GameGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completado",
                        tint = GameGreen,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                RewardLabel(reward = challenge.reward)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Barra de progreso
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { progressAnimation },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (challenge.isCompleted) GameGreen else difficultyColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Text(
                text = "${challenge.currentProgress}/${challenge.targetValue}",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = DynaPuffSemiCondensedFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DifficultyBadge(difficulty: ChallengeDifficulty, color: Color) {
    val label = when (difficulty) {
        ChallengeDifficulty.EASY -> "F"
        ChallengeDifficulty.MEDIUM -> "M"
        ChallengeDifficulty.HARD -> "D"
        ChallengeDifficulty.WEEKLY -> "S"
    }

    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            fontFamily = DynaPuffFamily,
            color = color
        )
    }
}

@Composable
private fun RewardLabel(reward: ChallengeReward) {
    Column(horizontalAlignment = Alignment.End) {
        if (reward.xp > 0) {
            Text(
                text = "+${reward.xp}XP",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                fontFamily = DynaPuffSemiCondensedFamily,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        if (reward.coins > 0) {
            Text(
                text = "+${reward.coins}\uD83E\uDE99",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = DynaPuffSemiCondensedFamily,
                color = GameGold
            )
        }
    }
}

@Composable
private fun AllCompletedBanner() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = GameGreen.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.all_completed),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                fontFamily = DynaPuffFamily,
                color = GameGreen
            )
            Text(
                text = "+75XP +15\uD83E\uDE99",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = DynaPuffSemiCondensedFamily,
                color = GameGreen.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun WeeklyChallengeSection(challenge: DailyChallenge) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Separador con etiqueta semanal
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = stringResource(R.string.weekly_label),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    fontFamily = DynaPuffFamily,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Fila del desafio semanal
        ChallengeRow(challenge = challenge)
    }
}

// ─── Utilidades ─────────────────────────────────────────────────────────────

private fun difficultyColor(difficulty: ChallengeDifficulty): Color = when (difficulty) {
    ChallengeDifficulty.EASY -> GameGreen
    ChallengeDifficulty.MEDIUM -> GameGold
    ChallengeDifficulty.HARD -> GameAnswerRed
    ChallengeDifficulty.WEEKLY -> Color(0xFF1A237E) // GameBlue (deep blue para semanal)
}

private fun formatDisplayDate(dateStr: String): String {
    // Entrada: "yyyy-MM-dd" → Salida: "16 abr"
    return try {
        val parts = dateStr.split("-")
        if (parts.size < 3) return dateStr
        val day = parts[2].trimStart('0').ifEmpty { "0" }
        val month = when (parts[1]) {
            "01" -> "ene"; "02" -> "feb"; "03" -> "mar"; "04" -> "abr"
            "05" -> "may"; "06" -> "jun"; "07" -> "jul"; "08" -> "ago"
            "09" -> "sep"; "10" -> "oct"; "11" -> "nov"; "12" -> "dic"
            else -> parts[1]
        }
        "$day $month"
    } catch (_: Exception) {
        dateStr
    }
}
