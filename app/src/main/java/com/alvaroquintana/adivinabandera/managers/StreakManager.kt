package com.alvaroquintana.adivinabandera.managers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alvaroquintana.adivinabandera.common.DataStoreKeys.StreakKeys
import com.alvaroquintana.domain.StreakCheckResult
import com.alvaroquintana.domain.StreakRules
import com.alvaroquintana.domain.StreakState
import com.alvaroquintana.usecases.engagement.StreakService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Manager que gestiona la persistencia del estado de la racha diaria.
 *
 * Delega toda la logica de negocio a [StreakRules] (dominio puro).
 * Este manager solo es responsable de:
 * - Leer el estado desde el DataStore compartido de AdivinaBandera
 * - Escribir el nuevo estado en dicho DataStore
 * - Calcular las fechas de hoy y ayer para proveerlas a StreakRules
 *
 * El Mutex garantiza que no haya condiciones de carrera si se llama
 * onGameCompleted() concurrentemente (ej: doble tap rapido en resultado).
 */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
class StreakManager(private val dataStore: DataStore<Preferences>) : StreakService {

    private val mutex = Mutex()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Debe llamarse al finalizar cada partida completada.
     * Evalua el estado de racha y persiste el nuevo estado si hubo cambios.
     *
     * @return StreakCheckResult describiendo que ocurrio (continua, rota, salvada, etc.)
     */
    override suspend fun onGameCompleted(): StreakCheckResult = mutex.withLock {
        val state = readState()
        val today = todayDate()
        val yesterday = yesterdayDate()

        val result = StreakRules.checkStreak(state, today, yesterday)

        // Solo persistir si el estado cambio (AlreadyPlayedToday no cambia nada)
        val newState = when (result) {
            is StreakCheckResult.AlreadyPlayedToday -> null
            is StreakCheckResult.StreakContinued -> result.newState
            is StreakCheckResult.StreakSavedByFreeze -> result.newState
            is StreakCheckResult.StreakBroken -> result.newState
            is StreakCheckResult.NewStreak -> result.newState
        }

        if (newState != null) {
            writeState(newState)
        }

        result
    }

    /** Retorna el estado actual de la racha sin modificarlo. */
    suspend fun getStreakState(): StreakState = readState()

    /**
     * Indica si la racha del jugador esta en riesgo de romperse hoy.
     * Util para mostrar avisos o notificaciones en la UI.
     */
    suspend fun isStreakAtRisk(): Boolean {
        val state = readState()
        return StreakRules.isStreakAtRisk(state, todayDate(), yesterdayDate())
    }

    /** Verifica si el jugador ya completo al menos una partida hoy. */
    suspend fun hasPlayedToday(): Boolean {
        return readState().lastPlayedDate == todayDate()
    }

    /**
     * Retorna el multiplicador de XP correspondiente a la racha actual.
     * Se debe aplicar sobre el XP base de la partida antes de persistirlo.
     */
    suspend fun getStreakMultiplier(): Float {
        return StreakRules.streakMultiplier(readState().currentStreak)
    }

    // ==================== PERSISTENCIA ====================

    private suspend fun readState(): StreakState {
        val prefs = dataStore.data.first()
        return StreakState(
            currentStreak = prefs[StreakKeys.CURRENT_STREAK] ?: 0,
            bestStreak = prefs[StreakKeys.BEST_STREAK] ?: 0,
            lastPlayedDate = prefs[StreakKeys.LAST_PLAYED_DATE] ?: "",
            freezeTokens = prefs[StreakKeys.FREEZE_TOKENS] ?: 0,
            cycleDay = prefs[StreakKeys.CYCLE_DAY] ?: 1,
            totalDaysPlayed = prefs[StreakKeys.TOTAL_DAYS_PLAYED] ?: 0,
            streakStartDate = prefs[StreakKeys.STREAK_START_DATE] ?: "",
            lastFreezeUsedDate = prefs[StreakKeys.LAST_FREEZE_USED_DATE] ?: ""
        )
    }

    private suspend fun writeState(state: StreakState) {
        dataStore.edit { prefs ->
            prefs[StreakKeys.CURRENT_STREAK] = state.currentStreak
            prefs[StreakKeys.BEST_STREAK] = state.bestStreak
            prefs[StreakKeys.LAST_PLAYED_DATE] = state.lastPlayedDate
            prefs[StreakKeys.FREEZE_TOKENS] = state.freezeTokens
            prefs[StreakKeys.CYCLE_DAY] = state.cycleDay
            prefs[StreakKeys.TOTAL_DAYS_PLAYED] = state.totalDaysPlayed
            prefs[StreakKeys.STREAK_START_DATE] = state.streakStartDate
            prefs[StreakKeys.LAST_FREEZE_USED_DATE] = state.lastFreezeUsedDate
        }
    }

    // ==================== FECHAS ====================

    private fun todayDate(): String = dateFormat.format(Calendar.getInstance().time)

    private fun yesterdayDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return dateFormat.format(cal.time)
    }
}
