package com.alvaroquintana.adivinabandera.datasource.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CountryEntity::class, SyncMetadata::class, CountryStatsEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun countryDao(): CountryDao
    abstract fun syncMetadataDao(): SyncMetadataDao
    abstract fun countryStatsDao(): CountryStatsDao
}
