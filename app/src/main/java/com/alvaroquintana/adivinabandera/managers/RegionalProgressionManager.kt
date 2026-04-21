package com.alvaroquintana.adivinabandera.managers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alvaroquintana.adivinabandera.common.DataStoreKeys.RegionalProgressionKeys
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.REGIONAL_UNLOCK_THRESHOLD
import com.alvaroquintana.domain.regionalAlpha2
import com.alvaroquintana.domain.regionalChain
import com.alvaroquintana.domain.regionalPrerequisite
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Trackea aciertos por pais para el chain de modos regionales.
 * El siguiente eslabon se desbloquea cuando el anterior alcanza [REGIONAL_UNLOCK_THRESHOLD] aciertos.
 */
class RegionalProgressionManager(private val dataStore: DataStore<Preferences>) {

    private val mutex = Mutex()

    /** Suma un acierto al contador del pais dado (alpha2). No hace nada si el alpha2 no esta mapeado. */
    suspend fun recordCorrectAnswer(alpha2: String) = mutex.withLock {
        val key = keyFor(alpha2) ?: return@withLock
        dataStore.edit { prefs ->
            val current = prefs[key] ?: 0
            prefs[key] = current + 1
        }
    }

    /** Aciertos acumulados para un pais (alpha2). 0 si no hay datos o el alpha2 no esta mapeado. */
    suspend fun getCorrectAnswersFor(alpha2: String): Int {
        val key = keyFor(alpha2) ?: return 0
        return dataStore.data.map { it[key] ?: 0 }.first()
    }

    /** Aciertos de todos los modos regionales en un solo snapshot (alpha2 -> aciertos). */
    suspend fun snapshot(): Map<String, Int> {
        val prefs = dataStore.data.first()
        return regionalChain.mapNotNull { mode ->
            val alpha2 = mode.regionalAlpha2 ?: return@mapNotNull null
            val key = keyFor(alpha2) ?: return@mapNotNull null
            alpha2 to (prefs[key] ?: 0)
        }.toMap()
    }

    /**
     * true si el modo regional esta desbloqueado. El primer eslabon (sin prerequisito) siempre lo esta;
     * los demas exigen [REGIONAL_UNLOCK_THRESHOLD] aciertos en el prerequisito.
     */
    suspend fun isUnlocked(mode: GameMode): Boolean {
        val prerequisite = mode.regionalPrerequisite ?: return true
        val prereqAlpha2 = prerequisite.regionalAlpha2 ?: return true
        return getCorrectAnswersFor(prereqAlpha2) >= REGIONAL_UNLOCK_THRESHOLD
    }

    private fun keyFor(alpha2: String): Preferences.Key<Int>? = when (alpha2) {
        "ES" -> RegionalProgressionKeys.CORRECT_ANSWERS_ES
        "MX" -> RegionalProgressionKeys.CORRECT_ANSWERS_MX
        "AR" -> RegionalProgressionKeys.CORRECT_ANSWERS_AR
        "BR" -> RegionalProgressionKeys.CORRECT_ANSWERS_BR
        "DE" -> RegionalProgressionKeys.CORRECT_ANSWERS_DE
        "US" -> RegionalProgressionKeys.CORRECT_ANSWERS_US
        else -> null
    }
}
