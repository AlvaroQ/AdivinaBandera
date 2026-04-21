package com.alvaroquintana.adivinabandera.ui.select.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Card para un modo regional del chain. Muestra:
 * - Bandera grande (emoji Unicode) como avatar
 * - Título (nombre del país)
 * - Subtítulo con estado:
 *   - Desbloqueado: "Acertá 6 para desbloquear el siguiente" + progreso hacia el próximo
 *   - Bloqueado: "Desbloqueá con X/6 aciertos en [previo]" + progreso del prerequisito
 * - Chevron (desbloqueado) o Lock (bloqueado)
 */
@Composable
fun RegionalModeCard(
    flagEmoji: String,
    title: String,
    subtitle: String,
    theme: CardTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    isNearUnlock: Boolean = false,
    progress: Float = 1f,
    progressLabel: String? = null,
    accentColor: Color = Color.Gray,
    isCompleted: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_regional_$title")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha_regional_$title"
    )

    val effectiveBorderColor = if (isNearUnlock) accentColor.copy(alpha = glowAlpha) else theme.borderColor
    val effectiveBorderWidth = if (isNearUnlock) 2.dp else 1.dp

    Surface(
        onClick = if (isLocked) { {} } else onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = theme.cardBg,
        shadowElevation = if (theme.isDark) 0.dp else 4.dp,
        border = BorderStroke(effectiveBorderWidth, effectiveBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (isLocked) theme.borderColor.copy(alpha = 0.4f)
                        else Color.White.copy(alpha = 0.0f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = flagEmoji,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = theme.textPrimary.copy(alpha = if (isLocked) 0.7f else 1f)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = theme.textMuted.copy(alpha = if (isLocked) 0.7f else 1f)
                )
                if (progressLabel != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = progressLabel,
                            fontSize = 11.sp,
                            color = if (isLocked) theme.textMuted else accentColor,
                            fontWeight = FontWeight.Medium
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isLocked) theme.textMuted else accentColor,
                            trackColor = theme.borderColor.copy(alpha = 0.4f),
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            when {
                isLocked -> Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = theme.textMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
                isCompleted -> Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
                else -> Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = theme.chevronColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
