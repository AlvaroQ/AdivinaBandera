package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.RankingRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetRecordScoreTest {

    private lateinit var rankingRepository: RankingRepository
    private lateinit var useCase: GetRecordScore

    @Before
    fun setUp() {
        rankingRepository = mockk()
        useCase = GetRecordScore(rankingRepository)
    }

    @Test
    fun `invoke delegates to repository with default gameMode`() = runTest {
        val expected = "Alice - 500 pts"
        coEvery { rankingRepository.getWorldRecords(10L, "Classic") } returns expected

        val result = useCase.invoke(10L)

        assertEquals(expected, result)
        coVerify { rankingRepository.getWorldRecords(10L, "Classic") }
    }

    @Test
    fun `invoke delegates to repository with explicit gameMode`() = runTest {
        val expected = "Bob - 300 pts"
        coEvery { rankingRepository.getWorldRecords(5L, "CapitalByFlag") } returns expected

        val result = useCase.invoke(5L, "CapitalByFlag")

        assertEquals(expected, result)
        coVerify { rankingRepository.getWorldRecords(5L, "CapitalByFlag") }
    }

    @Test
    fun `invoke propagates exception`() = runTest {
        coEvery { rankingRepository.getWorldRecords(any(), any()) } throws RuntimeException("error")

        org.junit.Assert.assertThrows(RuntimeException::class.java) {
            runBlocking { useCase.invoke(10L) }
        }
    }
}
