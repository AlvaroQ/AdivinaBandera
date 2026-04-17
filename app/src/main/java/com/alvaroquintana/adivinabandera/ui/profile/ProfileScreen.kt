package com.alvaroquintana.adivinabandera.ui.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Leaderboard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.ui.components.LoadingState
import com.alvaroquintana.adivinabandera.ui.profile.components.ImageSourceBottomSheet
import com.alvaroquintana.adivinabandera.ui.profile.components.ProfileHeroSection
import com.alvaroquintana.adivinabandera.ui.profile.components.toBase64
import com.alvaroquintana.adivinabandera.ui.profile.components.uriToBase64
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.net.Uri
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.ElevationTokens
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoftLight
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme
import com.alvaroquintana.adivinabandera.ui.theme.isExpanded
import com.alvaroquintana.domain.Achievement
import com.alvaroquintana.domain.challenge.ChallengeStats

@Composable
private fun profileAccentColor(): Color =
    if (isAppInDarkTheme()) MaterialTheme.colorScheme.primary else GeoNavySoftLight

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onNavigateToXpLeaderboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var imageSheetOpen by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let { viewModel.saveProfileImage(it.toBase64()) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uriToBase64(context = context, uri = it)?.let(viewModel::saveProfileImage)
        }
    }

    LaunchedEffect(Unit) {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_PROFILE)
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getBackgroundGradient())
        ) {
            LoadingState()
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(getBackgroundGradient()),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "hero") {
            ProfileHeroSection(
                nickname = uiState.nickname,
                imageBase64 = uiState.imageBase64,
                level = uiState.level,
                totalXp = uiState.totalXp,
                xpProgressInLevel = uiState.xpProgressInLevel,
                xpNeededForLevel = uiState.xpNeededForLevel,
                xpForNextLevel = uiState.xpForNextLevel,
                globalRank = uiState.globalRank,
                onNicknameChange = viewModel::onNicknameChanged,
                onEditImageClick = { imageSheetOpen = true },
                modifier = Modifier.animateItem()
            )
        }

        item(key = "stats") {
            StatsCard(
                totalGamesPlayed = uiState.totalGamesPlayed,
                accuracy = uiState.accuracy,
                bestStreakEver = uiState.bestStreakEver,
                totalPerfectGames = uiState.totalPerfectGames,
                totalTimePlayed = uiState.totalTimePlayed,
                modifier = Modifier.animateItem()
            )
        }

        item(key = "daily-streak") {
            DailyStreakCard(
                currentStreak = uiState.currentDailyStreak,
                bestStreak = uiState.bestDailyStreak,
                totalDaysPlayed = uiState.totalDaysPlayed,
                freezeTokens = uiState.freezeTokens,
                modifier = Modifier.animateItem()
            )
        }

        if (uiState.challengeStats.totalCompleted > 0) {
            item(key = "challenge-stats") {
                ChallengeStatsCard(
                    stats = uiState.challengeStats,
                    modifier = Modifier.animateItem()
                )
            }
        }

        item(key = "xp-leaderboard") {
            XpLeaderboardButton(
                onClick = onNavigateToXpLeaderboard,
                modifier = Modifier.animateItem()
            )
        }

        item(key = "achievements") {
            AchievementsSection(
                allAchievements = uiState.allAchievements,
                unlockedAchievements = uiState.unlockedAchievements,
                modifier = Modifier.animateItem()
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }

    if (imageSheetOpen) {
        ImageSourceBottomSheet(
            onDismiss = { imageSheetOpen = false },
            onPickFromCamera = { cameraLauncher.launch(null) },
            onPickFromGallery = { galleryLauncher.launch("image/*") }
        )
    }
}

