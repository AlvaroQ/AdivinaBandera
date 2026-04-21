package com.alvaroquintana.usecases

import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.CountrySubdivision
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRandomSubdivisionsTest {

    private lateinit var dataBaseSource: DataBaseSource
    private lateinit var useCase: GetRandomSubdivisions

    @Before
    fun setUp() {
        dataBaseSource = mockk()
        useCase = GetRandomSubdivisions(dataBaseSource)
    }

    @Test
    fun `invoke returns same list as data source`() = runTest {
        val expected = listOf(
            CountrySubdivision(id = "ES-A", countryAlpha2 = "ES", name = "Almería", type = "Province", flagUrl = "", difficulty = "medium"),
            CountrySubdivision(id = "ES-B", countryAlpha2 = "ES", name = "Barcelona", type = "Province", flagUrl = "", difficulty = "easy"),
            CountrySubdivision(id = "FR-1", countryAlpha2 = "FR", name = "Ain", type = "Department", flagUrl = "", difficulty = "hard")
        )
        coEvery { dataBaseSource.getRandomSubdivisions(3) } returns expected

        val result = useCase.invoke(3)

        assertEquals(expected, result)
        coVerify(exactly = 1) { dataBaseSource.getRandomSubdivisions(3) }
    }

    @Test
    fun `invoke returns empty list when data source returns empty`() = runTest {
        coEvery { dataBaseSource.getRandomSubdivisions(any()) } returns emptyList()

        val result = useCase.invoke(10)

        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { dataBaseSource.getRandomSubdivisions(10) }
    }

    @Test
    fun `invoke propagates exception from data source`() = runTest {
        coEvery { dataBaseSource.getRandomSubdivisions(any()) } throws RuntimeException("db error")

        val thrown = runCatching { useCase.invoke(4) }

        assertTrue(thrown.isFailure)
        assertEquals("db error", thrown.exceptionOrNull()?.message)
    }
}

class GetSubdivisionsForCountryTest {

    private lateinit var dataBaseSource: DataBaseSource
    private lateinit var useCase: GetSubdivisionsForCountry

    @Before
    fun setUp() {
        dataBaseSource = mockk()
        useCase = GetSubdivisionsForCountry(dataBaseSource)
    }

    @Test
    fun `invoke delegates to data source with given alpha2`() = runTest {
        val expected = listOf(
            CountrySubdivision(id = "ES-A", countryAlpha2 = "ES", name = "Almería", type = "Province", flagUrl = "", difficulty = "medium"),
            CountrySubdivision(id = "ES-B", countryAlpha2 = "ES", name = "Barcelona", type = "Province", flagUrl = "", difficulty = "easy")
        )
        coEvery { dataBaseSource.getSubdivisionsForCountry("ES") } returns expected

        val result = useCase.invoke("ES")

        assertEquals(expected, result)
        coVerify(exactly = 1) { dataBaseSource.getSubdivisionsForCountry("ES") }
    }

    @Test
    fun `invoke returns empty list when data source returns empty`() = runTest {
        coEvery { dataBaseSource.getSubdivisionsForCountry(any()) } returns emptyList()

        val result = useCase.invoke("XX")

        assertTrue(result.isEmpty())
    }
}

class GetSubdivisionCountryCodesWithMinCountTest {

    private lateinit var dataBaseSource: DataBaseSource
    private lateinit var useCase: GetSubdivisionCountryCodesWithMinCount

    @Before
    fun setUp() {
        dataBaseSource = mockk()
        useCase = GetSubdivisionCountryCodesWithMinCount(dataBaseSource)
    }

    @Test
    fun `invoke returns country codes from data source`() = runTest {
        val expected = listOf("ES", "FR", "DE")
        coEvery { dataBaseSource.getSubdivisionCountryCodesWithMinCount(4) } returns expected

        val result = useCase.invoke(4)

        assertEquals(expected, result)
        coVerify(exactly = 1) { dataBaseSource.getSubdivisionCountryCodesWithMinCount(4) }
    }

    @Test
    fun `invoke returns empty list when no country has enough subdivisions`() = runTest {
        coEvery { dataBaseSource.getSubdivisionCountryCodesWithMinCount(any()) } returns emptyList()

        val result = useCase.invoke(100)

        assertTrue(result.isEmpty())
    }
}
