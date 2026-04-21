package com.alvaroquintana.adivinabandera.datasource.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subdivisions")
data class SubdivisionEntity(
    @PrimaryKey val id: String = "",
    val countryAlpha2: String = "",
    val name: String = "",
    val type: String = "",
    val flagUrl: String = "",
    val difficulty: String = ""
)
