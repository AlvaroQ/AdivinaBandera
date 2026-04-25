package com.alvaroquintana.adivinabandera.managers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alvaroquintana.adivinabandera.common.DataStoreKeys.GameStatsKeys
import com.alvaroquintana.domain.GameResult
import com.alvaroquintana.usecases.engagement.GameStatsService
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
class GameStatsManager(private val dataStore: DataStore<Preferences>) : GameStatsService {

    private val mutex = Mutex()

    // Registra el resultado de una partida actualizando todas las estadisticas acumuladas
    override suspend fun recordGameResult(result: GameResult) {
        mutex.withLock {
            dataStore.edit { prefs ->
                val totalGames = (prefs[GameStatsKeys.TOTAL_GAMES_PLAYED] ?: 0) + 1
                val totalCorrect = (prefs[GameStatsKeys.TOTAL_CORRECT_ANSWERS] ?: 0) + result.correctAnswers
                val totalWrong = (prefs[GameStatsKeys.TOTAL_WRONG_ANSWERS] ?: 0) +
                    (result.totalQuestions - result.correctAnswers)
                val currentBestStreak = prefs[GameStatsKeys.BEST_STREAK_EVER] ?: 0
                val totalTime = (prefs[GameStatsKeys.TOTAL_TIME_PLAYED_MS] ?: 0L) + result.timePlayedMs
                val perfectGames = (prefs[GameStatsKeys.TOTAL_PERFECT_GAMES] ?: 0) +
                    if (result.completedAllQuestions && result.correctAnswers == result.totalQuestions) 1 else 0

                prefs[GameStatsKeys.TOTAL_GAMES_PLAYED] = totalGames
                prefs[GameStatsKeys.TOTAL_CORRECT_ANSWERS] = totalCorrect
                prefs[GameStatsKeys.TOTAL_WRONG_ANSWERS] = totalWrong
                prefs[GameStatsKeys.TOTAL_TIME_PLAYED_MS] = totalTime
                prefs[GameStatsKeys.TOTAL_PERFECT_GAMES] = perfectGames

                if (result.bestStreak > currentBestStreak) {
                    prefs[GameStatsKeys.BEST_STREAK_EVER] = result.bestStreak
                }
            }
        }
    }

    suspend fun getTotalGamesPlayed(): Int =
        dataStore.data.map { it[GameStatsKeys.TOTAL_GAMES_PLAYED] ?: 0 }.first()

    suspend fun getTotalCorrectAnswers(): Int =
        dataStore.data.map { it[GameStatsKeys.TOTAL_CORRECT_ANSWERS] ?: 0 }.first()

    suspend fun getTotalWrongAnswers(): Int =
        dataStore.data.map { it[GameStatsKeys.TOTAL_WRONG_ANSWERS] ?: 0 }.first()

    suspend fun getBestStreakEver(): Int =
        dataStore.data.map { it[GameStatsKeys.BEST_STREAK_EVER] ?: 0 }.first()

    suspend fun getTotalPerfectGames(): Int =
        dataStore.data.map { it[GameStatsKeys.TOTAL_PERFECT_GAMES] ?: 0 }.first()

    suspend fun getTotalTimePlayed(): Long =
        dataStore.data.map { it[GameStatsKeys.TOTAL_TIME_PLAYED_MS] ?: 0L }.first()

    // Precision global: respuestas correctas / total de respuestas
    suspend fun getAccuracy(): Float {
        val correct = getTotalCorrectAnswers()
        val wrong = getTotalWrongAnswers()
        val total = correct + wrong
        return if (total > 0) correct.toFloat() / total else 0f
    }
}
