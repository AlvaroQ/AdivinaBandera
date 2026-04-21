package com.alvaroquintana.adivinabandera.datasource.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "country_stats")
data class CountryStatsEntity(
    @PrimaryKey val alpha2Code: String,
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
    val firstDiscoveredTimestamp: Long = 0L,
    val lastSeenTimestamp: Long = 0L,
    val lastSeenInMode: String = ""
)
