package com.alvaroquintana.adivinabandera.datasource.db

import com.alvaroquintana.domain.CountrySubdivision

fun SubdivisionEntity.toDomain(): CountrySubdivision = CountrySubdivision(
    id = id,
    countryAlpha2 = countryAlpha2,
    name = name,
    type = type,
    flagUrl = flagUrl,
    difficulty = difficulty
)

fun CountrySubdivision.toEntity(): SubdivisionEntity = SubdivisionEntity(
    id = id,
    countryAlpha2 = countryAlpha2,
    name = name,
    type = type,
    flagUrl = flagUrl,
    difficulty = difficulty
)
