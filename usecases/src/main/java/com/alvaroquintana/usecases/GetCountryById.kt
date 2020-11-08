package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.CountryByIdRepository
import com.alvaroquintana.domain.Country

class GetCountryById(private val breedByIdRepository: CountryByIdRepository) {

    suspend fun invoke(id: Int): Country = breedByIdRepository.getCountryById(id)

}
