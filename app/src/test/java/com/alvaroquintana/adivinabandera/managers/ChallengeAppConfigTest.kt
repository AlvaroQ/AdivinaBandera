package com.alvaroquintana.adivinabandera.managers

import com.alvaroquintana.domain.challenge.ChallengeType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests para ChallengeAppConfig.
 *
 * Punto clave a verificar: AdivinaBandera NO tiene ChallengeType.PLAY_MODE
 * (ese tipo no existe en el enum de este proyecto). El catalogo de challenges
 * solo incluye tipos validos del enum ChallengeType de AdivinaBandera.
 */
class ChallengeAppConfigTest {

    // Tipos validos en AdivinaBandera (el enum ChallengeType de este proyecto)
    private val validTypes = ChallengeType.entries.toSet()

    // ===========================
    // availableGameModes
    // ===========================

    @Test
    fun `availableGameModes contiene exactamente Classic`() {
        assertEquals(listOf("Classic"), ChallengeAppConfig.availableGameModes)
    }

    @Test
    fun `availableGameModes tiene exactamente 1 modo de juego`() {
        assertEquals(1, ChallengeAppConfig.availableGameModes.size)
    }

    @Test
    fun `availableGameModes no contiene ningun modo llamado PLAY_MODE`() {
        // AdivinaBandera tiene un solo modo: Classic. No hay PLAY_MODE.
        assertFalse(
            "AdivinaBandera no debe tener PLAY_MODE como modo disponible",
            ChallengeAppConfig.availableGameModes.any { it.equals("PLAY_MODE", ignoreCase = true) }
        )
    }

    // ===========================
    // maxQuestionsPerGame
    // ===========================

    @Test
    fun `maxQuestionsPerGame es 249`() {
        assertEquals(249, ChallengeAppConfig.maxQuestionsPerGame)
    }

    // ===========================
    // Todos los templates usan ChallengeTypes validos del enum
    // (confirma que no existe ningun tipo externo como PLAY_MODE)
    // ===========================

    @Test
    fun `todos los templates de easyTemplates usan tipos validos del enum ChallengeType`() {
        ChallengeAppConfig.easyTemplates.forEach { template ->
            assertTrue(
                "Tipo desconocido en easyTemplates: ${template.type}",
                template.type in validTypes
            )
        }
    }

    @Test
    fun `todos los templates de mediumTemplates usan tipos validos del enum ChallengeType`() {
        ChallengeAppConfig.mediumTemplates.forEach { template ->
            assertTrue(
                "Tipo desconocido en mediumTemplates: ${template.type}",
                template.type in validTypes
            )
        }
    }

    @Test
    fun `todos los templates de hardTemplates usan tipos validos del enum ChallengeType`() {
        ChallengeAppConfig.hardTemplates.forEach { template ->
            assertTrue(
                "Tipo desconocido en hardTemplates: ${template.type}",
                template.type in validTypes
            )
        }
    }

    @Test
    fun `todos los templates de weeklyTemplates usan tipos validos del enum ChallengeType`() {
        ChallengeAppConfig.weeklyTemplates.forEach { template ->
            assertTrue(
                "Tipo desconocido en weeklyTemplates: ${template.type}",
                template.type in validTypes
            )
        }
    }

    // ===========================
    // easyTemplates
    // ===========================

    @Test
    fun `easyTemplates no esta vacio`() {
        assertTrue(ChallengeAppConfig.easyTemplates.isNotEmpty())
    }

    @Test
    fun `easyTemplates todos tienen descripcion no vacia`() {
        ChallengeAppConfig.easyTemplates.forEach { template ->
            assertTrue(
                "Template ${template.type} en EASY tiene descripcion vacia",
                template.description.isNotBlank()
            )
        }
    }

    @Test
    fun `easyTemplates todos tienen baseTarget positivo`() {
        ChallengeAppConfig.easyTemplates.forEach { template ->
            assertTrue(
                "Template ${template.type} en EASY tiene baseTarget <= 0: ${template.baseTarget}",
                template.baseTarget > 0
            )
        }
    }

    // ===========================
    // mediumTemplates
    // ===========================

    @Test
    fun `mediumTemplates no esta vacio`() {
        assertTrue(ChallengeAppConfig.mediumTemplates.isNotEmpty())
    }

    @Test
    fun `mediumTemplates todos tienen descripcion no vacia`() {
        ChallengeAppConfig.mediumTemplates.forEach { template ->
            assertTrue(
                "Template ${template.type} en MEDIUM tiene descripcion vacia",
                template.description.isNotBlank()
            )
        }
    }

    @Test
    fun `mediumTemplates todos tienen baseTarget positivo`() {
        ChallengeAppConfig.mediumTemplates.forEach { template ->
            assertTrue(
                "Template ${template.type} en MEDIUM tiene baseTarget <= 0: ${template.baseTarget}",
                template.baseTarget > 0
            )
        }
    }

