package com.alvaroquintana.adivinabandera.ui.leaderboard

import com.alvaroquintana.adivinabandera.ui.mvi.MviViewModel
import com.alvaroquintana.domain.XpLeaderboardEntry
import com.alvaroquintana.usecases.GetXpLeaderboardUseCase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

data class XpLeaderboardUiState(
    val isLoading: Boolean = true,
    val entries: List<XpLeaderboardEntry> = emptyList()
)

@ContributesIntoMap(AppScope::class)
@ViewModelKey(XpLeaderboardViewModel::class)
@Inject
class XpLeaderboardViewModel(
    private val getXpLeaderboardUseCase: GetXpLeaderboardUseCase
) : MviViewModel<XpLeaderboardUiState, XpLeaderboardViewModel.Intent, XpLeaderboardViewModel.Event>(XpLeaderboardUiState()) {

    sealed class Intent {
        object Load : Intent()
    }

    sealed class Event

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> loadLeaderboard()
        }
    }

    private suspend fun loadLeaderboard() {
        FirebaseCrashlytics.getInstance().log("xp_leaderboard_viewmodel_load_started")
        try {
            val entries = getXpLeaderboardUseCase.invoke(100)
            FirebaseCrashlytics.getInstance().log("xp_leaderboard_viewmodel_load_success")
            updateState { XpLeaderboardUiState(isLoading = false, entries = entries) }
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log("xp_leaderboard_viewmodel_load_failed")
            FirebaseCrashlytics.getInstance().recordException(e)
            updateState { XpLeaderboardUiState(isLoading = false, entries = emptyList()) }
        }
    }
}
