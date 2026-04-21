package com.alvaroquintana.adivinabandera.managers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alvaroquintana.adivinabandera.common.DataStoreKeys.ProgressionKeys
import com.alvaroquintana.domain.XpBreakdown
import com.alvaroquintana.domain.XpLeaderboardEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProgressionManager(private val dataStore: DataStore<Preferences>) {

    private val mutex = Mutex()

    companion object {
        const val XP_PER_CORRECT = 10
        const val XP_STREAK_BONUS = 5          // por cada umbral de 5 en racha
        const val XP_PERFECT_GAME = 100        // bonus por partida perfecta
        const val XP_WIN_BONUS = 25            // bonus por completar todas las preguntas

        // 50 umbrales de nivel (XP acumulado)
        val LEVEL_THRESHOLDS = listOf(
            0, 100, 250, 500, 800, 1200, 1700, 2300, 3000, 3800,             // 1-10
            4700, 5700, 6800, 8000, 9500, 11200, 13000, 15000, 17200, 19600, // 11-20
            22200, 25000, 28000, 31500, 35500, 40000, 45000, 50500, 56500, 63000, // 21-30
            70000, 78000, 87000, 97000, 108000, 120000, 133000, 147000, 162000, 178000, // 31-40
            195000, 213000, 232000, 252000, 273000, 295000, 318000, 342000, 367000, 393000 // 41-50
        )

        fun titleForLevel(level: Int): String = when {
            level <= 5  -> "Novato"
            level <= 10 -> "Explorador"
            level <= 15 -> "Entusiasta"
            level <= 20 -> "Conocedor"
            level <= 30 -> "Experto"
            level <= 40 -> "Maestro"
            level <= 50 -> "Gran Maestro"
            else        -> "Leyenda"
        }

        fun levelForXp(totalXp: Int): Int {
            val index = LEVEL_THRESHOLDS.indexOfLast { totalXp >= it }
            return (index + 1).coerceIn(1, 50)
        }

        fun xpForNextLevel(currentLevel: Int): Int {
            if (currentLevel >= 50) return 0
            return LEVEL_THRESHOLDS.getOrElse(currentLevel) { Int.MAX_VALUE }
        }

        fun xpProgressInCurrentLevel(totalXp: Int, currentLevel: Int): Int {
            val currentThreshold = LEVEL_THRESHOLDS.getOrElse(currentLevel - 1) { 0 }
            return totalXp - currentThreshold
        }

        fun xpNeededForCurrentLevel(currentLevel: Int): Int {
            if (currentLevel >= 50) return 1
            val current = LEVEL_THRESHOLDS.getOrElse(currentLevel - 1) { 0 }
            val next = LEVEL_THRESHOLDS.getOrElse(currentLevel) { current + 1 }
            return next - current
        }
    }

    // Calcula el XP ganado en una partida y su desglose
    fun calculateXp(
        correctAnswers: Int,
        bestStreak: Int,
        completedAll: Boolean
    ): Pair<Int, XpBreakdown> {
        val base = correctAnswers * XP_PER_CORRECT
        val streakBonus = (bestStreak / 5) * XP_STREAK_BONUS
        val perfectBonus = if (completedAll && correctAnswers > 0) XP_PERFECT_GAME else 0
        val winBonus = if (completedAll) XP_WIN_BONUS else 0
        val modeMultiplier = 1.0f // Same multiplier for all game modes

        val total = ((base + streakBonus + perfectBonus + winBonus) * modeMultiplier).toInt()

        val breakdown = XpBreakdown(
            base = base,
            streakBonus = streakBonus,
            perfectBonus = perfectBonus,
            winBonus = winBonus,
            modeMultiplier = modeMultiplier
        )

        return Pair(total, breakdown)
    }

    // Agrega XP y retorna (nuevoXpTotal, nuevoNivel, subioDeNivel)
    suspend fun addXp(amount: Int): Triple<Int, Int, Boolean> = mutex.withLock {
        val prefs = dataStore.data.first()
        val currentXp = prefs[ProgressionKeys.TOTAL_XP] ?: 0
        val previousLevel = prefs[ProgressionKeys.CURRENT_LEVEL] ?: 1

        val newXp = currentXp + amount
        val newLevel = levelForXp(newXp)
        val newTitle = titleForLevel(newLevel)
        val leveledUp = newLevel > previousLevel

        dataStore.edit { p ->
            p[ProgressionKeys.TOTAL_XP] = newXp
            p[ProgressionKeys.CURRENT_LEVEL] = newLevel
            p[ProgressionKeys.CURRENT_TITLE] = newTitle
        }

        Triple(newXp, newLevel, leveledUp)
    }

    suspend fun getTotalXp(): Int =
        dataStore.data.map { it[ProgressionKeys.TOTAL_XP] ?: 0 }.first()

    suspend fun getCurrentLevel(): Int =
        dataStore.data.map { it[ProgressionKeys.CURRENT_LEVEL] ?: 1 }.first()

    suspend fun getCurrentTitle(): String =
        dataStore.data.map { it[ProgressionKeys.CURRENT_TITLE] ?: titleForLevel(1) }.first()

    suspend fun getNickname(): String =
        dataStore.data.map { it[ProgressionKeys.NICKNAME] ?: "" }.first()

    suspend fun getImageBase64(): String =
        dataStore.data.map { it[ProgressionKeys.IMAGE_BASE64] ?: "" }.first()

    suspend fun setNickname(nickname: String) = mutex.withLock {
        dataStore.edit { it[ProgressionKeys.NICKNAME] = nickname }
    }

    suspend fun setImageBase64(image: String) = mutex.withLock {
        dataStore.edit { it[ProgressionKeys.IMAGE_BASE64] = image }
    }

    // Construye una entrada de leaderboard con los datos de progresion actuales
    // Los campos totalGamesPlayed y accuracy son completados por GameStatsManager
    suspend fun buildLeaderboardEntry(uid: String): XpLeaderboardEntry {
        val prefs = dataStore.data.first()
        return XpLeaderboardEntry(
            uid = uid,
            nickname = prefs[ProgressionKeys.NICKNAME] ?: "",
            imageBase64 = prefs[ProgressionKeys.IMAGE_BASE64] ?: "",
            totalXp = prefs[ProgressionKeys.TOTAL_XP] ?: 0,
            level = prefs[ProgressionKeys.CURRENT_LEVEL] ?: 1,
            title = prefs[ProgressionKeys.CURRENT_TITLE] ?: titleForLevel(1),
            totalGamesPlayed = 0,
            accuracy = 0f,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
