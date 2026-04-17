package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.GameAnswerRed
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GameGreen
import com.alvaroquintana.domain.challenge.ChallengeDifficulty
import com.alvaroquintana.domain.challenge.ChallengeCompletionResult
import com.alvaroquintana.domain.challenge.DailyChallenge
import kotlinx.coroutines.delay

/**
 * Banner inline que se muestra en ResultScreen cuando el jugador completo
 * uno o mas desafios durante la partida.
 *
 * NO es un dialog: es contenido inline que se integra en el flujo de la pantalla.
 * Aparece con animacion de slide desde abajo + fade.
 * Usa el sistema de colores de AdivinaBandera (Material3 + GameColors).
 */
@Composable
fun ChallengeCompletionBanner(
    completionResult: ChallengeCompletionResult,
    modifier: Modifier = Modifier
) {
    // Animacion de entrada retrasada para que no compita con la animacion del score
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(400)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(400)
        ) + fadeIn(animationSpec = tween(400))
    ) {
        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            // Header del banner
            BannerHeader()

            Spacer(modifier = Modifier.height(8.dp))

            // Filas de desafios completados
            completionResult.completedChallenges.forEach { challenge ->
                CompletedChallengeItem(challenge = challenge)
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Bonus por completar todos los diarios
            if (completionResult.allDailyJustCompleted) {
                Spacer(modifier = Modifier.height(4.dp))
                AllDailyBonusItem()
            }
        }
    }
}

@Composable
private fun BannerHeader() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = GameGreen.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.challenges_completed),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                fontFamily = DynaPuffFamily,
                color = GameGreen
            )
        }
    }
}

@Composable
private fun CompletedChallengeItem(challenge: DailyChallenge) {
    val difficultyColor = when (challenge.difficulty) {
        ChallengeDifficulty.EASY -> GameGreen
        ChallengeDifficulty.MEDIUM -> GameGold
        ChallengeDifficulty.HARD -> GameAnswerRed
        ChallengeDifficulty.WEEKLY -> Color(0xFF1A237E) // GameBlue para semanal
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icono de check
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(GameGreen.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = GameGreen,
                    modifier = Modifier.size(12.dp)
                )
            }

            // Descripcion
            Text(
                text = challenge.description,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = DynaPuffSemiCondensedFamily,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )

            // Recompensa
            Column(horizontalAlignment = Alignment.End) {
                if (challenge.reward.xp > 0) {
                    Text(
                        text = "+${challenge.reward.xp} XP",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        fontFamily = DynaPuffSemiCondensedFamily,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                if (challenge.reward.coins > 0) {
                    Text(
                        text = "+${challenge.reward.coins}\uD83E\uDE99",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = DynaPuffSemiCondensedFamily,
                        color = GameGold
                    )
                }
            }
        }
    }
}

@Composable
private fun AllDailyBonusItem() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(text = "\uD83C\uDF89", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = stringResource(R.string.bonus_complete_all),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    fontFamily = DynaPuffFamily,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "+75 XP",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    fontFamily = DynaPuffSemiCondensedFamily,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "+15\uD83E\uDE99",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = DynaPuffSemiCondensedFamily,
                    color = GameGold
                )
            }
        }
    }
}
