package com.alvaroquintana.domain

data class CountryStats(
    val alpha2Code: String,
    val timesCorrect: Int = 0,
    val timesWrong: Int = 0,
    val firstDiscoveredTimestamp: Long = 0L,
    val lastSeenTimestamp: Long = 0L,
    val lastSeenInMode: String = ""
)
