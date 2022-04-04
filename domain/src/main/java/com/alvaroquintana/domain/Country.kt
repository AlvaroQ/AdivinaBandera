package com.alvaroquintana.domain

data class Country(
    var name: String? = "",
    var icon: String? = "",
    var alpha2Code: String? = "",
    var capital: String? = "",
    var region: String? = "",
    var flag: String? = "",
    var callingCodes: MutableList<String> = mutableListOf(),
    var population: Int? = 0,
    var area: Int? = 0,
    var currencies: MutableList<Currency> = mutableListOf(),
    var languages: MutableList<Language> = mutableListOf()
)