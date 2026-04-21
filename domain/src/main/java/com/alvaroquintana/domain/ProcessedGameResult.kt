package com.alvaroquintana.domain

import com.alvaroquintana.domain.challenge.ChallengeCompletionResult

data class ProcessedGameResult(
    val xpGained: Int,
    val xpBreakdown: XpBreakdown,
    val newLevel: Int,
    val previousLevel: Int,
    val leveledUp: Boolean,
    val newTitle: String,
    val newAchievements: List<Achievement>,
    val streakCheckResult: StreakCheckResult? = null,
    val streakXpBonus: Int = 0,
    val challengeCompletionResult: ChallengeCompletionResult? = null,
    val coinsEarned: Int = 0,
    val gemsEarned: Int = 0
)

data class XpBreakdown(
    val base: Int,
    val streakBonus: Int,
    val perfectBonus: Int,
    val winBonus: Int,
    val modeMultiplier: Float
)
