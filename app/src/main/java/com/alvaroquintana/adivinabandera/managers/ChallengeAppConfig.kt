package com.alvaroquintana.adivinabandera.managers

import com.alvaroquintana.domain.challenge.ChallengeType

/**
 * Configuracion especifica de AdivinaBandera para el sistema de desafios diarios.
 * Centraliza los templates de desafios por dificultad y los parametros de la app.
 * Separado del manager para facilitar ajuste de balanceo sin tocar la logica.
 *
 * Diferencias respecto a PrideQuiz:
 * - Un solo modo de juego: "Classic" (sin PLAY_MODE challenge)
 * - 249 paises disponibles (vs 163 preguntas en PrideQuiz)
 * - Descripciones orientadas a banderas/paises
 * - Objetivos levemente mas altos dado el mayor banco de preguntas
 */
object ChallengeAppConfig {

    /** Modos de juego disponibles en AdivinaBandera */
    val availableGameModes = listOf("Classic")

    /** Total de paises/preguntas posibles en una partida */
    const val maxQuestionsPerGame = 249

    /**
     * Template de un desafio: tipo, objetivo base y descripcion en espanol rioplatense.
     * El objetivo base se escala por nivel del jugador (1.0x nivel 1, hasta 1.8x nivel 50).
     */
    data class ChallengeTemplate(
        val type: ChallengeType,
        val baseTarget: Int,
        val description: String,
        val extraParam: String = ""
    )

    val easyTemplates = listOf(
        ChallengeTemplate(ChallengeType.GAMES_PLAYED, 1, "Completa 1 partida"),
        ChallengeTemplate(ChallengeType.TOTAL_CORRECT, 5, "Identifica 5 banderas correctamente"),
        ChallengeTemplate(ChallengeType.SCORE_MINIMUM, 3, "Consigue al menos 3 puntos en una partida"),
        ChallengeTemplate(ChallengeType.CORRECT_STREAK, 3, "Haz una racha de 3 banderas correctas")
    )

    val mediumTemplates = listOf(
        ChallengeTemplate(ChallengeType.GAMES_PLAYED, 2, "Completa 2 partidas"),
        ChallengeTemplate(ChallengeType.TOTAL_CORRECT, 18, "Identifica 18 banderas correctamente"),
        ChallengeTemplate(ChallengeType.SCORE_MINIMUM, 10, "Consigue al menos 10 puntos en una partida"),
        ChallengeTemplate(ChallengeType.STREAK_IN_GAME, 5, "Haz una racha de 5+ en una sola partida"),
        ChallengeTemplate(ChallengeType.CUMULATIVE_SCORE, 15, "Acumula 15 puntos en total hoy")
    )

    val hardTemplates = listOf(
        ChallengeTemplate(ChallengeType.PERFECT_GAME, 1, "Completa una partida sin perder vidas"),
        ChallengeTemplate(ChallengeType.GAMES_PLAYED, 3, "Completa 3 partidas"),
        ChallengeTemplate(ChallengeType.TOTAL_CORRECT, 35, "Identifica 35 banderas correctamente"),
        ChallengeTemplate(ChallengeType.WIN_GAME, 1, "Completa todas las banderas en una partida"),
        ChallengeTemplate(ChallengeType.STREAK_IN_GAME, 10, "Haz una racha de 10+ en una sola partida"),
        ChallengeTemplate(ChallengeType.SCORE_MINIMUM, 20, "Consigue al menos 20 puntos en una partida")
    )

    val weeklyTemplates = listOf(
        ChallengeTemplate(ChallengeType.GAMES_PLAYED, 10, "Completa 10 partidas esta semana"),
        ChallengeTemplate(ChallengeType.TOTAL_CORRECT, 120, "Identifica 120 banderas correctamente esta semana"),
        ChallengeTemplate(ChallengeType.PERFECT_GAME, 3, "Completa 3 partidas perfectas esta semana")
    )
}
