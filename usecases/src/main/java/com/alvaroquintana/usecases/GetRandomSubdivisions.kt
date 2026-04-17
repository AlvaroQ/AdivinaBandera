package com.alvaroquintana.usecases

import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.CountrySubdivision

class GetRandomSubdivisions(private val dataBaseSource: DataBaseSource) {
    suspend operator fun invoke(count: Int): List<CountrySubdivision> = dataBaseSource.getRandomSubdivisions(count)
}

class GetSubdivisionsForCountry(private val dataBaseSource: DataBaseSource) {
    suspend operator fun invoke(alpha2: String): List<CountrySubdivision> = dataBaseSource.getSubdivisionsForCountry(alpha2)
}

class GetSubdivisionCountryCodesWithMinCount(private val dataBaseSource: DataBaseSource) {
    suspend operator fun invoke(min: Int): List<String> = dataBaseSource.getSubdivisionCountryCodesWithMinCount(min)
}
