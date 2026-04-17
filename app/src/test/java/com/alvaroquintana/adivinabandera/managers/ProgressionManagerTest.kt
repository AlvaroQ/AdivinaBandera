package com.alvaroquintana.adivinabandera.managers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests unitarios para ProgressionManager.
 * Las funciones del companion object se testean directamente.
 * calculateXp se testea via instancia con DataStore mockeado (no se llama durante el test).
 */
class ProgressionManagerTest {

    private lateinit var manager: ProgressionManager

    @Before
    fun setUp() {
        // DataStore mockeado — calculateXp no lo usa en absoluto
        val mockDataStore = mockk<DataStore<Preferences>>(relaxed = true)
        manager = ProgressionManager(mockDataStore)
    }

    // ===========================
    // levelForXp — limites de umbral
    // ===========================

    @Test
    fun `levelForXp con 0 XP retorna nivel 1`() {
        assertEquals(1, ProgressionManager.levelForXp(0))
    }

    @Test
    fun `levelForXp con 99 XP retorna nivel 1`() {
        assertEquals(1, ProgressionManager.levelForXp(99))
    }

    @Test
    fun `levelForXp con 100 XP retorna nivel 2`() {
        assertEquals(2, ProgressionManager.levelForXp(100))
    }

    @Test
    fun `levelForXp con 249 XP retorna nivel 2`() {
        assertEquals(2, ProgressionManager.levelForXp(249))
    }

    @Test
    fun `levelForXp con 250 XP retorna nivel 3`() {
        assertEquals(3, ProgressionManager.levelForXp(250))
    }

    @Test
    fun `levelForXp con 392999 XP retorna nivel 49`() {
        assertEquals(49, ProgressionManager.levelForXp(392999))
    }

    @Test
    fun `levelForXp con 393000 XP retorna nivel 50`() {
        assertEquals(50, ProgressionManager.levelForXp(393000))
    }

    @Test
    fun `levelForXp con XP mayor al maximo retorna nivel 50`() {
        assertEquals(50, ProgressionManager.levelForXp(500000))
    }

    @Test
    fun `levelForXp nunca retorna valor menor a 1`() {
        assertEquals(1, ProgressionManager.levelForXp(-100))
    }

    // ===========================
    // titleForLevel — todos los rangos
    // ===========================

    @Test
    fun `titleForLevel nivel 1 es Novato`() {
        assertEquals("Novato", ProgressionManager.titleForLevel(1))
    }

    @Test
    fun `titleForLevel nivel 5 es Novato`() {
        assertEquals("Novato", ProgressionManager.titleForLevel(5))
    }

    @Test
    fun `titleForLevel nivel 6 es Explorador`() {
        assertEquals("Explorador", ProgressionManager.titleForLevel(6))
    }

    @Test
    fun `titleForLevel nivel 10 es Explorador`() {
        assertEquals("Explorador", ProgressionManager.titleForLevel(10))
    }

    @Test
    fun `titleForLevel nivel 11 es Entusiasta`() {
        assertEquals("Entusiasta", ProgressionManager.titleForLevel(11))
    }

    @Test
    fun `titleForLevel nivel 15 es Entusiasta`() {
        assertEquals("Entusiasta", ProgressionManager.titleForLevel(15))
    }

    @Test
    fun `titleForLevel nivel 16 es Conocedor`() {
        assertEquals("Conocedor", ProgressionManager.titleForLevel(16))
    }

    @Test
    fun `titleForLevel nivel 20 es Conocedor`() {
        assertEquals("Conocedor", ProgressionManager.titleForLevel(20))
    }

    @Test
    fun `titleForLevel nivel 21 es Experto`() {
        assertEquals("Experto", ProgressionManager.titleForLevel(21))
    }

    @Test
    fun `titleForLevel nivel 30 es Experto`() {
        assertEquals("Experto", ProgressionManager.titleForLevel(30))
    }

    @Test
    fun `titleForLevel nivel 31 es Maestro`() {
        assertEquals("Maestro", ProgressionManager.titleForLevel(31))
    }

    @Test
    fun `titleForLevel nivel 40 es Maestro`() {
        assertEquals("Maestro", ProgressionManager.titleForLevel(40))
    }

