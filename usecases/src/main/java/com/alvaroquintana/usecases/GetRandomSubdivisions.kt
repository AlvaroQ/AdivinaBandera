package com.alvaroquintana.usecases

import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.CountrySubdivision
import dev.zacsweers.metro.Inject

@Inject
class GetRandomSubdivisions(private val dataBaseSource: DataBaseSource) {
    suspend operator fun invoke(count: Int): List<CountrySubdivision> = dataBaseSource.getRandomSubdivisions(count)
}

@Inject
class GetSubdivisionsForCountry(private val dataBaseSource: DataBaseSource) {
    suspend operator fun invoke(alpha2: String): List<CountrySubdivision> = dataBaseSource.getSubdivisionsForCountry(alpha2)
}

@Inject
class GetSubdivisionCountryCodesWithMinCount(private val dataBaseSource: DataBaseSource) {
    suspend operator fun invoke(min: Int): List<String> = dataBaseSource.getSubdivisionCountryCodesWithMinCount(min)
}
