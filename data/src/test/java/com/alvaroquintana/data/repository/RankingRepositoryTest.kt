package com.alvaroquintana.data.repository

import com.alvaroquintana.data.datasource.FirestoreDataSource
import com.alvaroquintana.domain.User
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.fail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RankingRepositoryTest {

    @MockK
    lateinit var firestoreDataSource: FirestoreDataSource

    private lateinit var repository: RankingRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = RankingRepositoryImpl(firestoreDataSource)
    }

    // region addRecord

    @Test
    fun `addRecord delegates to firestoreDataSource with default gameMode`() = runTest {
        val user = User(name = "Alvaro", points = "500", score = 10)
        val expected = Result.success(user)
        coEvery { firestoreDataSource.addRecord(user, "Classic") } returns expected

        val result = repository.addRecord(user)

        assertEquals(expected, result)
        coVerify(exactly = 1) { firestoreDataSource.addRecord(user, "Classic") }
    }

    @Test
    fun `addRecord delegates with explicit gameMode`() = runTest {
        val user = User(name = "Alvaro", points = "500", score = 10)
        val expected = Result.success(user)
        coEvery { firestoreDataSource.addRecord(user, "CapitalByFlag") } returns expected

        val result = repository.addRecord(user, "CapitalByFlag")

        assertEquals(expected, result)
        coVerify(exactly = 1) { firestoreDataSource.addRecord(user, "CapitalByFlag") }
    }

    @Test
    fun `addRecord propagates exception from firestoreDataSource`() = runTest {
        val user = User(name = "Alvaro", points = "500", score = 10)
        coEvery { firestoreDataSource.addRecord(any(), any()) } throws RuntimeException("firestore error")

        try {
            repository.addRecord(user)
            fail("Should throw RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("firestore error", e.message)
        }
    }

    // endregion

    // region getRanking

    @Test
    fun `getRanking delegates to firestoreDataSource with default gameMode`() = runTest {
        val expected = mutableListOf(
            User(name = "Alvaro", points = "500", score = 10),
            User(name = "Maria", points = "300", score = 6)
        )
        coEvery { firestoreDataSource.getRanking("Classic") } returns expected

        val result = repository.getRanking()

        assertEquals(expected, result)
        coVerify(exactly = 1) { firestoreDataSource.getRanking("Classic") }
    }

    @Test
    fun `getRanking propagates exception from firestoreDataSource`() = runTest {
        coEvery { firestoreDataSource.getRanking(any()) } throws RuntimeException("ranking error")

        try {
            repository.getRanking()
            fail("Should throw RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("ranking error", e.message)
        }
    }

    // endregion

    // region getWorldRecords

    @Test
    fun `getWorldRecords delegates to firestoreDataSource with default gameMode`() = runTest {
        val expected = "Alvaro:500"
        coEvery { firestoreDataSource.getWorldRecords(10L, "Classic") } returns expected

        val result = repository.getWorldRecords(10L)

        assertEquals(expected, result)
        coVerify(exactly = 1) { firestoreDataSource.getWorldRecords(10L, "Classic") }
    }

    @Test
    fun `getWorldRecords delegates with explicit gameMode`() = runTest {
        val expected = "Maria:200"
        coEvery { firestoreDataSource.getWorldRecords(5L, "CapitalByFlag") } returns expected

        val result = repository.getWorldRecords(5L, "CapitalByFlag")

        assertEquals(expected, result)
        coVerify(exactly = 1) { firestoreDataSource.getWorldRecords(5L, "CapitalByFlag") }
    }

    @Test
    fun `getWorldRecords propagates exception from firestoreDataSource`() = runTest {
        coEvery { firestoreDataSource.getWorldRecords(any(), any()) } throws RuntimeException("records error")

        try {
            repository.getWorldRecords(10L)
            fail("Should throw RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("records error", e.message)
        }
    }

    // endregion
}
