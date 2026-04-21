package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.managers.DailyRewardManager
import com.alvaroquintana.adivinabandera.ui.theme.DarkGeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.DarkSurface
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.GeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme

private val TierCommonColor = Color(0xFF3B82F6)     // azul
private val TierUncommonColor = Color(0xFF7C3AED)   // violeta
private val TierRareColor = Color(0xFFF59E0B)        // dorado

private val TierCommonLight = Color(0xFFBFDBFE)
private val TierUncommonLight = Color(0xFFEDE9FE)
private val TierRareLight = Color(0xFFFEF3C7)

private fun tierColor(tier: DailyRewardManager.RewardTier): Color = when (tier) {
    DailyRewardManager.RewardTier.COMMON -> TierCommonColor
    DailyRewardManager.RewardTier.UNCOMMON -> TierUncommonColor
    DailyRewardManager.RewardTier.RARE -> TierRareColor
}

private fun tierColorLight(tier: DailyRewardManager.RewardTier): Color = when (tier) {
    DailyRewardManager.RewardTier.COMMON -> TierCommonLight
    DailyRewardManager.RewardTier.UNCOMMON -> TierUncommonLight
    DailyRewardManager.RewardTier.RARE -> TierRareLight
}

/**
 * Tarjeta de recompensa diaria para SelectScreen.
 *
 * - [reward] null: estado de carga (shimmer)
 * - [reward.isClaimed] false: muestra boton "RECLAMAR" con shimmer en el borde
 * - [reward.isClaimed] true: muestra XP + monedas reclamados con color del tier
 *
 * [onClaim] se dispara cuando el usuario toca el boton RECLAMAR.
 */
@Composable
fun DailyRewardCard(
    reward: DailyRewardManager.DailyReward?,
    onClaim: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) DarkSurface else Color.White
    val borderColor = if (isDark) DarkGeoBorder else GeoBorder

    if (reward == null) {
        // Estado de carga
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(76.dp)
                .then(
                    if (isDark) Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                    else Modifier
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardBg)
                )
        ) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(76.dp))
        }
        return
    }

    AnimatedContent(
        targetState = reward.isClaimed,
        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
        label = "daily_reward_state"
    ) { isClaimed ->
        if (!isClaimed) {
            UnclaimedRewardCard(
                reward = reward,
                onClaim = onClaim,
                cardBg = cardBg,
                borderColor = borderColor,
                isDark = isDark,
                modifier = modifier
            )
        } else {
            ClaimedRewardCard(
                reward = reward,
                cardBg = cardBg,
                borderColor = borderColor,
                isDark = isDark,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun UnclaimedRewardCard(
    reward: DailyRewardManager.DailyReward,
    onClaim: () -> Unit,
    cardBg: Color,
    borderColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "reward_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "reward_shimmer_alpha"
    )

    val accentColor = tierColor(reward.tier)
    val glowBorder = accentColor.copy(alpha = shimmerAlpha)

    Surface(
        onClick = onClaim,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        border = androidx.compose.foundation.BorderStroke(2.dp, glowBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icono regalo con gradient del tier
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(accentColor, accentColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\uD83C\uDF81", fontSize = 22.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.daily_bonus),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDark) Color(0xFFD0DAEA) else Color(0xFF1A1A2E)
                )
                Text(
                    text = stringResource(R.string.claim_reward_description),
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF636E80) else Color(0xFF9CA3AF)
                )
            }

            // Boton RECLAMAR con shimmer
            val shimmerTranslate by rememberInfiniteTransition(label = "btn_shimmer")
                .animateFloat(
                    initialValue = -200f,
                    targetValue = 600f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "btn_shimmer_x"
                )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                accentColor,
                                accentColor.copy(alpha = 0.8f),
                                Color.White.copy(alpha = 0.3f),
                                accentColor.copy(alpha = 0.8f),
                                accentColor
                            ),
                            start = Offset(shimmerTranslate, 0f),
                            end = Offset(shimmerTranslate + 200f, 60f)
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.claim_button),
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun ClaimedRewardCard(
    reward: DailyRewardManager.DailyReward,
    cardBg: Color,
    borderColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val accentColor = tierColor(reward.tier)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) cardBg else Color(0xFFF8FAFF),
        shadowElevation = if (isDark) 0.dp else 2.dp,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isDark) 1.dp else 1.5.dp,
            color = if (isDark) accentColor.copy(alpha = 0.45f) else accentColor.copy(alpha = 0.75f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(accentColor.copy(alpha = if (isDark) 0.15f else 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "\u2705", fontSize = 22.sp)
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.bonus_claimed),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isDark) Color(0xFFD0DAEA) else Color(0xFF0F172A)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "+${reward.xpAmount} XP",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                    Text(
                        text = "+${reward.coinsAmount} \uD83E\uDE99",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            Text(
                text = stringResource(R.string.come_back_tomorrow),
                fontSize = 11.sp,
                color = if (isDark) Color(0xFF636E80) else Color(0xFF475569)
            )
        }
    }
}
