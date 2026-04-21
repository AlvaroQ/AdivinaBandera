package com.alvaroquintana.data.datasource

interface PreferencesDataSource {
    suspend fun getPersonalRecord(gameMode: String = "Classic"): Int
    suspend fun savePersonalRecord(record: Int, gameMode: String = "Classic")
    suspend fun getSoundEnabled(): Boolean
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun getThemeMode(): String
    suspend fun setThemeMode(mode: String)
}
