package com.alvaroquintana.adivinabandera.datasource

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alvaroquintana.adivinabandera.common.DataStoreKeys.PreferencesKeys
import com.alvaroquintana.data.datasource.PreferencesDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@dev.zacsweers.metro.ContributesBinding(dev.zacsweers.metro.AppScope::class)
@dev.zacsweers.metro.Inject
class PreferencesDataSourceImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesDataSource {

    private fun personalRecordKey(gameMode: String) = when (gameMode) {
        "CapitalByFlag" -> PreferencesKeys.PERSONAL_RECORD_CAPITAL_BY_FLAG
        "CurrencyDetective" -> PreferencesKeys.PERSONAL_RECORD_CURRENCY_DETECTIVE
        "PopulationChallenge" -> PreferencesKeys.PERSONAL_RECORD_POPULATION_CHALLENGE
        "WorldMix" -> PreferencesKeys.PERSONAL_RECORD_WORLD_MIX
        "RegionSpain",
        "RegionMexico",
        "RegionArgentina",
        "RegionBrazil",
        "RegionGermany",
        "RegionUSA" -> PreferencesKeys.PERSONAL_RECORD_REGIONAL
        else -> PreferencesKeys.PERSONAL_RECORD_CLASSIC
    }

    override suspend fun getPersonalRecord(gameMode: String): Int {
        val key = personalRecordKey(gameMode)
        return dataStore.data.map { prefs ->
            prefs[key] ?: 0
        }.first()
    }

    override suspend fun savePersonalRecord(record: Int, gameMode: String) {
        val key = personalRecordKey(gameMode)
        dataStore.edit { prefs ->
            prefs[key] = record
        }
    }

    override suspend fun getSoundEnabled(): Boolean {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.SOUND_ENABLED] ?: true
        }.first()
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.SOUND_ENABLED] = enabled
        }
    }

    override suspend fun getThemeMode(): String {
        return dataStore.data.map { prefs ->
            prefs[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
        }.first()
    }

    override suspend fun setThemeMode(mode: String) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode
        }
    }
}
