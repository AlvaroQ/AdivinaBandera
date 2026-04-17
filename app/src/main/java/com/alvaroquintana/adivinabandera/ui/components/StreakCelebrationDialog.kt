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
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.theme.DarkSurfaceVar
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GameGreen
import com.alvaroquintana.adivinabandera.ui.theme.GameRed
import com.alvaroquintana.domain.StreakCheckResult

/**
 * Dialog de celebracion que se muestra en ResultScreen cuando la racha
 * cambia de estado (continua, se salva, se rompe, o es nueva).
 *
 * Stateless: no se muestra para AlreadyPlayedToday.
 * El contenido varia segun el tipo de StreakCheckResult recibido.
 */
@Composable
fun StreakCelebrationDialog(
    streakCheckResult: StreakCheckResult,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dialog_glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "icon_scale"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Continuar",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (streakCheckResult) {
                    is StreakCheckResult.StreakContinued -> {
                        StreakContinuedContent(
                            result = streakCheckResult,
                            glowScale = glowScale
                        )
                    }
                    is StreakCheckResult.StreakSavedByFreeze -> {
                        StreakSavedContent(
                            result = streakCheckResult,
                            glowScale = glowScale
                        )
                    }
                    is StreakCheckResult.StreakBroken -> {
                        StreakBrokenContent(
                            result = streakCheckResult,
                            glowScale = glowScale
                        )
                    }
                    is StreakCheckResult.NewStreak -> {
                        NewStreakContent(glowScale = glowScale)
                    }
                    is StreakCheckResult.AlreadyPlayedToday -> {
                        // No deberia llegar aqui — el llamador debe filtrar este caso
                    }
                }
            }
        }
    )
}

// ─────────────────────── VARIANTES DE CONTENIDO ──────────────────────────────

@Composable
private fun StreakContinuedContent(
    result: StreakCheckResult.StreakContinued,
    glowScale: Float
) {
    val state = result.newState
    val reward = result.reward
    val isMilestone = reward.isMilestone

    // Icono principal
    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(glowScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(GameGold.copy(alpha = 0.25f), Color.Transparent)
                )
            )
            .drawBehind {
                drawCircle(
                    color = GameGold.copy(alpha = 0.15f),
                    radius = size.minDimension / 1.5f
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (isMilestone) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = GameGold,
                modifier = Modifier.size(44.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Whatshot,
                contentDescription = null,
                tint = GameGold,
                modifier = Modifier.size(44.dp)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val titulo = if (isMilestone) {
        "${reward.milestoneDay} dias seguidos"
    } else {
        "Racha de ${state.currentStreak} dias"
    }

    Text(
        text = if (isMilestone) "Hito alcanzado" else "Racha continua",
        fontFamily = DynaPuffSemiCondensedFamily,
        style = MaterialTheme.typography.labelLarge,
        color = GameGold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = titulo,
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = GameGold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    if (reward.xpBonus > 0) {
        XpBonusBadge(xpBonus = reward.xpBonus, multiplier = reward.streakMultiplier)
        Spacer(modifier = Modifier.height(12.dp))
    }

    if (reward.freezeTokens > 0) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "❄", fontSize = 18.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "+${reward.freezeTokens} token${if (reward.freezeTokens > 1) "s" else ""} de congelamiento",
                fontFamily = DynaPuffSemiCondensedFamily,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    CycleProgressDotsCompact(
        cycleDay = state.cycleDay,
        completed = true
    )
}

@Composable
private fun StreakSavedContent(
    result: StreakCheckResult.StreakSavedByFreeze,
    glowScale: Float
) {
    val state = result.newState
    val reward = result.reward

    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(glowScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "❄", fontSize = 40.sp)
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Racha salvada",
        fontFamily = DynaPuffSemiCondensedFamily,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "${state.currentStreak} dias",
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Usaste un token de congelamiento",
        fontFamily = DynaPuffSemiCondensedFamily,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )

    if (state.freezeTokens > 0) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Tokens restantes: ${state.freezeTokens} ❄",
            fontFamily = DynaPuffSemiCondensedFamily,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }

    if (reward.xpBonus > 0) {
        Spacer(modifier = Modifier.height(12.dp))
        XpBonusBadge(xpBonus = reward.xpBonus, multiplier = reward.streakMultiplier)
    }

    Spacer(modifier = Modifier.height(12.dp))

    CycleProgressDotsCompact(
        cycleDay = state.cycleDay,
        completed = true
    )
}

@Composable
private fun StreakBrokenContent(
    result: StreakCheckResult.StreakBroken,
    glowScale: Float
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(glowScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(GameRed.copy(alpha = 0.2f), Color.Transparent)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "\uD83D\uDC94", fontSize = 40.sp) // emoji corazon roto
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Racha perdida",
        fontFamily = DynaPuffSemiCondensedFamily,
        style = MaterialTheme.typography.labelLarge,
        color = GameRed,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Racha anterior: ${result.previousStreak} dias",
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(GameGreen.copy(alpha = 0.12f))
            .border(
                width = 1.dp,
                color = GameGreen.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "Nueva racha iniciada",
            fontFamily = DynaPuffFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = GameGreen,
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Juga todos los dias para construir tu racha",
        fontFamily = DynaPuffSemiCondensedFamily,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun NewStreakContent(glowScale: Float) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .scale(glowScale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(GameGreen.copy(alpha = 0.25f), Color.Transparent)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "\uD83C\uDF89", fontSize = 40.sp) // emoji cohete/fiesta
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Primera racha",
        fontFamily = DynaPuffSemiCondensedFamily,
        style = MaterialTheme.typography.labelLarge,
        color = GameGreen,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(4.dp))

    Text(
        text = "Dia 1",
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = GameGreen,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Juga cada dia para ganar recompensas",
        fontFamily = DynaPuffSemiCondensedFamily,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GameGreen.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = GameGreen.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = "7 dias seguidos = token ❄ + bonus XP",
            fontFamily = DynaPuffSemiCondensedFamily,
            style = MaterialTheme.typography.labelSmall,
            color = GameGreen.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ────────────────────────── COMPONENTES COMPARTIDOS ──────────────────────────

@Composable
private fun XpBonusBadge(xpBonus: Int, multiplier: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "xp_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        GameGold.copy(alpha = 0.12f),
                        GameGold.copy(alpha = 0.08f)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        GameGold.copy(alpha = glowAlpha),
                        GameGold.copy(alpha = glowAlpha * 0.7f)
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = "\u2B50", fontSize = 18.sp) // estrella
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "+$xpBonus XP",
            fontFamily = DynaPuffFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = GameGold
        )
        if (multiplier > 1f) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "x${String.format("%.1f", multiplier)}",
                fontFamily = DynaPuffSemiCondensedFamily,
                style = MaterialTheme.typography.bodySmall,
                color = GameGold.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun CycleProgressDotsCompact(
    cycleDay: Int,
    completed: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ciclo:",
            fontFamily = DynaPuffSemiCondensedFamily,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        for (day in 1..7) {
            val isFilled = if (completed) day <= cycleDay else day < cycleDay
            val isCurrent = day == cycleDay

            Box(
                modifier = Modifier
                    .size(if (isCurrent) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFilled) GameGold
                        else Color.Transparent
                    )
                    .border(
                        width = 1.dp,
                        color = if (isFilled) GameGold
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }
    }
}
