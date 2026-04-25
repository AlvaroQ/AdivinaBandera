package com.alvaroquintana.adivinabandera.ui.ranking

import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.ui.mvi.MviViewModel
import com.alvaroquintana.domain.User
import com.alvaroquintana.usecases.GetRankingScore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

data class RankingUiState(
    val isLoading: Boolean = false,
    val entries: List<User> = emptyList()
)

@ContributesIntoMap(AppScope::class)
@ViewModelKey(RankingViewModel::class)
@Inject
class RankingViewModel(
    private val getRankingScore: GetRankingScore
) : MviViewModel<RankingUiState, RankingViewModel.Intent, RankingViewModel.Event>(RankingUiState()) {

    sealed class Intent {
        object Load : Intent()
    }

    /** No one-shot side effects on this screen — kept for the type parameter. */
    sealed class Event

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_RANKING)
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> {
                updateState { it.copy(isLoading = true) }
                val ranking = getRankingScore.invoke()
                updateState { it.copy(isLoading = false, entries = ranking) }
            }
        }
    }
}
