package com.alvaroquintana.adivinabandera.datasource.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "sync_metadata")
data class SyncMetadata(
    @PrimaryKey val collection: String,
    val lastSyncTimestamp: Long = 0L
)

@Dao
interface SyncMetadataDao {

    @Query("SELECT * FROM sync_metadata WHERE collection = :collection")
    suspend fun getByCollection(collection: String): SyncMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(metadata: SyncMetadata)
}
