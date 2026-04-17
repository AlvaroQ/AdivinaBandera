package com.alvaroquintana.domain.challenge

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeRewardTest {

    // ===========================
    // forDifficulty — valores por dificultad
    // ===========================

    @Test
    fun `forDifficulty EASY retorna 25 XP y 5 coins`() {
        val reward = ChallengeReward.forDifficulty(ChallengeDifficulty.EASY)

        assertEquals(25, reward.xp)
        assertEquals(5, reward.coins)
    }

    @Test
    fun `forDifficulty MEDIUM retorna 50 XP y 10 coins`() {
        val reward = ChallengeReward.forDifficulty(ChallengeDifficulty.MEDIUM)

        assertEquals(50, reward.xp)
        assertEquals(10, reward.coins)
    }

    @Test
    fun `forDifficulty HARD retorna 100 XP y 20 coins`() {
        val reward = ChallengeReward.forDifficulty(ChallengeDifficulty.HARD)

        assertEquals(100, reward.xp)
        assertEquals(20, reward.coins)
    }

    @Test
    fun `forDifficulty WEEKLY retorna 200 XP y 50 coins`() {
        val reward = ChallengeReward.forDifficulty(ChallengeDifficulty.WEEKLY)

        assertEquals(200, reward.xp)
        assertEquals(50, reward.coins)
    }

    @Test
    fun `recompensas aumentan proporcionalmente por dificultad`() {
        val easy = ChallengeReward.forDifficulty(ChallengeDifficulty.EASY)
        val medium = ChallengeReward.forDifficulty(ChallengeDifficulty.MEDIUM)
        val hard = ChallengeReward.forDifficulty(ChallengeDifficulty.HARD)
        val weekly = ChallengeReward.forDifficulty(ChallengeDifficulty.WEEKLY)

        assertTrue("MEDIUM XP debe superar EASY", medium.xp > easy.xp)
        assertTrue("HARD XP debe superar MEDIUM", hard.xp > medium.xp)
        assertTrue("WEEKLY XP debe superar HARD", weekly.xp > hard.xp)

        assertTrue("MEDIUM coins debe superar EASY", medium.coins > easy.coins)
        assertTrue("HARD coins debe superar MEDIUM", hard.coins > medium.coins)
        assertTrue("WEEKLY coins debe superar HARD", weekly.coins > hard.coins)
    }

    // ===========================
    // ALL_DAILY_COMPLETE_BONUS
    // ===========================

    @Test
    fun `ALL_DAILY_COMPLETE_BONUS retorna 75 XP`() {
        assertEquals(75, ChallengeReward.ALL_DAILY_COMPLETE_BONUS.xp)
    }

    @Test
    fun `ALL_DAILY_COMPLETE_BONUS retorna 15 coins`() {
        assertEquals(15, ChallengeReward.ALL_DAILY_COMPLETE_BONUS.coins)
    }

    @Test
    fun `ALL_DAILY_COMPLETE_BONUS supera la recompensa de EASY`() {
        val easy = ChallengeReward.forDifficulty(ChallengeDifficulty.EASY)
        assertTrue(ChallengeReward.ALL_DAILY_COMPLETE_BONUS.xp > easy.xp)
        assertTrue(ChallengeReward.ALL_DAILY_COMPLETE_BONUS.coins > easy.coins)
    }

    // ===========================
    // Propiedades del data class
    // ===========================

    @Test
    fun `ChallengeReward es data class con igualdad por valor`() {
        val reward1 = ChallengeReward(xp = 25, coins = 5)
        val reward2 = ChallengeReward(xp = 25, coins = 5)

        assertEquals(reward1, reward2)
    }

    @Test
    fun `todas las dificultades tienen XP positivo`() {
        ChallengeDifficulty.entries.forEach { difficulty ->
            val reward = ChallengeReward.forDifficulty(difficulty)
            assertTrue("XP de $difficulty debe ser positivo", reward.xp > 0)
        }
    }

    @Test
    fun `todas las dificultades tienen coins positivos`() {
        ChallengeDifficulty.entries.forEach { difficulty ->
            val reward = ChallengeReward.forDifficulty(difficulty)
            assertTrue("Coins de $difficulty deben ser positivos", reward.coins > 0)
        }
    }
}
