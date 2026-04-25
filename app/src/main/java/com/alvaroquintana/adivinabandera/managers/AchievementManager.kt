package com.alvaroquintana.adivinabandera.managers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alvaroquintana.adivinabandera.common.DataStoreKeys.AchievementKeys
import com.alvaroquintana.domain.Achievement
import com.alvaroquintana.usecases.engagement.AchievementService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class AchievementManager(
    private val dataStore: DataStore<Preferences>,
    private val gameStatsManager: GameStatsManager,
    private val progressionManager: ProgressionManager,
    private val streakManager: StreakManager
) : AchievementService {

    private val mutex = Mutex()

    // Verifica todos los logros y desbloquea los nuevos que correspondan
    // Retorna la lista de logros recien desbloqueados (vacia si no hay nuevos)
    override suspend fun checkAndUnlockAchievements(): List<Achievement> = mutex.withLock {
        val unlockedIds = getUnlockedIds()
        val newlyUnlocked = mutableListOf<Achievement>()

        val totalGames = gameStatsManager.getTotalGamesPlayed()
        val perfectGames = gameStatsManager.getTotalPerfectGames()
        val bestStreak = gameStatsManager.getBestStreakEver()
        val level = progressionManager.getCurrentLevel()
        val accuracy = gameStatsManager.getAccuracy()
        val totalTime = gameStatsManager.getTotalTimePlayed()
        val streakState = streakManager.getStreakState()
        val currentDailyStreak = streakState.currentStreak

        fun check(achievement: Achievement, condition: Boolean) {
            if (!unlockedIds.contains(achievement.id) && condition) {
                newlyUnlocked.add(achievement)
            }
        }

        // Hitos de partidas jugadas
        check(Achievement.FIRST_GAME, totalGames >= 1)
        check(Achievement.TEN_GAMES, totalGames >= 10)
        check(Achievement.FIFTY_GAMES, totalGames >= 50)
        check(Achievement.HUNDRED_GAMES, totalGames >= 100)

        // Partidas perfectas
        check(Achievement.FIRST_PERFECT, perfectGames >= 1)
        check(Achievement.FIVE_PERFECT, perfectGames >= 5)

        // Rachas maximas (en partida)
        check(Achievement.STREAK_5, bestStreak >= 5)
        check(Achievement.STREAK_10, bestStreak >= 10)
        check(Achievement.STREAK_15, bestStreak >= 15)
        check(Achievement.STREAK_20, bestStreak >= 20)

        // Niveles alcanzados
        check(Achievement.LEVEL_10, level >= 10)
        check(Achievement.LEVEL_25, level >= 25)
        check(Achievement.LEVEL_50, level >= 50)

        // Logros especiales
        check(Achievement.SPEED_DEMON, totalTime > 0 && totalGames >= 10 &&
            (totalTime / totalGames) < 60_000L) // promedio menor a 1 minuto por partida
        check(Achievement.DEDICATED, totalTime >= 3_600_000L) // 1 hora total jugada
        check(Achievement.ACCURACY_80, accuracy >= 0.80f && totalGames >= 10)
        check(Achievement.ACCURACY_90, accuracy >= 0.90f && totalGames >= 20)

        // Rachas diarias
        check(Achievement.STREAK_DAILY_7, currentDailyStreak >= 7)
        check(Achievement.STREAK_DAILY_14, currentDailyStreak >= 14)
        check(Achievement.STREAK_DAILY_30, currentDailyStreak >= 30)
        check(Achievement.STREAK_DAILY_60, currentDailyStreak >= 60)
        check(Achievement.STREAK_DAILY_90, currentDailyStreak >= 90)
        check(Achievement.STREAK_DAILY_365, currentDailyStreak >= 365)

        if (newlyUnlocked.isNotEmpty()) {
            val updatedIds = (unlockedIds + newlyUnlocked.map { it.id }).toSet()
            dataStore.edit { prefs ->
                prefs[AchievementKeys.UNLOCKED_ACHIEVEMENTS] = updatedIds
            }
        }

        newlyUnlocked
    }

    suspend fun getUnlockedIds(): List<String> =
        dataStore.data.map { prefs ->
            prefs[AchievementKeys.UNLOCKED_ACHIEVEMENTS]?.toList() ?: emptyList()
        }.first()

    suspend fun getUnlockedAchievements(): List<Achievement> {
        val ids = getUnlockedIds()
        return Achievement.entries.filter { ids.contains(it.id) }
    }
}
