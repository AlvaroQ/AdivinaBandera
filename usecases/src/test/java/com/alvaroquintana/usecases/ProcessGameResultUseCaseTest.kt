package com.alvaroquintana.usecases

import com.alvaroquintana.domain.GameResult
import com.alvaroquintana.domain.StreakCheckResult
import com.alvaroquintana.domain.StreakReward
import com.alvaroquintana.domain.StreakState
import com.alvaroquintana.domain.XpBreakdown
import com.alvaroquintana.domain.challenge.ChallengeCompletionResult
import com.alvaroquintana.domain.cosmetics.CurrencyBalance
import com.alvaroquintana.usecases.engagement.AchievementService
import com.alvaroquintana.usecases.engagement.CurrencyService
import com.alvaroquintana.usecases.engagement.DailyChallengeService
import com.alvaroquintana.usecases.engagement.GameStatsService
import com.alvaroquintana.usecases.engagement.ProgressionService
import com.alvaroquintana.usecases.engagement.StreakService
import com.alvaroquintana.usecases.engagement.XpSyncService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProcessGameResultUseCaseTest {

    private lateinit var gameStats: GameStatsService
    private lateinit var progression: ProgressionService
    private lateinit var achievements: AchievementService
    private lateinit var xpSync: XpSyncService
    private lateinit var streak: StreakService
    private lateinit var dailyChallenge: DailyChallengeService
    private lateinit var currency: CurrencyService
    private lateinit var useCase: ProcessGameResultUseCase

    @Before
    fun setUp() {
        gameStats = mockk(relaxed = true)
        progression = mockk()
        achievements = mockk()
        xpSync = mockk(relaxed = true)
        streak = mockk()
        dailyChallenge = mockk()
        currency = mockk()

        useCase = ProcessGameResultUseCase(
            gameStats, progression, achievements, xpSync, streak, dailyChallenge, currency
        )
    }

    private fun baseStreakResult() = StreakCheckResult.StreakContinued(
        newState = StreakState(currentStreak = 3, bestStreak = 3, lastPlayedDate = "2026-04-21"),
        reward = StreakReward(xpBonus = 20, cycleDay = 3, streakMultiplier = 1.0f)
    )

    private fun perfectGame() = GameResult(
        gameMode = "Classic",
        correctAnswers = 10,
        totalQuestions = 10,
        bestStreak = 10,
        timePlayedMs = 60_000,
        completedAllQuestions = true
    )

    @Test
    fun `records stats, processes streak, adds XP and returns assembled result`() = runTest {
        val breakdown = XpBreakdown(base = 100, streakBonus = 10, perfectBonus = 100, winBonus = 25, modeMultiplier = 1.0f)
        coEvery { streak.onGameCompleted() } returns baseStreakResult()
        coEvery { progression.calculateXp(10, 10, true) } returns (235 to breakdown)
        coEvery { progression.addXp(any()) } returns Triple(1000, 5, true)
        coEvery { progression.getCurrentLevel() } returnsMany listOf(4, 5, 5)
        coEvery { progression.getCurrentTitle() } returns "Explorador"
        coEvery { achievements.checkAndUnlockAchievements() } returns emptyList()
        coEvery { dailyChallenge.processEvent(any(), any()) } returns
            ChallengeCompletionResult(emptyList(), false, 0, 0)
        coEvery { currency.earnCoins(any(), any()) } returns CurrencyBalance(0, 0)
        coEvery { currency.earnGems(any(), any()) } returns CurrencyBalance(0, 0)

        val result = useCase.invoke(perfectGame())

        // Game XP = (235 * 1.0) + 20 = 255
        val expectedGameXp = 255
        coVerifyOrder {
            gameStats.recordGameResult(any())
            streak.onGameCompleted()
            progression.calculateXp(10, 10, true)
            progression.getCurrentLevel()
            progression.addXp(expectedGameXp)
        }

        assertEquals(expectedGameXp, result.xpGained)
        assertEquals(5, result.newLevel)
        assertEquals(4, result.previousLevel)
        assertTrue(result.leveledUp)
        assertEquals("Explorador", result.newTitle)
        // Perfect game → 10*5 + 20 + 30 = 100 coins, 1 gem
        assertEquals(100, result.coinsEarned)
        assertEquals(1, result.gemsEarned)
        coVerify { currency.earnCoins(100, "game_completion") }
        coVerify { currency.earnGems(1, "perfect_game") }
    }

    @Test
    fun `AlreadyPlayedToday gives zero streak bonus but still multiplies XP`() = runTest {
        val breakdown = XpBreakdown(100, 0, 0, 0, 1.0f)
        coEvery { streak.onGameCompleted() } returns
            StreakCheckResult.AlreadyPlayedToday(StreakState(currentStreak = 10))
        coEvery { progression.calculateXp(any(), any(), any()) } returns (100 to breakdown)
        coEvery { progression.addXp(any()) } returns Triple(200, 2, false)
        coEvery { progression.getCurrentLevel() } returnsMany listOf(2, 2, 2)
        coEvery { progression.getCurrentTitle() } returns "Novato"
        coEvery { achievements.checkAndUnlockAchievements() } returns emptyList()
        coEvery { dailyChallenge.processEvent(any(), any()) } returns
            ChallengeCompletionResult(emptyList(), false, 0, 0)
        coEvery { currency.earnCoins(any(), any()) } returns CurrencyBalance(0, 0)
        coEvery { currency.earnGems(any(), any()) } returns CurrencyBalance(0, 0)

        val result = useCase.invoke(perfectGame())

        // streakMultiplier(10) = 1.1, xpBonus = 0 for AlreadyPlayedToday
        // Game XP = (100 * 1.1).toInt() + 0 = 110
        assertEquals(110, result.xpGained)
        assertFalse(result.leveledUp)
        assertEquals(0, result.streakXpBonus)
    }

    @Test
    fun `does not award coins or gems when game not completed`() = runTest {
        val partial = GameResult("Classic", 3, 10, 2, 30_000, completedAllQuestions = false)
        coEvery { streak.onGameCompleted() } returns baseStreakResult()
        coEvery { progression.calculateXp(any(), any(), any()) } returns (30 to XpBreakdown(30, 0, 0, 0, 1.0f))
        coEvery { progression.addXp(any()) } returns Triple(100, 1, false)
        coEvery { progression.getCurrentLevel() } returnsMany listOf(1, 1, 1)
        coEvery { progression.getCurrentTitle() } returns "Novato"
        coEvery { achievements.checkAndUnlockAchievements() } returns emptyList()
        coEvery { dailyChallenge.processEvent(any(), any()) } returns
            ChallengeCompletionResult(emptyList(), false, 0, 0)
        coEvery { currency.earnCoins(any(), any()) } returns CurrencyBalance(0, 0)

        val result = useCase.invoke(partial)

        assertEquals(15, result.coinsEarned) // 3 * 5 = 15, no win bonus
        assertEquals(0, result.gemsEarned)
        coVerify(exactly = 0) { currency.earnGems(any(), any()) }
    }

    @Test
    fun `xpSync failure is swallowed and does not break result`() = runTest {
        coEvery { streak.onGameCompleted() } returns baseStreakResult()
        coEvery { progression.calculateXp(any(), any(), any()) } returns (100 to XpBreakdown(100, 0, 0, 0, 1.0f))
        coEvery { progression.addXp(any()) } returns Triple(200, 2, false)
        coEvery { progression.getCurrentLevel() } returnsMany listOf(2, 2, 2)
        coEvery { progression.getCurrentTitle() } returns "Novato"
        coEvery { achievements.checkAndUnlockAchievements() } returns emptyList()
        coEvery { dailyChallenge.processEvent(any(), any()) } returns
            ChallengeCompletionResult(emptyList(), false, 0, 0)
        coEvery { currency.earnCoins(any(), any()) } returns CurrencyBalance(0, 0)
        coEvery { currency.earnGems(any(), any()) } returns CurrencyBalance(0, 0)
        coEvery { xpSync.syncAfterGame() } throws RuntimeException("network down")

        // No exception should escape
        val result = useCase.invoke(perfectGame())
        assertEquals(2, result.newLevel)
    }
}