    @Test
    fun `titleForLevel nivel 41 es Gran Maestro`() {
        assertEquals("Gran Maestro", ProgressionManager.titleForLevel(41))
    }

    @Test
    fun `titleForLevel nivel 50 es Gran Maestro`() {
        assertEquals("Gran Maestro", ProgressionManager.titleForLevel(50))
    }

    @Test
    fun `titleForLevel nivel superior a 50 es Leyenda`() {
        assertEquals("Leyenda", ProgressionManager.titleForLevel(51))
        assertEquals("Leyenda", ProgressionManager.titleForLevel(100))
    }

    // ===========================
    // xpForNextLevel
    // ===========================

    @Test
    fun `xpForNextLevel nivel 1 retorna 100`() {
        assertEquals(100, ProgressionManager.xpForNextLevel(1))
    }

    @Test
    fun `xpForNextLevel nivel 2 retorna 250`() {
        assertEquals(250, ProgressionManager.xpForNextLevel(2))
    }

    @Test
    fun `xpForNextLevel nivel 49 retorna 393000`() {
        assertEquals(393000, ProgressionManager.xpForNextLevel(49))
    }

    @Test
    fun `xpForNextLevel en nivel maximo 50 retorna 0`() {
        assertEquals(0, ProgressionManager.xpForNextLevel(50))
    }

    // ===========================
    // xpProgressInCurrentLevel
    // ===========================

    @Test
    fun `xpProgressInCurrentLevel nivel 1 con 50 XP es 50`() {
        // threshold nivel 1 = 0 => 50 - 0 = 50
        assertEquals(50, ProgressionManager.xpProgressInCurrentLevel(50, 1))
    }

    @Test
    fun `xpProgressInCurrentLevel nivel 2 con 150 XP es 50`() {
        // threshold nivel 2 = 100 => 150 - 100 = 50
        assertEquals(50, ProgressionManager.xpProgressInCurrentLevel(150, 2))
    }

    @Test
    fun `xpProgressInCurrentLevel nivel 3 con 300 XP es 50`() {
        // threshold nivel 3 = 250 => 300 - 250 = 50
        assertEquals(50, ProgressionManager.xpProgressInCurrentLevel(300, 3))
    }

    @Test
    fun `xpProgressInCurrentLevel al inicio exacto del nivel es 0`() {
        // threshold nivel 2 = 100, si totalXp = 100 entonces progreso = 0
        assertEquals(0, ProgressionManager.xpProgressInCurrentLevel(100, 2))
    }

    // ===========================
    // xpNeededForCurrentLevel
    // ===========================

    @Test
    fun `xpNeededForCurrentLevel nivel 1 es 100`() {
        // LEVEL_THRESHOLDS[0]=0, LEVEL_THRESHOLDS[1]=100 => 100 - 0 = 100
        assertEquals(100, ProgressionManager.xpNeededForCurrentLevel(1))
    }

    @Test
    fun `xpNeededForCurrentLevel nivel 2 es 150`() {
        // LEVEL_THRESHOLDS[1]=100, LEVEL_THRESHOLDS[2]=250 => 250 - 100 = 150
        assertEquals(150, ProgressionManager.xpNeededForCurrentLevel(2))
    }

    @Test
    fun `xpNeededForCurrentLevel nivel 50 retorna 1 indicando nivel maximo`() {
        assertEquals(1, ProgressionManager.xpNeededForCurrentLevel(50))
    }

    // ===========================
    // calculateXp — logica de instancia (modeMultiplier siempre 1.0f)
    // ===========================

    @Test
    fun `calculateXp con 0 respuestas correctas retorna 0 XP`() {
        val (xp, breakdown) = manager.calculateXp(
            correctAnswers = 0,
            bestStreak = 0,
            completedAll = false
        )

        assertEquals(0, xp)
        assertEquals(0, breakdown.base)
        assertEquals(0, breakdown.streakBonus)
        assertEquals(0, breakdown.perfectBonus)
        assertEquals(0, breakdown.winBonus)
        assertEquals(1.0f, breakdown.modeMultiplier)
    }

