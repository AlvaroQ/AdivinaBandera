package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.CountryRepository
import com.alvaroquintana.domain.Country
import dev.zacsweers.metro.Inject

@Inject
class GetCountryById(private val countryRepository: CountryRepository) {
    suspend fun invoke(id: Int): Country = countryRepository.getCountryById(id)
}

@Inject
class GetCountryList(private val countryRepository: CountryRepository) {
    suspend fun invoke(currentPage: Int): MutableList<Country> = countryRepository.getCountryList(currentPage)
}

@Inject
class GetRandomCountries(private val countryRepository: CountryRepository) {
    suspend fun invoke(count: Int): List<Country> = countryRepository.getRandomCountries(count)
}
