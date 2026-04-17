package com.alvaroquintana.data.datasource

import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.CountryStats

interface DataBaseSource {
    suspend fun getCountryById(id: Int): Country
    suspend fun getCountryList(currentPage: Int): MutableList<Country>
    suspend fun getRandomCountries(count: Int): List<Country>
    suspend fun upsertCountryStat(stat: CountryStats)
    suspend fun getCountryStatByCode(code: String): CountryStats?
    suspend fun getAllCountryStats(): List<CountryStats>
    suspend fun getTopWrongCountries(limit: Int): List<CountryStats>
    suspend fun getDiscoveredCountriesCount(): Int
    suspend fun getCountryIdByAlpha2Code(alpha2Code: String): Int?
}