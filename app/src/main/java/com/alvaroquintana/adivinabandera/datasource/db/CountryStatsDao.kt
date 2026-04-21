package com.alvaroquintana.adivinabandera.datasource.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface CountryStatsDao {

    @Query("SELECT * FROM country_stats WHERE alpha2Code = :code")
    suspend fun getStatByCode(code: String): CountryStatsEntity?

    @Query("SELECT * FROM country_stats")
    suspend fun getAllStats(): List<CountryStatsEntity>

    @Query("SELECT * FROM country_stats ORDER BY timesWrong DESC LIMIT :limit")
    suspend fun getTopWrongCountries(limit: Int): List<CountryStatsEntity>

    @Query("SELECT COUNT(*) FROM country_stats WHERE timesCorrect > 0")
    suspend fun getDiscoveredCount(): Int

    @Upsert
    suspend fun upsertStat(stat: CountryStatsEntity)
}
