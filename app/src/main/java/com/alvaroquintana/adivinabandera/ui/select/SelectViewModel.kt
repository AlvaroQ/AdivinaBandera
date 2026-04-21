package com.alvaroquintana.adivinabandera.ui.select

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.managers.CountryMasteryManager
import com.alvaroquintana.adivinabandera.managers.CurrencyManager
import com.alvaroquintana.adivinabandera.managers.DailyChallengeManager
import com.alvaroquintana.adivinabandera.managers.DailyRewardManager
import com.alvaroquintana.adivinabandera.managers.ProgressionManager
import com.alvaroquintana.adivinabandera.managers.RegionalProgressionManager
import com.alvaroquintana.adivinabandera.managers.StreakManager
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.GameModeDescriptor
import com.alvaroquintana.domain.REGIONAL_UNLOCK_THRESHOLD
import com.alvaroquintana.domain.RegionalModeDescriptor
import com.alvaroquintana.domain.StreakState
import com.alvaroquintana.domain.challenge.DailyChallengeState
import com.alvaroquintana.domain.cosmetics.CurrencyBalance
import com.alvaroquintana.domain.regionalAlpha2
import com.alvaroquintana.domain.regionalChain
import com.alvaroquintana.domain.regionalPrerequisite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SelectUiState(
    val streakState: StreakState = StreakState(),
    val isStreakAtRisk: Boolean = false,
    val hasPlayedToday: Boolean = false,
    val challengeState: DailyChallengeState = DailyChallengeState(),
    val currencyBalance: CurrencyBalance = CurrencyBalance(),
    val currentLevel: Int = 1,
    val currentXp: Int = 0,
    val gameModeDescriptors: List<GameModeDescriptor> = emptyList(),
    val regionalModeDescriptors: List<RegionalModeDescriptor> = emptyList(),
    val dailyReward: DailyRewardManager.DailyReward? = null,
    val discoveredCountries: Int = 0,
    val weakSpotCountryIds: List<Int> = emptyList()
) {
    /** Regiones ya desbloqueadas (incluye la primera que siempre lo está). */
    val unlockedRegionalCount: Int get() = regionalModeDescriptors.count { it.isUnlocked }
    val totalRegionalCount: Int get() = regionalModeDescriptors.size
}

class SelectViewModel(
    private val streakManager: StreakManager,
    private val dailyChallengeManager: DailyChallengeManager,
    private val progressionManager: ProgressionManager,
    private val currencyManager: CurrencyManager,
    private val dailyRewardManager: DailyRewardManager,
    private val countryMasteryManager: CountryMasteryManager,
    private val regionalProgressionManager: RegionalProgressionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SelectUiState())
    val uiState: StateFlow<SelectUiState> = _uiState.asStateFlow()

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_SELECT_GAME)
        loadInitialState()
        observeBalance()
    }

    private fun loadInitialState() {
        viewModelScope.launch {
            val state = streakManager.getStreakState()
            val atRisk = streakManager.isStreakAtRisk()
            val playedToday = streakManager.hasPlayedToday()
            val playerLevel = progressionManager.getCurrentLevel()
            val totalXp = progressionManager.getTotalXp()
            val challengeState = dailyChallengeManager.getDailyChallengeState(playerLevel)
            val descriptors = buildModeDescriptors(playerLevel, totalXp)
            val regionalSnapshot = regionalProgressionManager.snapshot()
            val regionalDescriptors = buildRegionalDescriptors(regionalSnapshot)
            val dailyReward = dailyRewardManager.getTodayReward()
            val discoveredCount = countryMasteryManager.getDiscoveredCount()
            val weakSpotIds = countryMasteryManager.getWeakSpotsAsIds()
            _uiState.update {
                it.copy(
                    streakState = state,
                    isStreakAtRisk = atRisk,
                    hasPlayedToday = playedToday,
                    challengeState = challengeState,
                    currentLevel = playerLevel,
                    currentXp = totalXp,
                    gameModeDescriptors = descriptors,
                    regionalModeDescriptors = regionalDescriptors,
                    dailyReward = dailyReward,
                    discoveredCountries = discoveredCount,
                    weakSpotCountryIds = weakSpotIds
                )
            }
        }
    }

    /** Refresca los descriptors regionales. Se llama tras volver de una partida regional. */
    fun refreshRegionalProgression() {
        viewModelScope.launch {
            val snapshot = regionalProgressionManager.snapshot()
            _uiState.update { it.copy(regionalModeDescriptors = buildRegionalDescriptors(snapshot)) }
        }
    }

    /**
     * Reclama el bono diario y aplica el XP + monedas al jugador.
     * Si ya fue reclamado hoy, no hace nada.
     */
    fun claimDailyReward() {
        viewModelScope.launch {
            val reward = dailyRewardManager.getTodayReward()
            if (!reward.isClaimed) {
                val claimed = dailyRewardManager.claimReward()
                progressionManager.addXp(claimed.xpAmount)
                currencyManager.earnCoins(claimed.coinsAmount, source = "daily_reward")
                _uiState.update { it.copy(dailyReward = claimed) }
            }
        }
    }

    private fun buildModeDescriptors(currentLevel: Int, totalXp: Int): List<GameModeDescriptor> {
        val modes = listOf(
            GameMode.Classic,
            GameMode.CapitalByFlag,
            GameMode.CurrencyDetective,
            GameMode.PopulationChallenge,
            GameMode.WorldMix
        )
        return modes.map { mode ->
            val unlockLevel = mode.unlockLevel
            val isUnlocked = unlockLevel == 0 || currentLevel >= unlockLevel
            val progress = if (unlockLevel == 0) 1f
                          else (currentLevel.toFloat() / unlockLevel).coerceAtMost(1f)
            val xpToUnlock = if (isUnlocked) 0
                             else ProgressionManager.LEVEL_THRESHOLDS.getOrElse(unlockLevel - 1) { 0 } - totalXp
            GameModeDescriptor(
                mode = mode,
                unlockLevel = unlockLevel,
                isUnlocked = isUnlocked,
                unlockProgress = progress,
                isNearUnlock = !isUnlocked && progress >= 0.75f,
                xpToUnlock = xpToUnlock.coerceAtLeast(0)
            )
        }
    }

    private fun buildRegionalDescriptors(snapshot: Map<String, Int>): List<RegionalModeDescriptor> {
        return regionalChain.mapNotNull { mode ->
            val alpha2 = mode.regionalAlpha2 ?: return@mapNotNull null
            val prereqAlpha2 = mode.regionalPrerequisite?.regionalAlpha2
            val prereqCorrect = prereqAlpha2?.let { snapshot[it] ?: 0 } ?: 0
            val isUnlocked = prereqAlpha2 == null || prereqCorrect >= REGIONAL_UNLOCK_THRESHOLD
            RegionalModeDescriptor(
                mode = mode,
                alpha2 = alpha2,
                isUnlocked = isUnlocked,
                correctAnswersInMode = snapshot[alpha2] ?: 0,
                prerequisiteCorrectAnswers = prereqCorrect,
                requiredToUnlockNext = REGIONAL_UNLOCK_THRESHOLD
            )
        }
    }

    /** Observa el balance de moneda en tiempo real para mantener el header actualizado. */
    private fun observeBalance() {
        viewModelScope.launch {
            currencyManager.observeBalance().collect { balance ->
                _uiState.update { it.copy(currencyBalance = balance) }
            }
        }
    }

}
