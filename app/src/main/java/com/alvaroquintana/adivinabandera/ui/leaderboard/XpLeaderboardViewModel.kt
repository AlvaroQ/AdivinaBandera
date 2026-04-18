package com.alvaroquintana.adivinabandera.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.domain.XpLeaderboardEntry
import com.alvaroquintana.usecases.GetXpLeaderboardUseCase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class XpLeaderboardUiState(
    val isLoading: Boolean = true,
    val entries: List<XpLeaderboardEntry> = emptyList()
)

class XpLeaderboardViewModel(
    private val getXpLeaderboardUseCase: GetXpLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(XpLeaderboardUiState())
    val uiState: StateFlow<XpLeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            FirebaseCrashlytics.getInstance().log("xp_leaderboard_viewmodel_load_started")
            try {
                val entries = getXpLeaderboardUseCase.invoke(100)
                FirebaseCrashlytics.getInstance().log("xp_leaderboard_viewmodel_load_success")
                _uiState.value = XpLeaderboardUiState(isLoading = false, entries = entries)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log("xp_leaderboard_viewmodel_load_failed")
                FirebaseCrashlytics.getInstance().recordException(e)
                _uiState.value = XpLeaderboardUiState(isLoading = false, entries = emptyList())
            }
        }
    }
}
