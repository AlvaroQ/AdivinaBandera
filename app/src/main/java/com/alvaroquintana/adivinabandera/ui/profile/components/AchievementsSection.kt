package com.alvaroquintana.adivinabandera.ui.profile.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.ElevationTokens
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoftLight
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme
import com.alvaroquintana.adivinabandera.ui.theme.isExpanded
import com.alvaroquintana.domain.Achievement

@Composable
internal fun AchievementsSection(
    allAchievements: List<Achievement>,
    unlockedAchievements: List<Achievement>,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isAppInDarkTheme()) MaterialTheme.colorScheme.primary else GeoNavySoftLight
    val unlockedIds = unlockedAchievements.map { it.id }.toSet()
    val unlockedCount = unlockedAchievements.size
    val windowSize = LocalWindowSizeClass.current
    val columnsCount = if (windowSize.isExpanded) 4 else 3

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.Level1)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = GameGold,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Logros",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "$unlockedCount/${allAchievements.size}",
                            fontFamily = DynaPuffSemiCondensedFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = accentColor.copy(alpha = 0.16f),
                        labelColor = accentColor
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            val rows = allAchievements.chunked(columnsCount)
            rows.forEachIndexed { rowIndex, rowItems ->
                val isLastRow = rowIndex == rows.lastIndex
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowItems.forEach { achievement ->
                        AchievementItem(
                            achievement = achievement,
                            isUnlocked = unlockedIds.contains(achievement.id),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                    repeat(columnsCount - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                if (!isLastRow) Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun AchievementItem(
    achievement: Achievement,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    if (isUnlocked) {
        UnlockedAchievementItem(achievement = achievement, modifier = modifier)
    } else {
        LockedAchievementItem(achievement = achievement, modifier = modifier)
    }
}

@Composable
private fun UnlockedAchievementItem(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

    val backgroundGradient = Brush.verticalGradient(
        colors = if (isDark) listOf(
            GameGold.copy(alpha = 0.45f),
            Color(0xFF3A2E15),
            Color(0xFF1A1508)
        ) else listOf(
            Color(0xFFFFF4C2),
            GameGold.copy(alpha = 0.55f),
            Color(0xFFE0A800)
        )
    )

    val topShine = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = if (isDark) 0.35f else 0.75f),
            Color.Transparent
        )
    )

    val bottomShade = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = if (isDark) 0.35f else 0.18f)
        )
    )

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.Level3),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.verticalGradient(
                colors = listOf(
                    GameGold.copy(alpha = 0.9f),
                    GameGold.copy(alpha = 0.4f)
                )
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(backgroundGradient)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(topShine)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bottomShade)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = achievement.icon, fontSize = 26.sp)
                Spacer(Modifier.weight(1f))
                Text(
                    text = achievementName(achievement),
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = if (isDark) Color(0xFFFFF1C2) else Color(0xFF3D2B00),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "+${achievement.xpReward} XP",
                    fontFamily = DynaPuffSemiCondensedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (isDark) GameGold else Color(0xFF6B4A00)
                )
            }
        }
    }
}

@Composable
private fun LockedAchievementItem(
    achievement: Achievement,
    modifier: Modifier = Modifier
) {
    val contentAlpha = 0.45f

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.Level0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = achievementName(achievement),
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "+${achievement.xpReward} XP",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )
        }
    }
}

internal fun achievementName(a: Achievement): String = when (a) {
    Achievement.FIRST_GAME       -> "Primera partida"
    Achievement.TEN_GAMES        -> "10 partidas"
    Achievement.FIFTY_GAMES      -> "50 partidas"
    Achievement.HUNDRED_GAMES    -> "100 partidas"
    Achievement.FIRST_PERFECT    -> "Primera perfecta"
    Achievement.FIVE_PERFECT     -> "5 perfectas"
    Achievement.STREAK_5         -> "Racha de 5"
    Achievement.STREAK_10        -> "Racha de 10"
    Achievement.STREAK_15        -> "Racha de 15"
    Achievement.STREAK_20        -> "Racha de 20"
    Achievement.LEVEL_10         -> "Nivel 10"
    Achievement.LEVEL_25         -> "Nivel 25"
    Achievement.LEVEL_50         -> "Nivel 50"
    Achievement.SPEED_DEMON      -> "Demonio veloz"
    Achievement.DEDICATED        -> "Dedicado"
    Achievement.ACCURACY_80      -> "Precisión 80%"
    Achievement.ACCURACY_90      -> "Precisión 90%"
    Achievement.STREAK_DAILY_7   -> "7 días seguidos"
    Achievement.STREAK_DAILY_14  -> "14 días seguidos"
    Achievement.STREAK_DAILY_30  -> "30 días seguidos"
    Achievement.STREAK_DAILY_60  -> "60 días seguidos"
    Achievement.STREAK_DAILY_90  -> "90 días seguidos"
    Achievement.STREAK_DAILY_365 -> "1 año de racha"
}
