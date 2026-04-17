package com.alvaroquintana.adivinabandera.datasource

import com.alvaroquintana.adivinabandera.datasource.db.CountryDao
import com.alvaroquintana.adivinabandera.datasource.db.CountryStatsDao
import com.alvaroquintana.adivinabandera.datasource.db.SyncMetadata
import com.alvaroquintana.adivinabandera.datasource.db.SyncMetadataDao
import com.alvaroquintana.adivinabandera.datasource.db.toDomain
import com.alvaroquintana.adivinabandera.datasource.db.toEntity
import com.alvaroquintana.adivinabandera.utils.Constants.PATH_REFERENCE_COUNTRIES
import com.alvaroquintana.adivinabandera.utils.Constants.STALE_THRESHOLD_MS
import com.alvaroquintana.adivinabandera.utils.Constants.TOTAL_ITEM_EACH_LOAD
import com.alvaroquintana.adivinabandera.utils.log
import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.CountryStats
import com.alvaroquintana.domain.Currency
import com.alvaroquintana.domain.Language
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private const val SYNC_COLLECTION_COUNTRIES = "countries"

/**
 * Mutable Firebase DTO used only for deserialization from Firebase Realtime Database.
 * Firebase SDK requires var fields and a no-arg constructor to deserialize via reflection.
 * Never expose this outside [DataBaseSourceImpl] — map to the immutable [Country] immediately.
 */
private data class CountryFirebaseDto(
    var name: String = "",
    var icon: String = "",
    var alpha2Code: String = "",
    var capital: String = "",
    var region: String = "",
    var flag: String = "",
    var callingCodes: MutableList<String> = mutableListOf(),
    var population: Int = 0,
    var area: Int = 0,
    var currencies: MutableList<Currency> = mutableListOf(),
    var languages: MutableList<Language> = mutableListOf(),
    var demonym: String = "",
    var borders: MutableList<String> = mutableListOf(),
    var alpha3Code: String = "",
    var subregion: String = "",
    var nativeName: String = "",
    var gini: Double? = null,
    var timezones: MutableList<String> = mutableListOf(),
    var latlng: MutableList<Double> = mutableListOf(),
    var topLevelDomain: MutableList<String> = mutableListOf(),
    var translations: MutableMap<String, String> = mutableMapOf(),
    var altSpellings: MutableList<String> = mutableListOf(),
    var numericCode: String = ""
) {
    fun toDomain(): Country = Country(
        name = name,
        icon = icon,
        alpha2Code = alpha2Code,
        capital = capital,
        region = region,
        flag = flag,
        callingCodes = callingCodes,
        population = population,
        area = area,
        currencies = currencies,
        languages = languages,
        demonym = demonym,
        borders = borders,
        alpha3Code = alpha3Code,
        subregion = subregion,
        nativeName = nativeName,
        gini = gini,
        timezones = timezones,
        latlng = latlng,
        topLevelDomain = topLevelDomain,
        translations = translations,
        altSpellings = altSpellings,
        numericCode = numericCode
    )
}

