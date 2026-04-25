package com.alvaroquintana.adivinabandera.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.ui.components.ChallengeCompletionBanner
import com.alvaroquintana.adivinabandera.ui.components.ConfettiOverlay
import com.alvaroquintana.adivinabandera.ui.components.ModeUnlockCelebration
import com.alvaroquintana.adivinabandera.ui.components.MysteryBoxDialog
import com.alvaroquintana.adivinabandera.ui.components.StreakCelebrationDialog
import com.alvaroquintana.adivinabandera.ui.result.components.AchievementsSection
import com.alvaroquintana.adivinabandera.ui.result.components.CurrencyEarnedBadge
import com.alvaroquintana.adivinabandera.ui.result.components.EngagementBadge
import com.alvaroquintana.adivinabandera.ui.result.components.LevelUpDialog
import com.alvaroquintana.adivinabandera.ui.result.components.PlayAgainButton
import com.alvaroquintana.adivinabandera.ui.result.components.RankingButton
import com.alvaroquintana.adivinabandera.ui.result.components.RateButton
import com.alvaroquintana.adivinabandera.ui.result.components.SaveRankingDialog
import com.alvaroquintana.adivinabandera.ui.result.components.ShareButton
import com.alvaroquintana.adivinabandera.ui.result.components.StatsRow
import com.alvaroquintana.adivinabandera.ui.result.components.XpGainedBadge
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.domain.User

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    gamePoints: Int,
    onPlayAgain: () -> Unit,
    onShare: () -> Unit,
    onRate: () -> Unit,
    onViewRanking: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingPoints by remember { mutableStateOf("") }

    // Observe one-shot events to show the save dialog. Other events
    // (Game / Rate / Ranking / Share) are handled by the parent route.
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ResultViewModel.Event.SaveScoreDialog -> {
                    pendingPoints = event.points
                    showSaveDialog = true
                }
                else -> { /* handled by parent route */ }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getBackgroundGradient())
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                StatsRow(
                    gamePoints = gamePoints,
                    uiState = uiState
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                PlayAgainButton(onClick = onPlayAgain)
            }

            item {
                RankingButton(onClick = onViewRanking)
            }

            item {
                ShareButton(onClick = onShare)
            }

            item {
                RateButton(onClick = onRate)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                EngagementBadge(
                    uiState = uiState,
                    gamePoints = gamePoints
                )
            }

            item {
                XpGainedBadge(uiState = uiState)
            }

            if (uiState.coinsEarned > 0 || uiState.gemsEarned > 0) {
                item {
                    CurrencyEarnedBadge(
                        coinsEarned = uiState.coinsEarned,
                        gemsEarned = uiState.gemsEarned
                    )
                }
            }

            if (uiState.newAchievements.isNotEmpty()) {
                item {
                    AchievementsSection(achievements = uiState.newAchievements)
                }
            }

            val challengeResult = uiState.challengeCompletionResult
            if (challengeResult != null && (challengeResult.completedChallenges.isNotEmpty() || challengeResult.allDailyJustCompleted)) {
                item {
                    ChallengeCompletionBanner(
                        completionResult = challengeResult,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (gamePoints > 0) {
            ConfettiOverlay()
        }
    }

    if (showSaveDialog) {
        SaveRankingDialog(
            points = pendingPoints,
            onSave = { name ->
                viewModel.dispatch(
                    ResultViewModel.Intent.SaveTopScore(
                        User(name = name, points = pendingPoints, score = pendingPoints.toIntOrNull() ?: 0)
                    )
                )
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    if (uiState.showLevelUpDialog) {
        LevelUpDialog(
            newLevel = uiState.newLevel,
            newTitle = uiState.newTitle,
            xpGained = uiState.xpGained,
            onDismiss = { viewModel.dispatch(ResultViewModel.Intent.DismissLevelUp) }
        )
    }

    val streakResult = uiState.streakCheckResult
    if (uiState.showStreakDialog && streakResult != null) {
        StreakCelebrationDialog(
            streakCheckResult = streakResult,
            onDismiss = { viewModel.dispatch(ResultViewModel.Intent.DismissStreakDialog) }
        )
    }

    val unlockEvent = uiState.unlockEvent
    if (unlockEvent is UnlockEvent.ModeUnlocked) {
        ModeUnlockCelebration(
            modeName = unlockEvent.modeName,
            onDismiss = { viewModel.dispatch(ResultViewModel.Intent.DismissModeUnlock) }
        )
    }

    val mysteryBox = uiState.mysteryBoxReward
    if (mysteryBox != null) {
        MysteryBoxDialog(
            reward = mysteryBox,
            onDismiss = { viewModel.dispatch(ResultViewModel.Intent.DismissMysteryBox) }
        )
    }
}
