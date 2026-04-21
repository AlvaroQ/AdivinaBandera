package com.alvaroquintana.adivinabandera.application

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.alvaroquintana.adivinabandera.BuildConfig
import com.alvaroquintana.adivinabandera.cosmetics.BanderaCatalog
import com.alvaroquintana.adivinabandera.datasource.DataBaseSourceImpl
import com.alvaroquintana.adivinabandera.datasource.FirestoreDataSourceImpl
import com.alvaroquintana.adivinabandera.datasource.GameResultProcessorImpl
import com.alvaroquintana.adivinabandera.datasource.PreferencesDataSourceImpl
import com.alvaroquintana.adivinabandera.datasource.XpLeaderboardDataSourceImpl
import com.alvaroquintana.adivinabandera.managers.AchievementManager
import com.alvaroquintana.adivinabandera.managers.CountryMasteryManager
import com.alvaroquintana.adivinabandera.managers.CurrencyManager
import com.alvaroquintana.adivinabandera.managers.DailyChallengeManager
import com.alvaroquintana.adivinabandera.managers.DailyRewardManager
import com.alvaroquintana.adivinabandera.managers.GameStatsManager
import com.alvaroquintana.adivinabandera.managers.ProgressionManager
import com.alvaroquintana.adivinabandera.managers.RegionalProgressionManager
import com.alvaroquintana.adivinabandera.managers.StreakManager
import com.alvaroquintana.adivinabandera.managers.UnlockableCatalog
import com.alvaroquintana.adivinabandera.managers.UnlockablesManager
import com.alvaroquintana.adivinabandera.managers.XpSyncManager
import com.alvaroquintana.data.datasource.GameResultProcessorDataSource
import com.alvaroquintana.data.datasource.XpLeaderboardDataSource
import com.alvaroquintana.adivinabandera.datasource.db.AppDatabase
import com.alvaroquintana.adivinabandera.ui.game.GameViewModel
import com.alvaroquintana.adivinabandera.ui.info.InfoViewModel
import com.alvaroquintana.adivinabandera.ui.leaderboard.XpLeaderboardViewModel
import com.alvaroquintana.adivinabandera.ui.profile.ProfileViewModel
import com.alvaroquintana.adivinabandera.ui.ranking.RankingViewModel
import com.alvaroquintana.adivinabandera.ui.result.ResultViewModel
import com.alvaroquintana.adivinabandera.ui.select.SelectViewModel
import com.alvaroquintana.adivinabandera.ui.shop.ShopViewModel
import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.data.datasource.FirestoreDataSource
import com.alvaroquintana.data.datasource.PreferencesDataSource
import com.alvaroquintana.data.repository.CountryRepository
import com.alvaroquintana.data.repository.CountryRepositoryImpl
import com.alvaroquintana.data.repository.RankingRepository
import com.alvaroquintana.data.repository.RankingRepositoryImpl
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.GetCountryList
import com.alvaroquintana.usecases.GetRandomCountries
import com.alvaroquintana.usecases.GetRandomSubdivisions
import com.alvaroquintana.usecases.GetSubdivisionCountryCodesWithMinCount
import com.alvaroquintana.usecases.GetSubdivisionsForCountry
import com.alvaroquintana.usecases.GetRankingScore
import com.alvaroquintana.usecases.GetRecordScore
import com.alvaroquintana.usecases.GetUserGlobalRankUseCase
import com.alvaroquintana.usecases.GetXpLeaderboardUseCase
import com.alvaroquintana.usecases.ProcessGameResultUseCase
import com.alvaroquintana.usecases.SaveTopScore
import com.alvaroquintana.usecases.SyncUserXpUseCase
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.firestore
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val Context.dataStore by preferencesDataStore(
    name = "adivinabandera_preferences"
)

fun Application.initDI() {
    startKoin {
        if (BuildConfig.DEBUG) {
            androidLogger()
        }
        androidContext(this@initDI)
        modules(appModule, dataModule, scopesModule)
    }
}

private val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "adivinabandera-db"
        ).fallbackToDestructiveMigration(true).build()
    }
    single { get<AppDatabase>().countryDao() }
    single { get<AppDatabase>().syncMetadataDao() }
    single { get<AppDatabase>().countryStatsDao() }
    single { get<AppDatabase>().subdivisionDao() }
    factory { Firebase.firestore }
    factory { FirebaseDatabase.getInstance() }
    single { androidContext().dataStore }
    factory<DataBaseSource> { DataBaseSourceImpl(get(), get(), get(), get(), get()) }
    factory<FirestoreDataSource> { FirestoreDataSourceImpl() }
    factory<PreferencesDataSource> { PreferencesDataSourceImpl(get()) }

    // Managers (single — una instancia por ciclo de vida de la app)
    single { ProgressionManager(get()) }
    single { GameStatsManager(get()) }
    single { StreakManager(get()) }
    single { AchievementManager(get(), get(), get(), get()) }
    single { XpSyncManager(get(), get(), get(), get()) }
    single { DailyChallengeManager(androidContext()) }
    single { DailyRewardManager(get()) }
    single { CountryMasteryManager(get()) }
    single { RegionalProgressionManager(get()) }

    // Economia virtual y cosmeticos
    single { CurrencyManager(androidContext()) }
    single<UnlockableCatalog> { BanderaCatalog }
    single { UnlockablesManager(androidContext(), get(), get()) }

    // DataSources de la capa de engagement
    factory<XpLeaderboardDataSource> { XpLeaderboardDataSourceImpl() }
    factory<GameResultProcessorDataSource> { GameResultProcessorImpl(get(), get(), get(), get(), get(), get(), get()) }
}

val dataModule = module {
    factory<CountryRepository> { CountryRepositoryImpl(get()) }
    factory<RankingRepository> { RankingRepositoryImpl(get()) }
}

private val scopesModule = module {
    viewModel { SelectViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { params -> GameViewModel(get(), get(), get(), get(), get(), params.get(), get(), get(), params.getOrNull<List<Int>>() ?: emptyList()) }
    viewModel { ResultViewModel(get(), get(), get(), get(), get()) }
    viewModel { ShopViewModel(get(), get()) }
    viewModel { RankingViewModel(get()) }
    viewModel { InfoViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { XpLeaderboardViewModel(get()) }

    factory { GetCountryById(get()) }
    factory { GetCountryList(get()) }
    factory { GetRandomCountries(get()) }
    factory { GetRandomSubdivisions(get()) }
    factory { GetSubdivisionsForCountry(get()) }
    factory { GetSubdivisionCountryCodesWithMinCount(get()) }
    factory { GetRecordScore(get()) }
    factory { SaveTopScore(get()) }
    factory { GetRankingScore(get()) }

    // Use cases de engagement y XP
    factory { ProcessGameResultUseCase(get()) }
    factory { SyncUserXpUseCase(get()) }
    factory { GetXpLeaderboardUseCase(get()) }
    factory { GetUserGlobalRankUseCase(get()) }
}
