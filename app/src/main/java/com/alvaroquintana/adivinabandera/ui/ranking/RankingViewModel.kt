package com.alvaroquintana.adivinabandera.ui.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.domain.User
import com.alvaroquintana.usecases.GetRankingScore
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@dev.zacsweers.metro.ContributesIntoMap(dev.zacsweers.metro.AppScope::class)
@dev.zacsweers.metrox.viewmodel.ViewModelKey(RankingViewModel::class)
@dev.zacsweers.metro.Inject
class RankingViewModel(private val getRankingScore: GetRankingScore) : ViewModel() {

    private val _progress = MutableStateFlow<UiModel>(UiModel.Loading(false))
    val progress: StateFlow<UiModel> = _progress.asStateFlow()

    private val _navigation = MutableSharedFlow<Navigation>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val navigation: SharedFlow<Navigation> = _navigation.asSharedFlow()

    private val _rankingList = MutableStateFlow<MutableList<User>>(mutableListOf())
    val rankingList: StateFlow<MutableList<User>> = _rankingList.asStateFlow()

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_RANKING)
        viewModelScope.launch {
            _progress.value = UiModel.Loading(true)
            _rankingList.value = getRanking()
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun getRanking(): MutableList<User> {
        return getRankingScore.invoke()
    }

    sealed class Navigation {
        object Result : Navigation()
    }

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
    }
}
