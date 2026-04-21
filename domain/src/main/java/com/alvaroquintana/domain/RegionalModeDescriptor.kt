package com.alvaroquintana.domain

/**
 * Estado de un modo regional para pintar UI y calcular desbloqueos del chain.
 *
 * @property mode modo concreto (RegionSpain, RegionMexico, ...)
 * @property alpha2 codigo ISO del pais asociado al modo
 * @property isUnlocked true si el jugador puede entrar al modo
 * @property correctAnswersInMode aciertos acumulados jugando este modo
 * @property prerequisiteCorrectAnswers aciertos acumulados en el modo previo del chain (0 si es el primero)
 * @property requiredToUnlockNext umbral para que este modo desbloquee el siguiente
 */
data class RegionalModeDescriptor(
    val mode: GameMode,
    val alpha2: String,
    val isUnlocked: Boolean,
    val correctAnswersInMode: Int,
    val prerequisiteCorrectAnswers: Int,
    val requiredToUnlockNext: Int
) {
    /** Progreso hacia desbloquear el SIGUIENTE modo del chain (0f..1f). */
    val unlockNextProgress: Float
        get() = if (requiredToUnlockNext == 0) 1f
                else (correctAnswersInMode.toFloat() / requiredToUnlockNext).coerceIn(0f, 1f)

    /** Progreso del prerequisito (para cards bloqueados: cuán cerca está el jugador de abrir este modo). */
    val unlockSelfProgress: Float
        get() = if (requiredToUnlockNext == 0) 1f
                else (prerequisiteCorrectAnswers.toFloat() / requiredToUnlockNext).coerceIn(0f, 1f)
}
