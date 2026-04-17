package com.alvaroquintana.adivinabandera.datasource.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SubdivisionDao {

    @Query("SELECT COUNT(*) FROM subdivisions")
    suspend fun count(): Int

    @Query("SELECT * FROM subdivisions WHERE id = :id")
    suspend fun getById(id: String): SubdivisionEntity?

    @Query("SELECT * FROM subdivisions WHERE countryAlpha2 = :countryAlpha2")
    suspend fun getByCountry(countryAlpha2: String): List<SubdivisionEntity>

    @Query("SELECT * FROM subdivisions")
    suspend fun getAll(): List<SubdivisionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SubdivisionEntity>)

    @Query("DELETE FROM subdivisions")
    suspend fun clear()

    @Query("SELECT DISTINCT countryAlpha2 FROM subdivisions")
    suspend fun getDistinctCountryCodes(): List<String>

    @Query("SELECT COUNT(*) FROM subdivisions WHERE countryAlpha2 = :countryAlpha2")
    suspend fun countByCountry(countryAlpha2: String): Int
}