    @Test
    fun `calculateXp happy path sin racha ni partida completa`() {
        // 5 correctas * 10 = 50 XP base, racha 0 = 0 bonus
        val (xp, breakdown) = manager.calculateXp(
            correctAnswers = 5,
            bestStreak = 0,
            completedAll = false
        )

        assertEquals(50, xp)
        assertEquals(50, breakdown.base)
        assertEquals(0, breakdown.streakBonus)
        assertEquals(0, breakdown.perfectBonus)
        assertEquals(0, breakdown.winBonus)
    }

    @Test
    fun `calculateXp con racha de 5 agrega un bonus de 5 XP`() {
        // (5 / 5) * 5 = 5 de streakBonus
        val (xp, breakdown) = manager.calculateXp(
            correctAnswers = 3,
            bestStreak = 5,
            completedAll = false
        )

        assertEquals(5, breakdown.streakBonus)
        assertEquals(35, xp) // 30 base + 5 streak
    }

    @Test
    fun `calculateXp con racha de 10 agrega 10 XP de streak bonus`() {
        // (10 / 5) * 5 = 10 de streakBonus
        val (xp, breakdown) = manager.calculateXp(
            correctAnswers = 0,
            bestStreak = 10,
            completedAll = false
        )

        assertEquals(10, breakdown.streakBonus)
    }

    @Test
    fun `calculateXp partida perfecta agrega XP_PERFECT_GAME y XP_WIN_BONUS`() {
        val (xp, breakdown) = manager.calculateXp(
            correctAnswers = 10,
            bestStreak = 0,
            completedAll = true
        )

        assertEquals(100, breakdown.perfectBonus) // XP_PERFECT_GAME
        assertEquals(25, breakdown.winBonus)       // XP_WIN_BONUS
        assertEquals(225, xp) // 100 base + 100 perfect + 25 win
    }

    @Test
    fun `calculateXp completedAll con 0 correctas no otorga perfectBonus`() {
        // perfectBonus requiere completedAll && correctAnswers > 0
        val (xp, breakdown) = manager.calculateXp(
            correctAnswers = 0,
            bestStreak = 0,
            completedAll = true
        )

        assertEquals(0, breakdown.perfectBonus)
        assertEquals(25, breakdown.winBonus) // winBonus si aplica
        assertEquals(25, xp)
    }

    @Test
    fun `calculateXp modeMultiplier siempre es 1_0f en AdivinaBandera`() {
        val (_, breakdown) = manager.calculateXp(
            correctAnswers = 10,
            bestStreak = 15,
            completedAll = true
        )

        assertEquals(1.0f, breakdown.modeMultiplier)
    }

    @Test
    fun `calculateXp resultado total es consistente con la formula`() {
        // base = 8*10 = 80, streak = (7/5)*5 = 5, perfect = 100, win = 25 => total = 210
        val (xp, breakdown) = manager.calculateXp(
            correctAnswers = 8,
            bestStreak = 7,
            completedAll = true
        )

        val expectedTotal = (breakdown.base + breakdown.streakBonus +
                breakdown.perfectBonus + breakdown.winBonus) * breakdown.modeMultiplier
        assertEquals(expectedTotal.toInt(), xp)
    }

    // ===========================
    // Constantes y LEVEL_THRESHOLDS
    // ===========================

    @Test
    fun `LEVEL_THRESHOLDS tiene exactamente 50 umbrales`() {
        assertEquals(50, ProgressionManager.LEVEL_THRESHOLDS.size)
    }

    @Test
    fun `LEVEL_THRESHOLDS primer umbral es 0`() {
        assertEquals(0, ProgressionManager.LEVEL_THRESHOLDS[0])
    }

    @Test
    fun `LEVEL_THRESHOLDS segundo umbral es 100`() {
        assertEquals(100, ProgressionManager.LEVEL_THRESHOLDS[1])
    }

    @Test
    fun `LEVEL_THRESHOLDS ultimo umbral nivel 50 es 393000`() {
        assertEquals(393000, ProgressionManager.LEVEL_THRESHOLDS[49])
    }

    @Test
    fun `LEVEL_THRESHOLDS esta en orden estrictamente creciente`() {
        val thresholds = ProgressionManager.LEVEL_THRESHOLDS
        for (i in 0 until thresholds.size - 1) {
            assertTrue(
                "Umbral en indice $i (${thresholds[i]}) debe ser estrictamente menor al siguiente (${thresholds[i + 1]})",
                thresholds[i] < thresholds[i + 1]
            )
        }
    }
}
