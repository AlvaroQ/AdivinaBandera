package com.alvaroquintana.domain

data class CountrySubdivision(
    val id: String,
    val countryAlpha2: String,
    val name: String,
    val type: String,
    val flagUrl: String,
    val difficulty: String
)