// ── StatsCard (now a M3 Card) ─────────────────────────────────────────────────
@Composable
private fun StatsCard(
    totalGamesPlayed: Int,
    accuracy: Float,
    bestStreakEver: Int,
    totalPerfectGames: Int,
    totalTimePlayed: Long,
    modifier: Modifier = Modifier
) {
    val totalMinutes = totalTimePlayed / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val timeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    val accuracyText = "${(accuracy * 100).toInt()}%"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.Level1)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estadísticas",
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Partidas",
                    value = totalGamesPlayed.toString(),
                    emoji = "\uD83C\uDFAE",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Precisión",
                    value = accuracyText,
                    emoji = "\uD83C\uDFAF",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Mejor racha",
                    value = bestStreakEver.toString(),
                    emoji = "\uD83D\uDD25",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Perfectas",
                    value = totalPerfectGames.toString(),
                    emoji = "\u2728",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))

            StatItem(
                label = "Tiempo jugado",
                value = timeText,
                emoji = "\u23F1\uFE0F",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontFamily = DynaPuffFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── DailyStreakCard ───────────────────────────────────────────────────────────
@Composable
private fun DailyStreakCard(
    currentStreak: Int,
    bestStreak: Int,
    totalDaysPlayed: Int,
    freezeTokens: Int,
    modifier: Modifier = Modifier
) {
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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Whatshot,
                    contentDescription = null,
                    tint = GameGold,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Racha Diaria",
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
                            text = "$currentStreak días",
                            fontFamily = DynaPuffSemiCondensedFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = GameGold.copy(alpha = 0.15f),
                        labelColor = GameGold
                    ),
                    border = AssistChipDefaults.assistChipBorder(
                        enabled = true,
                        borderColor = GameGold.copy(alpha = 0.4f)
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Mejor racha",
                    value = "$bestStreak días",
                    emoji = "\uD83D\uDD25",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Días jugados",
                    value = totalDaysPlayed.toString(),
                    emoji = "\uD83D\uDCC5",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Tokens ❄",
                    value = freezeTokens.toString(),
                    emoji = "\u2744\uFE0F",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── ChallengeStatsCard ────────────────────────────────────────────────────────
@Composable
private fun ChallengeStatsCard(
    stats: ChallengeStats,
    modifier: Modifier = Modifier
) {
    val accentColor = profileAccentColor()

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "\uD83C\uDFC6", fontSize = 18.sp)
                Text(
                    text = "Desafíos Diarios",
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
                            text = "${stats.totalCompleted} completados",
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Días perfectos",
                    value = stats.totalAllDailyCompleteDays.toString(),
                    emoji = "\u2B50",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Racha actual",
                    value = "${stats.currentAllDailyStreak} días",
                    emoji = "\uD83D\uDD25",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Mejor racha",
                    value = "${stats.bestAllDailyStreak} días",
                    emoji = "\uD83C\uDF1F",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ── XpLeaderboardButton (ElevatedCard clickable) ──────────────────────────────
@Composable
private fun XpLeaderboardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = profileAccentColor()

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = accentColor.copy(alpha = 0.16f)
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = ElevationTokens.Level2)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Leaderboard,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Ver ranking mundial de XP",
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Achievements section ──────────────────────────────────────────────────────
@Composable
private fun AchievementsSection(
    allAchievements: List<Achievement>,
    unlockedAchievements: List<Achievement>,
    modifier: Modifier = Modifier
) {
    val accentColor = profileAccentColor()
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

    // Gradient dorado con profundidad: top cálido (gold vivo) → medio tinted → bottom oscuro.
    // Esta caída simula un material metálico bajo luz, dándole sensación 3D al fondo.
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

    // Brillo top — simula reflejo de luz (bevel superior)
    val topShine = Brush.horizontalGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = if (isDark) 0.35f else 0.75f),
            Color.Transparent
        )
    )

    // Sombra interior inferior — profundidad 3D
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
            // Shine de arriba (1dp) — bevel de luz
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(topShine)
            )
            // Sombra inferior — da volumen
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
                Spacer(Modifier.height(6.dp))
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
                Spacer(Modifier.height(4.dp))
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
            Spacer(Modifier.height(6.dp))
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
            Spacer(Modifier.height(4.dp))
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

private fun achievementName(a: Achievement): String = when (a) {
    Achievement.FIRST_GAME      -> "Primera partida"
    Achievement.TEN_GAMES       -> "10 partidas"
    Achievement.FIFTY_GAMES     -> "50 partidas"
    Achievement.HUNDRED_GAMES   -> "100 partidas"
    Achievement.FIRST_PERFECT   -> "Primera perfecta"
    Achievement.FIVE_PERFECT    -> "5 perfectas"
    Achievement.STREAK_5        -> "Racha de 5"
    Achievement.STREAK_10       -> "Racha de 10"
    Achievement.STREAK_15       -> "Racha de 15"
    Achievement.STREAK_20       -> "Racha de 20"
    Achievement.LEVEL_10        -> "Nivel 10"
    Achievement.LEVEL_25        -> "Nivel 25"
    Achievement.LEVEL_50        -> "Nivel 50"
    Achievement.SPEED_DEMON     -> "Demonio veloz"
    Achievement.DEDICATED       -> "Dedicado"
    Achievement.ACCURACY_80     -> "Precisión 80%"
    Achievement.ACCURACY_90     -> "Precisión 90%"
    Achievement.STREAK_DAILY_7  -> "7 días seguidos"
    Achievement.STREAK_DAILY_14 -> "14 días seguidos"
    Achievement.STREAK_DAILY_30 -> "30 días seguidos"
    Achievement.STREAK_DAILY_60 -> "60 días seguidos"
    Achievement.STREAK_DAILY_90 -> "90 días seguidos"
    Achievement.STREAK_DAILY_365 -> "1 año de racha"
}
