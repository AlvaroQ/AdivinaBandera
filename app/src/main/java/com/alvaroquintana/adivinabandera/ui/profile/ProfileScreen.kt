package com.alvaroquintana.adivinabandera.ui.profile

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.ui.components.LoadingState
import com.alvaroquintana.adivinabandera.ui.profile.components.AchievementsSection
import com.alvaroquintana.adivinabandera.ui.profile.components.ChallengeStatsCard
import com.alvaroquintana.adivinabandera.ui.profile.components.DailyStreakCard
import com.alvaroquintana.adivinabandera.ui.profile.components.ImageSourceBottomSheet
import com.alvaroquintana.adivinabandera.ui.profile.components.ProfileHeroSection
import com.alvaroquintana.adivinabandera.ui.profile.components.StatsCard
import com.alvaroquintana.adivinabandera.ui.profile.components.XpLeaderboardButton
import com.alvaroquintana.adivinabandera.ui.profile.components.toBase64
import com.alvaroquintana.adivinabandera.ui.profile.components.uriToBase64
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient

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
