package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.CountryRepository
import com.alvaroquintana.domain.Country

class GetCountryById(private val countryRepository: CountryRepository) {
    suspend fun invoke(id: Int): Country = countryRepository.getCountryById(id)
}
class GetCountryList(private val countryRepository: CountryRepository) {
    suspend fun invoke(currentPage: Int): MutableList<Country> = countryRepository.getCountryList(currentPage)
}
