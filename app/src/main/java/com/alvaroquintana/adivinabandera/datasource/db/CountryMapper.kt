package com.alvaroquintana.adivinabandera.datasource.db

import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.Currency
import com.alvaroquintana.domain.Language

fun CountryEntity.toDomain(): Country = Country(
    name = name,
    icon = icon,
    alpha2Code = alpha2Code,
    capital = capital,
    region = region,
    flag = flag,
    callingCodes = if (callingCodes.isBlank()) mutableListOf()
                   else callingCodes.split(",").map { it.trim() }.toMutableList(),
    population = population,
    area = area,
    currencies = if (currencies.isBlank()) mutableListOf()
                 else currencies.split(";").mapNotNull { entry ->
                     val parts = entry.split("|")
                     if (parts.size == 3) Currency(
                         code = parts[0],
                         name = parts[1],
                         symbol = parts[2]
                     ) else null
                 }.toMutableList(),
    languages = if (languages.isBlank()) mutableListOf()
                else languages.split(";").mapNotNull { entry ->
                    val parts = entry.split("|")
                    if (parts.size == 2) Language(
                        name = parts[0],
                        nativeName = parts[1]
                    ) else null
                }.toMutableList(),
    demonym = demonym,
    borders = if (borders.isBlank()) emptyList()
              else borders.split(",").map { it.trim() },
    alpha3Code = alpha3Code,
    subregion = subregion,
    nativeName = nativeName,
    gini = if (gini == 0.0) null else gini,
    timezones = if (timezones.isBlank()) emptyList()
                else timezones.split(",").map { it.trim() },
    latlng = if (latlng.isBlank()) emptyList()
             else latlng.split(",").mapNotNull { it.trim().toDoubleOrNull() },
    topLevelDomain = if (topLevelDomain.isBlank()) emptyList()
                     else topLevelDomain.split(",").map { it.trim() },
    translations = if (translations.isBlank()) emptyMap()
                   else translations.split(";").mapNotNull { entry ->
                       val parts = entry.split("|")
                       if (parts.size == 2) parts[0] to parts[1] else null
                   }.toMap(),
    altSpellings = if (altSpellings.isBlank()) emptyList()
                   else altSpellings.split(",").map { it.trim() },
    numericCode = numericCode
)

fun Country.toEntity(id: Int): CountryEntity = CountryEntity(
    id = id,
    name = name,
    icon = icon,
    alpha2Code = alpha2Code,
    capital = capital,
    region = region,
    flag = flag,
    callingCodes = callingCodes.joinToString(","),
    population = population,
    area = area,
    currencies = currencies.joinToString(";") { "${it.code}|${it.name}|${it.symbol}" },
    languages = languages.joinToString(";") { "${it.name}|${it.nativeName}" },
    demonym = demonym,
    borders = borders.joinToString(","),
    alpha3Code = alpha3Code,
    subregion = subregion,
    nativeName = nativeName,
    gini = gini ?: 0.0,
    timezones = timezones.joinToString(","),
    latlng = latlng.joinToString(","),
    topLevelDomain = topLevelDomain.joinToString(","),
    translations = translations.entries.joinToString(";") { "${it.key}|${it.value}" },
    altSpellings = altSpellings.joinToString(","),
    numericCode = numericCode
)
