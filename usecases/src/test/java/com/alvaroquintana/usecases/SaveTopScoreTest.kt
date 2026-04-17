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

class SaveTopScoreTest {

    private lateinit var rankingRepository: RankingRepository
    private lateinit var useCase: SaveTopScore

    @Before
    fun setUp() {
        rankingRepository = mockk()
        useCase = SaveTopScore(rankingRepository)
    }

    @Test
    fun `invoke delegates to repository with default gameMode`() = runTest {
        val user = User(name = "Alice", score = 500)
        val expected = Result.success(user)
        coEvery { rankingRepository.addRecord(user, "Classic") } returns expected

        val result = useCase.invoke(user)

        assertEquals(expected, result)
        coVerify { rankingRepository.addRecord(user, "Classic") }
    }

    @Test
    fun `invoke delegates to repository with explicit gameMode`() = runTest {
        val user = User(name = "Alice", score = 500)
        val expected = Result.success(user)
        coEvery { rankingRepository.addRecord(user, "CapitalByCountry") } returns expected

        val result = useCase.invoke(user, "CapitalByCountry")

        assertEquals(expected, result)
        coVerify { rankingRepository.addRecord(user, "CapitalByCountry") }
    }

    @Test
    fun `invoke propagates exception`() = runTest {
        val user = User(name = "Alice", score = 500)
        coEvery { rankingRepository.addRecord(any(), any()) } throws RuntimeException("error")

        org.junit.Assert.assertThrows(RuntimeException::class.java) {
            runBlocking { useCase.invoke(user) }
        }
    }
}
