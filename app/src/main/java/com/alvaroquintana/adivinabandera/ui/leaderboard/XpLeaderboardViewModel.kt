package com.alvaroquintana.adivinabandera.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.domain.XpLeaderboardEntry
import com.alvaroquintana.usecases.GetXpLeaderboardUseCase
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
            val entries = getXpLeaderboardUseCase.invoke(100)
            _uiState.value = XpLeaderboardUiState(isLoading = false, entries = entries)
        }
    }
}
