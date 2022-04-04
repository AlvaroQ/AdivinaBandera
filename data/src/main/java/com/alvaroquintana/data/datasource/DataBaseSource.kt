package com.alvaroquintana.data.datasource

import com.alvaroquintana.domain.App
import com.alvaroquintana.domain.Country

interface DataBaseSource {
    suspend fun getCountryById(id: Int): Country
    suspend fun getCountryList(currentPage: Int): MutableList<Country>
    suspend fun getAppsRecommended(): MutableList<App>
}