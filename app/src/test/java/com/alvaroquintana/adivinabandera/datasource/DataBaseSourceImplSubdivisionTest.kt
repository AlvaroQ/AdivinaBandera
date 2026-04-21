package com.alvaroquintana.adivinabandera.datasource

import com.alvaroquintana.adivinabandera.datasource.db.CountryDao
import com.alvaroquintana.adivinabandera.datasource.db.CountryStatsDao
import com.alvaroquintana.adivinabandera.datasource.db.SubdivisionDao
import com.alvaroquintana.adivinabandera.datasource.db.SubdivisionEntity
import com.alvaroquintana.adivinabandera.datasource.db.SyncMetadata
import com.alvaroquintana.adivinabandera.datasource.db.SyncMetadataDao
import com.alvaroquintana.domain.CountrySubdivision
import com.google.firebase.database.FirebaseDatabase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Tests for [DataBaseSourceImpl] subdivision-related operations.
 *
 * Strategy: we bypass the Firebase sync path by making the cache appear fresh
 * (subdivisionDao.count() > 0 AND syncMetadataDao returns a recent timestamp).
 * This lets us test the Entity→Domain mapping and DAO delegation in isolation
 * without mocking Firebase or FirebaseCrashlytics.
 */
class DataBaseSourceImplSubdivisionTest {

    private lateinit var countryDao: CountryDao
    private lateinit var syncMetadataDao: SyncMetadataDao
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var countryStatsDao: CountryStatsDao
    private lateinit var subdivisionDao: SubdivisionDao

    private lateinit var dataBaseSource: DataBaseSourceImpl

    /** A SyncMetadata entry that is considered fresh (timestamp = now). */
    private val freshSubdivisionMetadata = SyncMetadata(
        collection = "subdivisions",
        lastSyncTimestamp = System.currentTimeMillis()
    )

    /** A SyncMetadata entry for countries — needed for any country-path calls. */
    private val freshCountryMetadata = SyncMetadata(
        collection = "countries",
        lastSyncTimestamp = System.currentTimeMillis()
    )

    @Before
    fun setUp() {
        countryDao = mockk(relaxed = true)
        syncMetadataDao = mockk(relaxed = true)
        firebaseDatabase = mockk(relaxed = true) // relaxed: Firebase calls never triggered in these tests
        countryStatsDao = mockk(relaxed = true)
        subdivisionDao = mockk(relaxed = true)

        // Make the subdivisions cache appear fresh so sync is skipped
        coEvery { subdivisionDao.count() } returns 4
        coEvery { syncMetadataDao.getByCollection("subdivisions") } returns freshSubdivisionMetadata

        dataBaseSource = DataBaseSourceImpl(
            countryDao = countryDao,
            syncMetadataDao = syncMetadataDao,
            firebaseDatabase = firebaseDatabase,
            countryStatsDao = countryStatsDao,
            subdivisionDao = subdivisionDao
        )
    }

    // region getSubdivisionsForCountry

    @Test
    fun `getSubdivisionsForCountry maps SubdivisionEntities to domain objects correctly`() = runTest {
        val entities = listOf(
            SubdivisionEntity(id = "ES-A", countryAlpha2 = "ES", name = "Almería",   type = "Province", flagUrl = "url-a", difficulty = "medium"),
            SubdivisionEntity(id = "ES-B", countryAlpha2 = "ES", name = "Barcelona", type = "Province", flagUrl = "url-b", difficulty = "easy")
        )
        coEvery { subdivisionDao.getByCountry("ES") } returns entities

        val result = dataBaseSource.getSubdivisionsForCountry("ES")

        assertEquals(2, result.size)
        assertEquals(
            CountrySubdivision(id = "ES-A", countryAlpha2 = "ES", name = "Almería",   type = "Province", flagUrl = "url-a", difficulty = "medium"),
            result[0]
        )
        assertEquals(
            CountrySubdivision(id = "ES-B", countryAlpha2 = "ES", name = "Barcelona", type = "Province", flagUrl = "url-b", difficulty = "easy"),
            result[1]
        )
        coVerify(exactly = 1) { subdivisionDao.getByCountry("ES") }
    }

    @Test
    fun `getSubdivisionsForCountry returns empty list when DAO has no rows for country`() = runTest {
        coEvery { subdivisionDao.getByCountry("XX") } returns emptyList()

        val result = dataBaseSource.getSubdivisionsForCountry("XX")

        assertTrue(result.isEmpty())
        coVerify(exactly = 1) { subdivisionDao.getByCountry("XX") }
    }

    @Test
    fun `getSubdivisionsForCountry preserves all entity fields through mapping`() = runTest {
        val entity = SubdivisionEntity(
            id = "FR-1",
            countryAlpha2 = "FR",
            name = "Ain",
            type = "Department",
            flagUrl = "https://example.com/fr-1.svg",
            difficulty = "hard"
        )
        coEvery { subdivisionDao.getByCountry("FR") } returns listOf(entity)

        val result = dataBaseSource.getSubdivisionsForCountry("FR")

        assertEquals(1, result.size)
        with(result.first()) {
            assertEquals("FR-1", id)
            assertEquals("FR", countryAlpha2)
            assertEquals("Ain", name)
            assertEquals("Department", type)
            assertEquals("https://example.com/fr-1.svg", flagUrl)
            assertEquals("hard", difficulty)
        }
    }

    // endregion

    // region getSubdivisionCountryCodesWithMinCount

    @Test
    fun `getSubdivisionCountryCodesWithMinCount returns only countries with enough subdivisions`() = runTest {
        coEvery { subdivisionDao.getDistinctCountryCodes() } returns listOf("ES", "FR", "DE")
        coEvery { subdivisionDao.countByCountry("ES") } returns 10
        coEvery { subdivisionDao.countByCountry("FR") } returns 4
        coEvery { subdivisionDao.countByCountry("DE") } returns 2 // below threshold

        val result = dataBaseSource.getSubdivisionCountryCodesWithMinCount(4)

        assertEquals(listOf("ES", "FR"), result)
    }

    @Test
    fun `getSubdivisionCountryCodesWithMinCount returns empty when no country meets minimum`() = runTest {
        coEvery { subdivisionDao.getDistinctCountryCodes() } returns listOf("IT", "PT")
        coEvery { subdivisionDao.countByCountry("IT") } returns 1
        coEvery { subdivisionDao.countByCountry("PT") } returns 3

        val result = dataBaseSource.getSubdivisionCountryCodesWithMinCount(4)

        assertTrue(result.isEmpty())
    }

    // endregion

    // region getSubdivisionsCount

    @Test
    fun `getSubdivisionsCount delegates to DAO count`() = runTest {
        coEvery { subdivisionDao.count() } returns 42

        val result = dataBaseSource.getSubdivisionsCount()

        assertEquals(42, result)
    }

    // endregion
}