    // ===========================
    // hardTemplates
    // ===========================

    @Test
    fun `hardTemplates no esta vacio`() {
        assertTrue(ChallengeAppConfig.hardTemplates.isNotEmpty())
    }

    @Test
    fun `hardTemplates todos tienen descripcion no vacia`() {
        ChallengeAppConfig.hardTemplates.forEach { template ->
            assertTrue(
                "Template ${template.type} en HARD tiene descripcion vacia",
                template.description.isNotBlank()
            )
        }
    }

    @Test
    fun `hardTemplates todos tienen baseTarget positivo`() {
        ChallengeAppConfig.hardTemplates.forEach { template ->
            assertTrue(
                "Template ${template.type} en HARD tiene baseTarget <= 0: ${template.baseTarget}",
                template.baseTarget > 0
            )
        }
    }

    // ===========================
    // weeklyTemplates
    // ===========================

    @Test
    fun `weeklyTemplates no esta vacio`() {
        assertTrue(ChallengeAppConfig.weeklyTemplates.isNotEmpty())
    }

    @Test
    fun `weeklyTemplates todos tienen descripcion no vacia`() {
        ChallengeAppConfig.weeklyTemplates.forEach { template ->
            assertTrue(
                "Template ${template.type} en WEEKLY tiene descripcion vacia",
                template.description.isNotBlank()
            )
        }
    }

    @Test
    fun `weeklyTemplates todos tienen baseTarget positivo`() {
        ChallengeAppConfig.weeklyTemplates.forEach { template ->
            assertTrue(
                "Template ${template.type} en WEEKLY tiene baseTarget <= 0: ${template.baseTarget}",
                template.baseTarget > 0
            )
        }
    }

    // ===========================
    // Verificacion global de todos los templates
    // ===========================

    @Test
    fun `todos los templates de todos los niveles tienen descripcion y baseTarget validos`() {
        val allTemplates = ChallengeAppConfig.easyTemplates +
                ChallengeAppConfig.mediumTemplates +
                ChallengeAppConfig.hardTemplates +
                ChallengeAppConfig.weeklyTemplates

        allTemplates.forEach { template ->
            assertTrue(
                "Template ${template.type} tiene descripcion vacia",
                template.description.isNotBlank()
            )
            assertTrue(
                "Template ${template.type} tiene baseTarget <= 0: ${template.baseTarget}",
                template.baseTarget > 0
            )
        }
    }

    @Test
    fun `ningun template en ningun nivel usa tipo de desafio invalido`() {
        val allTemplates = ChallengeAppConfig.easyTemplates +
                ChallengeAppConfig.mediumTemplates +
                ChallengeAppConfig.hardTemplates +
                ChallengeAppConfig.weeklyTemplates

        val invalidTemplates = allTemplates.filter { it.type !in validTypes }

        assertTrue(
            "Templates con tipo invalido: ${invalidTemplates.map { it.description }}",
            invalidTemplates.isEmpty()
        )
    }

    // ===========================
    // Targets escalados: HARD y WEEKLY son mayores que EASY
    // ===========================

    @Test
    fun `GAMES_PLAYED en HARD tiene target mayor que en EASY`() {
        val easyGames = ChallengeAppConfig.easyTemplates
            .first { it.type == ChallengeType.GAMES_PLAYED }.baseTarget

        val hardGames = ChallengeAppConfig.hardTemplates
            .first { it.type == ChallengeType.GAMES_PLAYED }.baseTarget

        assertTrue(
            "HARD GAMES_PLAYED ($hardGames) debe ser mayor que EASY GAMES_PLAYED ($easyGames)",
            hardGames > easyGames
        )
    }

    @Test
    fun `TOTAL_CORRECT en HARD tiene target mayor que en EASY`() {
        val easyTarget = ChallengeAppConfig.easyTemplates
            .first { it.type == ChallengeType.TOTAL_CORRECT }.baseTarget

        val hardTarget = ChallengeAppConfig.hardTemplates
            .first { it.type == ChallengeType.TOTAL_CORRECT }.baseTarget

        assertTrue(
            "HARD TOTAL_CORRECT ($hardTarget) debe ser mayor que EASY TOTAL_CORRECT ($easyTarget)",
            hardTarget > easyTarget
        )
    }

    @Test
    fun `GAMES_PLAYED en WEEKLY tiene target mayor que en HARD`() {
        val hardGames = ChallengeAppConfig.hardTemplates
            .first { it.type == ChallengeType.GAMES_PLAYED }.baseTarget

        val weeklyGames = ChallengeAppConfig.weeklyTemplates
            .first { it.type == ChallengeType.GAMES_PLAYED }.baseTarget

        assertTrue(
            "WEEKLY GAMES_PLAYED ($weeklyGames) debe ser mayor que HARD GAMES_PLAYED ($hardGames)",
            weeklyGames > hardGames
        )
    }
}
