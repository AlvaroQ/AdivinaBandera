package com.alvaroquintana.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StreakRulesTest {

    // Fechas de referencia usadas en los tests
    private val today = "2024-06-15"
    private val yesterday = "2024-06-14"
    private val twoDaysAgo = "2024-06-13"

    // Estado vacio (jugador nuevo)
    private val emptyState = StreakState()

    // ===========================
    // checkStreak — Caso 1: primer juego
    // ===========================

    @Test
    fun `checkStreak cuando el jugador nunca jugo retorna NewStreak con dia 1`() {
        val result = StreakRules.checkStreak(emptyState, today, yesterday)

        assertTrue(result is StreakCheckResult.NewStreak)
        val newStreak = result as StreakCheckResult.NewStreak
        assertEquals(1, newStreak.newState.currentStreak)
        assertEquals(1, newStreak.newState.bestStreak)
        assertEquals(today, newStreak.newState.lastPlayedDate)
        assertEquals(1, newStreak.newState.cycleDay)
        assertEquals(1, newStreak.newState.totalDaysPlayed)
        assertEquals(today, newStreak.newState.streakStartDate)
    }

    @Test
    fun `checkStreak primer juego retorna recompensa de dia 1 del ciclo`() {
        val result = StreakRules.checkStreak(emptyState, today, yesterday) as StreakCheckResult.NewStreak

        assertEquals(1, result.reward.cycleDay)
        assertEquals(10, result.reward.xpBonus) // dia 1 = 10 XP
        assertFalse(result.reward.isMilestone)
    }

    // ===========================
    // checkStreak — Caso 2: jugo ayer
    // ===========================

    @Test
    fun `checkStreak cuando jugo ayer retorna StreakContinued con racha incrementada`() {
        val state = StreakState(
            currentStreak = 3,
            bestStreak = 3,
            lastPlayedDate = yesterday,
            cycleDay = 3,
            totalDaysPlayed = 3
        )

        val result = StreakRules.checkStreak(state, today, yesterday)

        assertTrue(result is StreakCheckResult.StreakContinued)
        val continued = result as StreakCheckResult.StreakContinued
        assertEquals(4, continued.newState.currentStreak)
        assertEquals(today, continued.newState.lastPlayedDate)
        assertEquals(4, continued.newState.cycleDay)
        assertEquals(4, continued.newState.totalDaysPlayed)
    }

    @Test
    fun `checkStreak racha continua actualiza bestStreak si la nueva es mayor`() {
        val state = StreakState(
            currentStreak = 5,
            bestStreak = 5,
            lastPlayedDate = yesterday,
            cycleDay = 5,
            totalDaysPlayed = 5
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        assertEquals(6, result.newState.currentStreak)
        assertEquals(6, result.newState.bestStreak)
    }

    @Test
    fun `checkStreak racha continua no modifica bestStreak si la nueva es menor`() {
        val state = StreakState(
            currentStreak = 3,
            bestStreak = 20,
            lastPlayedDate = yesterday,
            cycleDay = 3,
            totalDaysPlayed = 20
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        assertEquals(20, result.newState.bestStreak)
    }

    // ===========================
    // checkStreak — Caso 3: ya jugo hoy
    // ===========================

    @Test
    fun `checkStreak cuando ya jugo hoy retorna AlreadyPlayedToday sin cambios`() {
        val state = StreakState(
            currentStreak = 5,
            bestStreak = 5,
            lastPlayedDate = today,
            cycleDay = 5,
            totalDaysPlayed = 5
        )

        val result = StreakRules.checkStreak(state, today, yesterday)

        assertTrue(result is StreakCheckResult.AlreadyPlayedToday)
        val alreadyPlayed = result as StreakCheckResult.AlreadyPlayedToday
        assertEquals(state, alreadyPlayed.state)
    }

    // ===========================
    // checkStreak — Caso 4: falto + tiene freeze
    // ===========================

    @Test
    fun `checkStreak cuando falto ayer y tiene freeze retorna StreakSavedByFreeze`() {
        val state = StreakState(
            currentStreak = 5,
            bestStreak = 5,
            lastPlayedDate = twoDaysAgo,
            freezeTokens = 1,
            cycleDay = 5,
            totalDaysPlayed = 5
        )

        val result = StreakRules.checkStreak(state, today, yesterday)

        assertTrue(result is StreakCheckResult.StreakSavedByFreeze)
        val saved = result as StreakCheckResult.StreakSavedByFreeze
        assertEquals(6, saved.newState.currentStreak)
        assertEquals(0, saved.newState.freezeTokens) // se consumio 1
        assertEquals(today, saved.newState.lastFreezeUsedDate)
    }

    // ===========================
    // checkStreak — Caso 5: racha rota (sin freeze)
    // ===========================

    @Test
    fun `checkStreak cuando falto ayer y no tiene freeze retorna StreakBroken`() {
        val state = StreakState(
            currentStreak = 10,
            bestStreak = 10,
            lastPlayedDate = twoDaysAgo,
            freezeTokens = 0,
            cycleDay = 3,
            totalDaysPlayed = 10
        )

        val result = StreakRules.checkStreak(state, today, yesterday)

        assertTrue(result is StreakCheckResult.StreakBroken)
        val broken = result as StreakCheckResult.StreakBroken
        assertEquals(10, broken.previousStreak)
        assertEquals(1, broken.newState.currentStreak)  // resetea a 1
        assertEquals(10, broken.newState.bestStreak)    // conserva el mejor
        assertEquals(1, broken.newState.cycleDay)       // ciclo reinicia
        assertEquals(today, broken.newState.streakStartDate)
    }

    @Test
    fun `checkStreak con ausencia de varios dias retorna StreakBroken`() {
        // "twoDaysAgo" equivale a mas de 1 dia de ausencia
        val state = StreakState(
            currentStreak = 7,
            bestStreak = 15,
            lastPlayedDate = "2024-06-01", // hace 14 dias
            freezeTokens = 0,
            cycleDay = 7,
            totalDaysPlayed = 30
        )

        val result = StreakRules.checkStreak(state, today, yesterday)

        assertTrue(result is StreakCheckResult.StreakBroken)
        val broken = result as StreakCheckResult.StreakBroken
        assertEquals(7, broken.previousStreak)
        assertEquals(15, broken.newState.bestStreak) // no se pierde el record
    }

    // ===========================
    // Ciclo de dias (wrap 7 -> 1)
    // ===========================

    @Test
    fun `checkStreak en dia 6 del ciclo pasa a dia 7`() {
        val state = StreakState(
            currentStreak = 6,
            bestStreak = 6,
            lastPlayedDate = yesterday,
            cycleDay = 6,
            totalDaysPlayed = 6
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        assertEquals(7, result.newState.cycleDay)
    }

    @Test
    fun `checkStreak en dia 7 del ciclo vuelve al dia 1`() {
        val state = StreakState(
            currentStreak = 7,
            bestStreak = 7,
            lastPlayedDate = yesterday,
            cycleDay = 7,
            totalDaysPlayed = 7
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        assertEquals(1, result.newState.cycleDay)
    }

    // ===========================
    // Freeze ganado al completar ciclo de 7 dias
    // ===========================

    @Test
    fun `checkStreak completa ciclo de 7 dias gana 1 freeze token`() {
        // Cuando el nuevo cycleDay es 7 se gana 1 freeze (porque el ciclo alcanza dia 7)
        val state = StreakState(
            currentStreak = 6,
            bestStreak = 6,
            lastPlayedDate = yesterday,
            freezeTokens = 0,
            cycleDay = 6,
            totalDaysPlayed = 6
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        // Dia 7 completado: 1 freeze por ciclo + 1 por milestone de 7 dias
        assertEquals(2, result.newState.freezeTokens)
    }

    @Test
    fun `checkStreak en hito 7 dias gana freeze adicional por milestone`() {
        // racha llega a 7 exactamente (FREEZE_MILESTONES = {7, 30, 90})
        // cycleDay tambien llega a 7 => 2 freezes en total
        val state = StreakState(
            currentStreak = 6,
            bestStreak = 6,
            lastPlayedDate = yesterday,
            freezeTokens = 0,
            cycleDay = 6,
            totalDaysPlayed = 6
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        // freeze por ciclo (1) + freeze por milestone 7 dias (1) = 2
        assertEquals(2, result.newState.freezeTokens)
    }

    // ===========================
    // Freeze se topa en MAX_FREEZE_TOKENS = 3
    // ===========================

    @Test
    fun `checkStreak no supera el maximo de freeze tokens`() {
        val state = StreakState(
            currentStreak = 6,
            bestStreak = 6,
            lastPlayedDate = yesterday,
            freezeTokens = 3, // ya en maximo
            cycleDay = 6,
            totalDaysPlayed = 6
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        assertEquals(3, result.newState.freezeTokens) // sigue en 3 (no supera)
    }

    @Test
    fun `MAX_FREEZE_TOKENS es 3`() {
        assertEquals(3, StreakRules.MAX_FREEZE_TOKENS)
    }

    // ===========================
    // streakMultiplier
    // ===========================

    @Test
    fun `streakMultiplier con 0 dias es 1_0x`() {
        assertEquals(1.0f, StreakRules.streakMultiplier(0))
    }

    @Test
    fun `streakMultiplier con 6 dias es 1_0x`() {
        assertEquals(1.0f, StreakRules.streakMultiplier(6))
    }

    @Test
    fun `streakMultiplier con 7 dias es 1_1x`() {
        assertEquals(1.1f, StreakRules.streakMultiplier(7))
    }

    @Test
    fun `streakMultiplier con 13 dias es 1_1x`() {
        assertEquals(1.1f, StreakRules.streakMultiplier(13))
    }

    @Test
    fun `streakMultiplier con 14 dias es 1_2x`() {
        assertEquals(1.2f, StreakRules.streakMultiplier(14))
    }

    @Test
    fun `streakMultiplier con 29 dias es 1_2x`() {
        assertEquals(1.2f, StreakRules.streakMultiplier(29))
    }

    @Test
    fun `streakMultiplier con 30 dias es 1_3x`() {
        assertEquals(1.3f, StreakRules.streakMultiplier(30))
    }

    @Test
    fun `streakMultiplier con 59 dias es 1_3x`() {
        assertEquals(1.3f, StreakRules.streakMultiplier(59))
    }

    @Test
    fun `streakMultiplier con 60 dias es 1_5x`() {
        assertEquals(1.5f, StreakRules.streakMultiplier(60))
    }

    @Test
    fun `streakMultiplier con 89 dias es 1_5x`() {
        assertEquals(1.5f, StreakRules.streakMultiplier(89))
    }

    @Test
    fun `streakMultiplier con 90 dias es 2_0x`() {
        assertEquals(2.0f, StreakRules.streakMultiplier(90))
    }

    @Test
    fun `streakMultiplier con 365 dias es 2_0x`() {
        assertEquals(2.0f, StreakRules.streakMultiplier(365))
    }

    // ===========================
    // isStreakAtRisk
    // ===========================

    @Test
    fun `isStreakAtRisk retorna true si tiene racha activa y jugo ayer pero no hoy`() {
        val state = StreakState(
            currentStreak = 5,
            lastPlayedDate = yesterday
        )

        assertTrue(StreakRules.isStreakAtRisk(state, today, yesterday))
    }

    @Test
    fun `isStreakAtRisk retorna false si ya jugo hoy`() {
        val state = StreakState(
            currentStreak = 5,
            lastPlayedDate = today
        )

        assertFalse(StreakRules.isStreakAtRisk(state, today, yesterday))
    }

    @Test
    fun `isStreakAtRisk retorna false si no tiene racha activa`() {
        val state = StreakState(
            currentStreak = 0,
            lastPlayedDate = yesterday
        )

        assertFalse(StreakRules.isStreakAtRisk(state, today, yesterday))
    }

    @Test
    fun `isStreakAtRisk retorna false si la ultima fecha no es ayer`() {
        val state = StreakState(
            currentStreak = 5,
            lastPlayedDate = twoDaysAgo
        )

        assertFalse(StreakRules.isStreakAtRisk(state, today, yesterday))
    }

    // ===========================
    // XP por dia del ciclo
    // ===========================

    @Test
    fun `recompensa de dia 1 del ciclo es 10 XP`() {
        val result = StreakRules.checkStreak(emptyState, today, yesterday) as StreakCheckResult.NewStreak
        assertEquals(10, result.reward.xpBonus)
    }

    @Test
    fun `recompensa de dia 7 del ciclo es 100 XP`() {
        val state = StreakState(
            currentStreak = 6,
            bestStreak = 6,
            lastPlayedDate = yesterday,
            freezeTokens = 0,
            cycleDay = 6,
            totalDaysPlayed = 6
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        // dia 7 del ciclo = 100 XP + 200 XP de milestone 7 dias = 300 total
        assertEquals(300, result.reward.xpBonus)
        assertEquals(7, result.reward.cycleDay)
    }

    // ===========================
    // XP por hitos de racha
    // ===========================

    @Test
    fun `hito de 7 dias agrega 200 XP de milestone`() {
        val state = StreakState(
            currentStreak = 6,
            bestStreak = 6,
            lastPlayedDate = yesterday,
            cycleDay = 6,
            totalDaysPlayed = 6
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        assertTrue(result.reward.isMilestone)
        assertEquals(7, result.reward.milestoneDay)
    }

    @Test
    fun `hito de 30 dias agrega 1000 XP de milestone`() {
        val state = StreakState(
            currentStreak = 29,
            bestStreak = 29,
            lastPlayedDate = yesterday,
            cycleDay = 1, // cycleDay 1 => reward dia 1 = 10 XP + 1000 = 1010
            totalDaysPlayed = 29
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        assertTrue(result.reward.isMilestone)
        assertEquals(30, result.reward.milestoneDay)
        assertEquals(1015, result.reward.xpBonus) // 15 (ciclo dia 2) + 1000
    }

    @Test
    fun `hito de 90 dias agrega 5000 XP de milestone`() {
        val state = StreakState(
            currentStreak = 89,
            bestStreak = 89,
            lastPlayedDate = yesterday,
            cycleDay = 1,
            totalDaysPlayed = 89,
            freezeTokens = 2
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        assertTrue(result.reward.isMilestone)
        assertEquals(90, result.reward.milestoneDay)
        // 15 XP (cycleDay 2) + 5000 XP milestone = 5015
        assertEquals(5015, result.reward.xpBonus)
    }

    @Test
    fun `dia sin milestone no marca isMilestone`() {
        val state = StreakState(
            currentStreak = 4,
            bestStreak = 4,
            lastPlayedDate = yesterday,
            cycleDay = 4,
            totalDaysPlayed = 4
        )

        val result = StreakRules.checkStreak(state, today, yesterday) as StreakCheckResult.StreakContinued

        assertFalse(result.reward.isMilestone)
        assertEquals(0, result.reward.milestoneDay)
    }
}
