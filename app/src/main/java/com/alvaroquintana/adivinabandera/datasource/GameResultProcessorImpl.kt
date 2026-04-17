package com.alvaroquintana.adivinabandera.datasource

import com.alvaroquintana.adivinabandera.managers.AchievementManager
import com.alvaroquintana.adivinabandera.managers.CurrencyManager
import com.alvaroquintana.adivinabandera.managers.DailyChallengeManager
import com.alvaroquintana.adivinabandera.managers.GameStatsManager
import com.alvaroquintana.adivinabandera.managers.ProgressionManager
import com.alvaroquintana.adivinabandera.managers.StreakManager
import com.alvaroquintana.adivinabandera.managers.XpSyncManager
import com.alvaroquintana.data.datasource.GameResultProcessorDataSource
import com.alvaroquintana.domain.GameResult
import com.alvaroquintana.domain.ProcessedGameResult
import com.alvaroquintana.domain.StreakCheckResult
import com.alvaroquintana.domain.challenge.ChallengeEvent

class GameResultProcessorImpl(
    private val gameStatsManager: GameStatsManager,
    private val progressionManager: ProgressionManager,
    private val achievementManager: AchievementManager,
    private val xpSyncManager: XpSyncManager,
    private val streakManager: StreakManager,
    private val dailyChallengeManager: DailyChallengeManager,
    private val currencyManager: CurrencyManager
) : GameResultProcessorDataSource {

    override suspend fun processGameResult(gameResult: GameResult): ProcessedGameResult {
        // 1. Registrar estadisticas de la partida
        gameStatsManager.recordGameResult(gameResult)

        // 2. Procesar racha diaria y obtener multiplicador ANTES de calcular XP
        val streakResult = streakManager.onGameCompleted()
        val streakMultiplier = when (streakResult) {
            is StreakCheckResult.AlreadyPlayedToday -> streakResult.state.let {
                com.alvaroquintana.domain.StreakRules.streakMultiplier(it.currentStreak)
            }
            is StreakCheckResult.StreakContinued -> streakResult.reward.streakMultiplier
            is StreakCheckResult.StreakSavedByFreeze -> streakResult.reward.streakMultiplier
            is StreakCheckResult.StreakBroken -> streakResult.reward.streakMultiplier
            is StreakCheckResult.NewStreak -> streakResult.reward.streakMultiplier
        }
        val streakXpBonus = when (streakResult) {
            is StreakCheckResult.AlreadyPlayedToday -> 0
            is StreakCheckResult.StreakContinued -> streakResult.reward.xpBonus
            is StreakCheckResult.StreakSavedByFreeze -> streakResult.reward.xpBonus
            is StreakCheckResult.StreakBroken -> streakResult.reward.xpBonus
            is StreakCheckResult.NewStreak -> streakResult.reward.xpBonus
        }

        // 3. Calcular y agregar XP de la partida (con multiplicador de racha aplicado)
        val (baseGameXp, breakdown) = progressionManager.calculateXp(
            correctAnswers = gameResult.correctAnswers,
            bestStreak = gameResult.bestStreak,
            completedAll = gameResult.completedAllQuestions
        )
        val gameXp = (baseGameXp * streakMultiplier).toInt() + streakXpBonus

        val previousLevel = progressionManager.getCurrentLevel()
        val (_, newLevel, leveledUp) = progressionManager.addXp(gameXp)

        // 4. Verificar logros y otorgar XP bonus por los nuevos
        val newAchievements = achievementManager.checkAndUnlockAchievements()
        var achievementXp = 0
        for (achievement in newAchievements) {
            achievementXp += achievement.xpReward
        }
        if (achievementXp > 0) {
            progressionManager.addXp(achievementXp)
        }

        // 5. Procesar desafios diarios con el evento de partida completada
        val playerLevel = progressionManager.getCurrentLevel()
        val isPerfect = gameResult.correctAnswers == gameResult.totalQuestions
        val challengeCompletionResult = dailyChallengeManager.processEvent(
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

        // Acreditar XP de desafios completados
        if (challengeCompletionResult.totalXpEarned > 0) {
            progressionManager.addXp(challengeCompletionResult.totalXpEarned)
        }

        // 6. Sincronizar al leaderboard de Firestore (best-effort)
        try {
            xpSyncManager.syncAfterGame()
        } catch (_: Exception) {
            // Los fallos de sincronizacion son manejados internamente por XpSyncManager
        }

        val finalLevel = progressionManager.getCurrentLevel()
        val finalTitle = progressionManager.getCurrentTitle()

        // 7. Calcular y otorgar moneda virtual segun rendimiento de la partida
        val coinsEarned = calculateCoinsForGame(
            correctAnswers = gameResult.correctAnswers,
            totalQuestions = gameResult.totalQuestions,
            completedAll = gameResult.completedAllQuestions
        )
        val gemsEarned = if (gameResult.completedAllQuestions &&
            gameResult.correctAnswers == gameResult.totalQuestions) 1 else 0

        if (coinsEarned > 0) {
            currencyManager.earnCoins(coinsEarned, source = "game_completion")
        }
        if (gemsEarned > 0) {
            currencyManager.earnGems(gemsEarned, source = "perfect_game")
        }

        return ProcessedGameResult(
            xpGained = gameXp + achievementXp + challengeCompletionResult.totalXpEarned,
            xpBreakdown = breakdown,
            newLevel = finalLevel,
            previousLevel = previousLevel,
            leveledUp = finalLevel > previousLevel || leveledUp,
            newTitle = finalTitle,
            newAchievements = newAchievements,
            streakCheckResult = streakResult,
            streakXpBonus = streakXpBonus,
            challengeCompletionResult = if (challengeCompletionResult.completedChallenges.isNotEmpty() || challengeCompletionResult.allDailyJustCompleted) {
                challengeCompletionResult
            } else {
                null
            },
            coinsEarned = coinsEarned,
            gemsEarned = gemsEarned
        )
    }

    /**
     * Calcula las monedas a otorgar segun el rendimiento de la partida.
     * - Base: 5 coins por respuesta correcta
     * - Bonus completar todas: +20 coins
     * - Bonus partida perfecta: +30 coins adicionales
     */
    private fun calculateCoinsForGame(
        correctAnswers: Int,
        totalQuestions: Int,
        completedAll: Boolean
    ): Int {
        var coins = correctAnswers * 5
        if (completedAll) coins += 20
        if (completedAll && correctAnswers == totalQuestions) coins += 30
        return coins
    }
}
