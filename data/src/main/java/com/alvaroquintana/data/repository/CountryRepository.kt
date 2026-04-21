package com.alvaroquintana.data.repository

import com.alvaroquintana.domain.Country

interface CountryRepository {
    suspend fun getCountryById(id: Int): Country
    suspend fun getCountryList(currentPage: Int): MutableList<Country>
    suspend fun getRandomCountries(count: Int): List<Country>
}
