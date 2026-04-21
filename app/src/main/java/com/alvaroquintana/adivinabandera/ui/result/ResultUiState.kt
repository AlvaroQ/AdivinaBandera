package com.alvaroquintana.adivinabandera.ui.result

import com.alvaroquintana.domain.Achievement
import com.alvaroquintana.domain.StreakCheckResult
import com.alvaroquintana.domain.XpBreakdown
import com.alvaroquintana.domain.challenge.ChallengeCompletionResult

sealed class UnlockEvent {
    data class ModeUnlocked(val modeName: String, val levelReached: Int) : UnlockEvent()
}

/**
 * Recompensa de la caja misteriosa que aparece tras cada 10a partida.
 * [type] determina que icono mostrar y que texto de descripcion usar.
 */
data class MysteryBoxReward(
    val xpAmount: Int = 0,
    val coinsAmount: Int = 0,
    val type: Type = Type.XP_BONUS
) {
    enum class Type { XP_BONUS, COINS_BONUS, FREEZE_TOKEN }
}

/**
 * Estado unificado de la pantalla de resultado.
 * [worldRecord] es null mientras se carga desde Firestore, string cuando llega el dato.
 * [engagementLevel] y [pointsDifference] se calculan en el ViewModel, nunca en la UI.
 * Los campos de XP/nivel/logros se populan tras procesar el resultado via [ProcessGameResultUseCase].
 * [streakCheckResult] es null hasta que se procesa el resultado; no null si hay algo para celebrar.
 * [challengeCompletionResult] es null si no se completaron desafios en esta partida.
 */
data class ResultUiState(
    val personalRecord: Int = 0,
    val worldRecord: String? = null,
    val engagementLevel: EngagementLevel = EngagementLevel.KEEP_TRYING,
    val pointsDifference: Int = 0,
    // Campos de engagement XP
    val xpGained: Int = 0,
    val xpBreakdown: XpBreakdown? = null,
    val leveledUp: Boolean = false,
    val newLevel: Int = 1,
    val newTitle: String = "",
    val previousLevel: Int = 1,
    val newAchievements: List<Achievement> = emptyList(),
    val showLevelUpDialog: Boolean = false,
    // Racha diaria
    val streakCheckResult: StreakCheckResult? = null,
    val showStreakDialog: Boolean = false,
    // Desafios diarios
    val challengeCompletionResult: ChallengeCompletionResult? = null,
    // Moneda virtual ganada en esta partida
    val coinsEarned: Int = 0,
    val gemsEarned: Int = 0,
    // Evento de desbloqueo de modo de juego
    val unlockEvent: UnlockEvent? = null,
    // Caja misteriosa — aparece tras la 10a partida, 20a, 30a, etc.
    val mysteryBoxReward: MysteryBoxReward? = null
)

enum class EngagementLevel {
    /** El jugador superó el récord mundial */
    NEW_WORLD_RECORD,

    /** El jugador superó su récord personal pero no el mundial */
    NEW_PERSONAL_BEST,

    /** Le faltaron entre 1 y 10 puntos para su récord personal */
    SO_CLOSE,

    /** Cualquier otro caso */
    KEEP_TRYING
}

