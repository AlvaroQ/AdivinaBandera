package com.alvaroquintana.data.repository

import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.Country

class CountryRepositoryImpl(private val dataBaseSource: DataBaseSource) : CountryRepository {
    override suspend fun getCountryById(id: Int): Country = dataBaseSource.getCountryById(id)
    override suspend fun getCountryList(currentPage: Int): MutableList<Country> = dataBaseSource.getCountryList(currentPage)
    override suspend fun getRandomCountries(count: Int): List<Country> = dataBaseSource.getRandomCountries(count)
}
