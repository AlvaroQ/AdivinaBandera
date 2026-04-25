package com.alvaroquintana.usecases

import com.alvaroquintana.domain.GameResult
import com.alvaroquintana.domain.ProcessedGameResult
import com.alvaroquintana.domain.StreakCheckResult
import com.alvaroquintana.domain.StreakRules
import com.alvaroquintana.domain.challenge.ChallengeEvent
import com.alvaroquintana.usecases.engagement.AchievementService
import com.alvaroquintana.usecases.engagement.CurrencyService
import com.alvaroquintana.usecases.engagement.DailyChallengeService
import com.alvaroquintana.usecases.engagement.GameStatsService
import com.alvaroquintana.usecases.engagement.ProgressionService
import com.alvaroquintana.usecases.engagement.StreakService
import com.alvaroquintana.usecases.engagement.XpSyncService
import dev.zacsweers.metro.Inject

/**
 * Orchestrates all post-game side effects: stats, streak, XP, achievements,
 * daily challenges, leaderboard sync and virtual currency rewards.
 *
 * Previously this lived in `GameResultProcessorImpl` in the app/data layer.
 * Having the orchestration live here — the use case layer — is a better fit
 * for Clean Architecture: DataSources belong to the data layer; orchestration
 * of business rules belongs to application services.
 */
@Inject
class ProcessGameResultUseCase(
    private val gameStats: GameStatsService,
    private val progression: ProgressionService,
    private val achievements: AchievementService,
    private val xpSync: XpSyncService,
    private val streak: StreakService,
    private val dailyChallenge: DailyChallengeService,
    private val currency: CurrencyService
) {
    suspend fun invoke(gameResult: GameResult): ProcessedGameResult {
        gameStats.recordGameResult(gameResult)

        val streakResult = streak.onGameCompleted()
        val streakMultiplier = streakMultiplierOf(streakResult)
        val streakXpBonus = streakXpBonusOf(streakResult)

        val (baseGameXp, breakdown) = progression.calculateXp(
            correctAnswers = gameResult.correctAnswers,
            bestStreak = gameResult.bestStreak,
            completedAll = gameResult.completedAllQuestions
        )
        val gameXp = (baseGameXp * streakMultiplier).toInt() + streakXpBonus

        val previousLevel = progression.getCurrentLevel()
        val (_, _, leveledUp) = progression.addXp(gameXp)

        val newAchievements = achievements.checkAndUnlockAchievements()
        val achievementXp = newAchievements.sumOf { it.xpReward }
        if (achievementXp > 0) progression.addXp(achievementXp)

        val playerLevel = progression.getCurrentLevel()
        val isPerfect = gameResult.correctAnswers == gameResult.totalQuestions
        val challengeCompletion = dailyChallenge.processEvent(
            event = ChallengeEvent.GameCompleted(
                gameMode = gameResult.gameMode,
                score = gameResult.correctAnswers,
                bestStreak = gameResult.bestStreak,
                isPerfect = isPerfect,
                completedAll = gameResult.completedAllQuestions,
                correctAnswers = gameResult.correctAnswers,
                totalQuestions = gameResult.totalQuestions
            ),
            playerLevel = playerLevel
        )
        if (challengeCompletion.totalXpEarned > 0) progression.addXp(challengeCompletion.totalXpEarned)

        runCatching { xpSync.syncAfterGame() }

        val finalLevel = progression.getCurrentLevel()
        val finalTitle = progression.getCurrentTitle()

        val coinsEarned = coinsForGame(
            correctAnswers = gameResult.correctAnswers,
            totalQuestions = gameResult.totalQuestions,
            completedAll = gameResult.completedAllQuestions
        )
        val gemsEarned = if (gameResult.completedAllQuestions && isPerfect) 1 else 0

        if (coinsEarned > 0) currency.earnCoins(coinsEarned, source = "game_completion")
        if (gemsEarned > 0) currency.earnGems(gemsEarned, source = "perfect_game")

        return ProcessedGameResult(
            xpGained = gameXp + achievementXp + challengeCompletion.totalXpEarned,
            xpBreakdown = breakdown,
            newLevel = finalLevel,
            previousLevel = previousLevel,
            leveledUp = finalLevel > previousLevel || leveledUp,
            newTitle = finalTitle,
            newAchievements = newAchievements,
            streakCheckResult = streakResult,
            streakXpBonus = streakXpBonus,
            challengeCompletionResult = challengeCompletion.takeIf {
                it.completedChallenges.isNotEmpty() || it.allDailyJustCompleted
            },
            coinsEarned = coinsEarned,
            gemsEarned = gemsEarned
        )
    }

    private fun streakMultiplierOf(r: StreakCheckResult): Float = when (r) {
        is StreakCheckResult.AlreadyPlayedToday -> StreakRules.streakMultiplier(r.state.currentStreak)
        is StreakCheckResult.StreakContinued -> r.reward.streakMultiplier
        is StreakCheckResult.StreakSavedByFreeze -> r.reward.streakMultiplier
        is StreakCheckResult.StreakBroken -> r.reward.streakMultiplier
        is StreakCheckResult.NewStreak -> r.reward.streakMultiplier
    }

    private fun streakXpBonusOf(r: StreakCheckResult): Int = when (r) {
        is StreakCheckResult.AlreadyPlayedToday -> 0
        is StreakCheckResult.StreakContinued -> r.reward.xpBonus
        is StreakCheckResult.StreakSavedByFreeze -> r.reward.xpBonus
        is StreakCheckResult.StreakBroken -> r.reward.xpBonus
        is StreakCheckResult.NewStreak -> r.reward.xpBonus
    }

    private fun coinsForGame(correctAnswers: Int, totalQuestions: Int, completedAll: Boolean): Int {
        var coins = correctAnswers * COINS_PER_CORRECT
        if (completedAll) coins += COINS_WIN_BONUS
        if (completedAll && correctAnswers == totalQuestions) coins += COINS_PERFECT_BONUS
        return coins
    }

    private companion object {
        const val COINS_PER_CORRECT = 5
        const val COINS_WIN_BONUS = 20
        const val COINS_PERFECT_BONUS = 30
    }
}
