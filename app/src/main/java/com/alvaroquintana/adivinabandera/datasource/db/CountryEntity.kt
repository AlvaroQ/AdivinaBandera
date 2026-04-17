package com.alvaroquintana.adivinabandera.datasource.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "countries")
data class CountryEntity(
    @PrimaryKey val id: Int = 0,
    val name: String = "",
    val icon: String = "",
    val alpha2Code: String = "",
    val capital: String = "",
    val region: String = "",
    val flag: String = "",
    val callingCodes: String = "",
    val population: Int = 0,
    val area: Int = 0,
    val currencies: String = "",
    val languages: String = "",
    val demonym: String = "",
    val borders: String = "",
    val alpha3Code: String = "",
    val subregion: String = "",
    val nativeName: String = "",
    val gini: Double = 0.0,
    val timezones: String = "",
    val latlng: String = "",
    val topLevelDomain: String = "",
    val translations: String = "",
    val altSpellings: String = "",
    val numericCode: String = ""
)
