package com.alvaroquintana.data.repository

import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.Country
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.fail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CountryRepositoryTest {

    @MockK
    lateinit var dataBaseSource: DataBaseSource

    private lateinit var repository: CountryRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        repository = CountryRepositoryImpl(dataBaseSource)
    }

    // region getCountryById

    @Test
    fun `getCountryById delegates to dataBaseSource`() = runTest {
        val expected = Country(name = "España", alpha2Code = "ES")
        coEvery { dataBaseSource.getCountryById(1) } returns expected

        val result = repository.getCountryById(1)

        assertEquals(expected, result)
        coVerify(exactly = 1) { dataBaseSource.getCountryById(1) }
    }

    @Test
    fun `getCountryById propagates exception from dataBaseSource`() = runTest {
        coEvery { dataBaseSource.getCountryById(any()) } throws RuntimeException("db error")

        try {
            repository.getCountryById(1)
            fail("Should throw RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("db error", e.message)
        }
    }

    // endregion

    // region getCountryList

    @Test
    fun `getCountryList delegates to dataBaseSource`() = runTest {
        val expected = mutableListOf(
            Country(name = "España", alpha2Code = "ES"),
            Country(name = "Francia", alpha2Code = "FR")
        )
        coEvery { dataBaseSource.getCountryList(0) } returns expected

        val result = repository.getCountryList(0)

        assertEquals(expected, result)
        coVerify(exactly = 1) { dataBaseSource.getCountryList(0) }
    }

    @Test
    fun `getCountryList propagates exception from dataBaseSource`() = runTest {
        coEvery { dataBaseSource.getCountryList(any()) } throws RuntimeException("page error")

        try {
            repository.getCountryList(0)
            fail("Should throw RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("page error", e.message)
        }
    }

    // endregion

    // region getRandomCountries

    @Test
    fun `getRandomCountries delegates to dataBaseSource`() = runTest {
        val expected = listOf(
            Country(name = "Italia", alpha2Code = "IT"),
            Country(name = "Alemania", alpha2Code = "DE"),
            Country(name = "Portugal", alpha2Code = "PT")
        )
        coEvery { dataBaseSource.getRandomCountries(3) } returns expected

        val result = repository.getRandomCountries(3)

        assertEquals(expected, result)
        coVerify(exactly = 1) { dataBaseSource.getRandomCountries(3) }
    }

    @Test
    fun `getRandomCountries propagates exception from dataBaseSource`() = runTest {
        coEvery { dataBaseSource.getRandomCountries(any()) } throws RuntimeException("random error")

        try {
            repository.getRandomCountries(3)
            fail("Should throw RuntimeException")
        } catch (e: RuntimeException) {
            assertEquals("random error", e.message)
        }
    }

    // endregion
}
