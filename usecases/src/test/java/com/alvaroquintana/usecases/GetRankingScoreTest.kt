package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.RankingRepository
import com.alvaroquintana.domain.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetRankingScoreTest {

    private lateinit var rankingRepository: RankingRepository
    private lateinit var useCase: GetRankingScore

    @Before
    fun setUp() {
        rankingRepository = mockk()
        useCase = GetRankingScore(rankingRepository)
    }

    @Test
    fun `invoke delegates to repository with default gameMode`() = runTest {
        val expected = mutableListOf(
            User(name = "Alice", score = 100),
            User(name = "Bob", score = 80)
        )
        coEvery { rankingRepository.getRanking("Classic") } returns expected

        val result = useCase.invoke()

        assertEquals(expected, result)
        coVerify { rankingRepository.getRanking("Classic") }
    }

    @Test
    fun `invoke delegates to repository with explicit gameMode`() = runTest {
        val expected = mutableListOf(
            User(name = "Alice", score = 100)
        )
        coEvery { rankingRepository.getRanking("CapitalByFlag") } returns expected

        val result = useCase.invoke("CapitalByFlag")

        assertEquals(expected, result)
        coVerify { rankingRepository.getRanking("CapitalByFlag") }
    }

    @Test
    fun `invoke propagates exception`() = runTest {
        coEvery { rankingRepository.getRanking(any()) } throws RuntimeException("error")

        org.junit.Assert.assertThrows(RuntimeException::class.java) {
            runBlocking { useCase.invoke() }
        }
    }
}