class DataBaseSourceImpl(
    private val countryDao: CountryDao,
    private val syncMetadataDao: SyncMetadataDao,
    private val firebaseDatabase: FirebaseDatabase,
    private val countryStatsDao: CountryStatsDao
) : DataBaseSource {

    private suspend fun isCacheFresh(): Boolean {
        val metadata = syncMetadataDao.getByCollection(SYNC_COLLECTION_COUNTRIES) ?: return false
        val elapsed = System.currentTimeMillis() - metadata.lastSyncTimestamp
        return elapsed < STALE_THRESHOLD_MS
    }

    private suspend fun ensureSyncedIfNeeded() {
        val cachedCount = countryDao.count()
        val hasMetadata = syncMetadataDao.getByCollection(SYNC_COLLECTION_COUNTRIES) != null

        if (!hasMetadata || cachedCount == 0 || !isCacheFresh()) {
            syncAllCountriesFromRtdb()
        }
    }

    private suspend fun syncAllCountriesFromRtdb(): Int {
        return try {
            val snapshot = suspendCancellableCoroutine<DataSnapshot?> { continuation ->
                val reference = firebaseDatabase.getReference(PATH_REFERENCE_COUNTRIES)
                val listener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        continuation.resume(dataSnapshot)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        FirebaseCrashlytics.getInstance().recordException(error.toException())
                        continuation.resume(null)
                    }
                }
                reference.addListenerForSingleValueEvent(listener)
                continuation.invokeOnCancellation { reference.removeEventListener(listener) }
            }

            if (snapshot == null || !snapshot.hasChildren()) {
                log("DataBaseSourceImpl", "syncAllCountriesFromRtdb: empty snapshot")
                return 0
            }

            val entities = mutableListOf<com.alvaroquintana.adivinabandera.datasource.db.CountryEntity>()
            for (child in snapshot.children) {
                val id = child.key?.toIntOrNull() ?: continue
                val country = child.getValue(CountryFirebaseDto::class.java)?.toDomain() ?: continue
                entities.add(country.toEntity(id))
            }

            if (entities.isNotEmpty()) {
                countryDao.deleteAll()
                countryDao.insertAll(entities)
                syncMetadataDao.upsert(
                    SyncMetadata(
                        collection = SYNC_COLLECTION_COUNTRIES,
                        lastSyncTimestamp = System.currentTimeMillis()
                    )
                )
            } else {
                log("DataBaseSourceImpl", "syncAllCountriesFromRtdb: fetched=${snapshot.childrenCount} mapped=0")
            }

            log("DataBaseSourceImpl", "sync done fetched=${snapshot.childrenCount} mapped=${entities.size}")
            entities.size
        } catch (e: Exception) {
            log("DataBaseSourceImpl", "syncAllCountriesFromRtdb failed", e)
            FirebaseCrashlytics.getInstance().recordException(e)
            0
        }
    }

    override suspend fun getCountryById(id: Int): Country {
        ensureSyncedIfNeeded()

        val cached = countryDao.getById(id)
        if (cached != null) return cached.toDomain()

        val snapshot = suspendCancellableCoroutine<DataSnapshot?> { continuation ->
            val reference = firebaseDatabase.getReference(PATH_REFERENCE_COUNTRIES + id)
            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    continuation.resume(dataSnapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    log("DataBaseSourceImpl", "getCountryById FAILED", error.toException())
                    FirebaseCrashlytics.getInstance().recordException(error.toException())
                    continuation.resume(null)
                }
            }
            reference.addListenerForSingleValueEvent(listener)
            continuation.invokeOnCancellation { reference.removeEventListener(listener) }
        }

        val country = snapshot?.getValue(CountryFirebaseDto::class.java)?.toDomain() ?: return Country()
        try {
            countryDao.insertAll(listOf(country.toEntity(id)))
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
        }
        return country
    }

    override suspend fun getCountryList(currentPage: Int): MutableList<Country> {
        ensureSyncedIfNeeded()

        val offset = currentPage * TOTAL_ITEM_EACH_LOAD
        var cached = countryDao.getPaginated(TOTAL_ITEM_EACH_LOAD, offset)
        if (cached.isEmpty()) {
            val synced = syncAllCountriesFromRtdb()
            if (synced > 0) {
                cached = countryDao.getPaginated(TOTAL_ITEM_EACH_LOAD, offset)
            }
        }

        val result = cached.map { it.toDomain() }.toMutableList()
        if (result.isEmpty()) {
            log("DataBaseSourceImpl", "getCountryList page=$currentPage returned empty after sync")
        }
        return result
    }

    override suspend fun getRandomCountries(count: Int): List<Country> {
        ensureSyncedIfNeeded()

        var result = countryDao.getRandom(count).map { it.toDomain() }
        if (result.size < count) {
            val synced = syncAllCountriesFromRtdb()
            if (synced > 0) result = countryDao.getRandom(count).map { it.toDomain() }
        }

        val trimmed = result.take(count)
        if (trimmed.isEmpty()) {
            log("DataBaseSourceImpl", "getRandomCountries returned empty")
        }
        return trimmed
    }

    override suspend fun upsertCountryStat(stat: CountryStats) {
        countryStatsDao.upsertStat(stat.toEntity())
    }

    override suspend fun getCountryStatByCode(code: String): CountryStats? {
        return countryStatsDao.getStatByCode(code)?.toDomain()
    }

    override suspend fun getAllCountryStats(): List<CountryStats> {
        return countryStatsDao.getAllStats().map { it.toDomain() }
    }

    override suspend fun getTopWrongCountries(limit: Int): List<CountryStats> {
        return countryStatsDao.getTopWrongCountries(limit).map { it.toDomain() }
    }

    override suspend fun getDiscoveredCountriesCount(): Int {
        return countryStatsDao.getDiscoveredCount()
    }

    override suspend fun getCountryIdByAlpha2Code(alpha2Code: String): Int? {
        return countryDao.getIdByAlpha2Code(alpha2Code)
    }

}
