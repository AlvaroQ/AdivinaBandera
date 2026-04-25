package com.alvaroquintana.adivinabandera.application.di

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.alvaroquintana.adivinabandera.cosmetics.BanderaCatalog
import com.alvaroquintana.adivinabandera.datasource.db.AppDatabase
import com.alvaroquintana.adivinabandera.datasource.db.CountryDao
import com.alvaroquintana.adivinabandera.datasource.db.CountryStatsDao
import com.alvaroquintana.adivinabandera.datasource.db.SubdivisionDao
import com.alvaroquintana.adivinabandera.datasource.db.SyncMetadataDao
import com.alvaroquintana.adivinabandera.managers.UnlockableCatalog
import com.alvaroquintana.adivinabandera.managers.XpSyncManager
import com.alvaroquintana.adivinabandera.utils.Constants
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

private val Context.dataStore by preferencesDataStore(
    name = "adivinabandera_preferences"
)

@DependencyGraph(scope = AppScope::class)
interface AppGraph : ViewModelGraph {

    /** Used by [LocalMetroViewModelFactory] to inject ViewModels in Compose. */
    val viewModelFactory: MetroViewModelFactory

    /**
     * Exposed for [com.alvaroquintana.adivinabandera.ui.MainActivity] which
     * triggers a leaderboard sync on resume. The Activity lifecycle is owned
     * by the Android framework so we cannot constructor-inject it.
     */
    val xpSyncManager: XpSyncManager

    @Provides
    fun provideContext(application: Application): Context = application

    @Provides
    @SingleIn(AppScope::class)
    fun provideAppDatabase(application: Application): AppDatabase =
        Room.databaseBuilder(application, AppDatabase::class.java, "adivinabandera-db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun provideCountryDao(db: AppDatabase): CountryDao = db.countryDao()

    @Provides
    fun provideSyncMetadataDao(db: AppDatabase): SyncMetadataDao = db.syncMetadataDao()

    @Provides
    fun provideCountryStatsDao(db: AppDatabase): CountryStatsDao = db.countryStatsDao()

    @Provides
    fun provideSubdivisionDao(db: AppDatabase): SubdivisionDao = db.subdivisionDao()

    @Provides
    fun provideFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @SingleIn(AppScope::class)
    fun provideDataStore(application: Application): DataStore<Preferences> =
        application.dataStore

    @Provides
    fun provideUnlockableCatalog(): UnlockableCatalog = BanderaCatalog

    // QuestionGeneratorFactory has @Inject on its primary constructor and
    // pulls 3 use cases from the graph. The only non-injectable parameter
    // is the total-countries integer which is provided here.
    @Provides
    fun provideTotalCountries(): Int = Constants.TOTAL_COUNTRIES

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}
