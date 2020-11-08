package com.alvaroquintana.data.repository

import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.Country

class CountryByIdRepository(private val dataBaseSource: DataBaseSource) {

    suspend fun getCountryById(id: Int): Country = dataBaseSource.getCountryById(id)

}