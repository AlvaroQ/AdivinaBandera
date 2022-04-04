package com.alvaroquintana.data.repository

import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.Country

class CountryRepository(private val dataBaseSource: DataBaseSource) {
    suspend fun getCountryById(id: Int): Country = dataBaseSource.getCountryById(id)
    suspend fun getCountryList(currentPage: Int): MutableList<Country> = dataBaseSource.getCountryList(currentPage)

}