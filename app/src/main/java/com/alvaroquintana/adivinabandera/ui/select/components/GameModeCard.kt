package com.alvaroquintana.adivinabandera.ui.select.components

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CardTheme(
    val cardBg: Color,
    val borderColor: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val chevronColor: Color,
    val isDark: Boolean
)

@Composable
fun GameModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconGradient: Brush,
    theme: CardTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    isNearUnlock: Boolean = false,
    unlockProgress: Float = 1f,
    unlockLevel: Int = 0,
    currentLevel: Int = 0,
    accentColor: Color = Color.Gray
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_$title")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha_$title"
    )

    val effectiveBorderColor = if (isNearUnlock) accentColor.copy(alpha = glowAlpha) else theme.borderColor
    val effectiveBorderWidth = if (isNearUnlock) 2.dp else 1.dp

    Surface(
        onClick = if (isLocked) { {} } else onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = theme.cardBg,
        shadowElevation = if (theme.isDark) 0.dp else 4.dp,
        border = androidx.compose.foundation.BorderStroke(effectiveBorderWidth, effectiveBorderColor)
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
                    .background(iconGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = if (isLocked) 0.6f else 1f),
                    modifier = Modifier.size(24.dp)
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
                if (isLocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    UnlockProgressRow(
                        currentLevel = currentLevel,
                        unlockLevel = unlockLevel,
                        unlockProgress = unlockProgress,
                        accentColor = accentColor
                    )
                }
            }

            if (isLocked) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = theme.textMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = theme.chevronColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
internal fun UnlockProgressRow(
    currentLevel: Int,
    unlockLevel: Int,
    unlockProgress: Float,
    accentColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = "Nivel $currentLevel/$unlockLevel",
            fontSize = 11.sp,
            color = accentColor,
            fontWeight = FontWeight.Medium
        )
        LinearProgressIndicator(
            progress = { unlockProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = accentColor,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round
        )
    }
}
