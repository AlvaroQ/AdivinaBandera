package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.CountryByIdRepository
import com.alvaroquintana.domain.Country

class GetCountryById(private val countryByIdRepository: CountryByIdRepository) {

    suspend fun invoke(id: Int): Country = countryByIdRepository.getCountryById(id)

}
