package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.CountryRepository
import com.alvaroquintana.domain.Country
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetCountryByIdTest {

    private lateinit var countryRepository: CountryRepository
    private lateinit var useCase: GetCountryById

    @Before
    fun setUp() {
        countryRepository = mockk()
        useCase = GetCountryById(countryRepository)
    }

    @Test
    fun `invoke delegates to repository`() = runTest {
        val expected = Country(name = "Argentina", alpha2Code = "AR")
        coEvery { countryRepository.getCountryById(1) } returns expected

        val result = useCase.invoke(1)

        assertEquals(expected, result)
        coVerify { countryRepository.getCountryById(1) }
    }

    @Test
    fun `invoke propagates exception`() = runTest {
        coEvery { countryRepository.getCountryById(any()) } throws RuntimeException("error")

        org.junit.Assert.assertThrows(RuntimeException::class.java) {
            runBlocking { useCase.invoke(1) }
        }
    }
}

class GetCountryListTest {

    private lateinit var countryRepository: CountryRepository
    private lateinit var useCase: GetCountryList

    @Before
    fun setUp() {
        countryRepository = mockk()
        useCase = GetCountryList(countryRepository)
    }

    @Test
    fun `invoke delegates to repository`() = runTest {
        val expected = mutableListOf(Country(name = "Argentina"), Country(name = "Brazil"))
        coEvery { countryRepository.getCountryList(0) } returns expected

        val result = useCase.invoke(0)

        assertEquals(expected, result)
        coVerify { countryRepository.getCountryList(0) }
    }

    @Test
    fun `invoke propagates exception`() = runTest {
        coEvery { countryRepository.getCountryList(any()) } throws RuntimeException("error")

        org.junit.Assert.assertThrows(RuntimeException::class.java) {
            runBlocking { useCase.invoke(0) }
        }
    }
}

class GetRandomCountriesTest {

    private lateinit var countryRepository: CountryRepository
    private lateinit var useCase: GetRandomCountries

    @Before
    fun setUp() {
        countryRepository = mockk()
        useCase = GetRandomCountries(countryRepository)
    }

    @Test
    fun `invoke delegates to repository`() = runTest {
        val expected = listOf(Country(name = "Argentina"), Country(name = "Brazil"))
        coEvery { countryRepository.getRandomCountries(2) } returns expected

        val result = useCase.invoke(2)

        assertEquals(expected, result)
        coVerify { countryRepository.getRandomCountries(2) }
    }

    @Test
    fun `invoke propagates exception`() = runTest {
        coEvery { countryRepository.getRandomCountries(any()) } throws RuntimeException("error")

        org.junit.Assert.assertThrows(RuntimeException::class.java) {
            runBlocking { useCase.invoke(2) }
        }
    }
}
