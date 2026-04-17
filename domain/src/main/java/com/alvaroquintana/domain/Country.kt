package com.alvaroquintana.domain

data class Country(
    val name: String = "",
    val icon: String = "",
    val alpha2Code: String = "",
    val capital: String = "",
    val region: String = "",
    val flag: String = "",
    val callingCodes: List<String> = emptyList(),
    val population: Int = 0,
    val area: Int = 0,
    val currencies: List<Currency> = emptyList(),
    val languages: List<Language> = emptyList(),
    val demonym: String = "",
    val borders: List<String> = emptyList(),
    val alpha3Code: String = "",
    val subregion: String = "",
    val nativeName: String = "",
    val gini: Double? = null,
    val timezones: List<String> = emptyList(),
    val latlng: List<Double> = emptyList(),
    val topLevelDomain: List<String> = emptyList(),
    val translations: Map<String, String> = emptyMap(),
    val altSpellings: List<String> = emptyList(),
    val numericCode: String = ""
)
