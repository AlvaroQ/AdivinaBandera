package com.alvaroquintana.usecases.engagement

import com.alvaroquintana.domain.Achievement
import com.alvaroquintana.domain.GameResult
import com.alvaroquintana.domain.StreakCheckResult
import com.alvaroquintana.domain.XpBreakdown
import com.alvaroquintana.domain.challenge.ChallengeCompletionResult
import com.alvaroquintana.domain.challenge.ChallengeEvent
import com.alvaroquintana.domain.cosmetics.CurrencyBalance

/**
 * Abstractions consumed by [ProcessGameResultUseCase]. The concrete implementations
 * (the Managers) live in the Android layer because they depend on DataStore and
 * Firebase, but the use case only needs these stateless contracts.
 */
interface ProgressionService {
    fun calculateXp(correctAnswers: Int, bestStreak: Int, completedAll: Boolean): Pair<Int, XpBreakdown>
    suspend fun addXp(amount: Int): Triple<Int, Int, Boolean>
    suspend fun getCurrentLevel(): Int
    suspend fun getCurrentTitle(): String
}

interface GameStatsService {
    suspend fun recordGameResult(result: GameResult)
}

interface StreakService {
    suspend fun onGameCompleted(): StreakCheckResult
}

interface AchievementService {
    suspend fun checkAndUnlockAchievements(): List<Achievement>
}

interface DailyChallengeService {
    suspend fun processEvent(event: ChallengeEvent, playerLevel: Int): ChallengeCompletionResult
}

interface CurrencyService {
    suspend fun earnCoins(amount: Int, source: String): CurrencyBalance
    suspend fun earnGems(amount: Int, source: String): CurrencyBalance
}

interface XpSyncService {
    suspend fun syncAfterGame()
}
