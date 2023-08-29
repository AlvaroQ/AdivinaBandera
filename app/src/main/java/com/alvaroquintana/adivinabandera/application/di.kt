package com.alvaroquintana.adivinabandera.application

import com.alvaroquintana.adivinabandera.datasource.FirestoreDataSourceImpl
import com.alvaroquintana.data.datasource.FirestoreDataSource
import android.app.Application
import com.alvaroquintana.adivinabandera.ui.game.GameFragment
import com.alvaroquintana.adivinabandera.ui.game.GameViewModel
import com.alvaroquintana.adivinabandera.ui.result.ResultFragment
import com.alvaroquintana.adivinabandera.ui.result.ResultViewModel
import com.alvaroquintana.adivinabandera.ui.select.SelectFragment
import com.alvaroquintana.adivinabandera.ui.select.SelectViewModel
import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.adivinabandera.datasource.DataBaseSourceImpl
import com.alvaroquintana.adivinabandera.ui.info.InfoViewModel
import com.alvaroquintana.adivinabandera.ui.ranking.RankingViewModel
import com.alvaroquintana.data.repository.AppsRecommendedRepository
import com.alvaroquintana.data.repository.CountryRepository
import com.alvaroquintana.data.repository.RankingRepository
import com.alvaroquintana.usecases.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.koin.core.module.dsl.factoryOf

fun Application.initDI() {
    startKoin {
        androidLogger()
        androidContext(this@initDI)
        modules(appModule, dataModule, scopesModule)
    }
}

private val appModule = module {
    factory { Firebase.firestore }
    single<CoroutineDispatcher> { Dispatchers.Main }
    factory<DataBaseSource> { DataBaseSourceImpl() }
    factory<FirestoreDataSource> { FirestoreDataSourceImpl(get()) }
}

val dataModule = module {
    factoryOf(::CountryRepository)
    factoryOf(::AppsRecommendedRepository)
    factoryOf(::RankingRepository)
}

private val scopesModule = module {
    viewModel { SelectViewModel() }
    viewModel { GameViewModel(get()) }
    viewModel { ResultViewModel(get(), get(), get()) }
    viewModel { RankingViewModel(get()) }
    viewModel { InfoViewModel(get()) }

    factory { GetCountryById(get()) }
    factory { GetRecordScore(get()) }
    factory { GetAppsRecommended(get()) }
    factory { SaveTopScore(get()) }
    factory { GetRankingScore(get()) }
    factory { GetCountryList(get()) }
}
