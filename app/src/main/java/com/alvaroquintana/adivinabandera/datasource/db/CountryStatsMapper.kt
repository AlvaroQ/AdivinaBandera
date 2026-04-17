package com.alvaroquintana.adivinabandera.datasource.db

import com.alvaroquintana.domain.CountryStats

fun CountryStatsEntity.toDomain(): CountryStats = CountryStats(
    alpha2Code = alpha2Code,
    timesCorrect = timesCorrect,
    timesWrong = timesWrong,
    firstDiscoveredTimestamp = firstDiscoveredTimestamp,
    lastSeenTimestamp = lastSeenTimestamp,
    lastSeenInMode = lastSeenInMode
)

fun CountryStats.toEntity(): CountryStatsEntity = CountryStatsEntity(
    alpha2Code = alpha2Code,
    timesCorrect = timesCorrect,
    timesWrong = timesWrong,
    firstDiscoveredTimestamp = firstDiscoveredTimestamp,
    lastSeenTimestamp = lastSeenTimestamp,
    lastSeenInMode = lastSeenInMode
)
