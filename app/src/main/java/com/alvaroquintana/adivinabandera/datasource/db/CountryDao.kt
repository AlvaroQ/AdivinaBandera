package com.alvaroquintana.adivinabandera.datasource.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CountryDao {

    @Query("SELECT * FROM countries ORDER BY id ASC")
    suspend fun getAll(): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE id = :id")
    suspend fun getById(id: Int): CountryEntity?

    @Query("SELECT * FROM countries ORDER BY id ASC LIMIT :limit OFFSET :offset")
    suspend fun getPaginated(limit: Int, offset: Int): List<CountryEntity>

    @Query("SELECT COUNT(*) FROM countries")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(countries: List<CountryEntity>)

    @Query("DELETE FROM countries")
    suspend fun deleteAll()

    @Query("SELECT * FROM countries ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandom(count: Int): List<CountryEntity>

    @Query("SELECT id FROM countries WHERE alpha2Code = :alpha2Code LIMIT 1")
    suspend fun getIdByAlpha2Code(alpha2Code: String): Int?
}
