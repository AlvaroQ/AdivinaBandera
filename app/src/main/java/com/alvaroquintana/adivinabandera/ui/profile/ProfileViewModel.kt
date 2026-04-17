package com.alvaroquintana.adivinabandera.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.adivinabandera.managers.AchievementManager
import com.alvaroquintana.adivinabandera.managers.DailyChallengeManager
import com.alvaroquintana.adivinabandera.managers.GameStatsManager
import com.alvaroquintana.adivinabandera.managers.ProgressionManager
import com.alvaroquintana.adivinabandera.managers.StreakManager
import com.alvaroquintana.adivinabandera.managers.XpSyncManager
import com.alvaroquintana.domain.Achievement
import com.alvaroquintana.domain.challenge.ChallengeStats
import com.alvaroquintana.usecases.GetUserGlobalRankUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val nickname: String = "",
    val imageBase64: String = "",
    val level: Int = 1,
    val title: String = "Novato",
    val totalXp: Int = 0,
    val xpForNextLevel: Int = 100,
    val xpProgressInLevel: Int = 0,
    val xpNeededForLevel: Int = 100,
    val globalRank: Int = -1,
    val totalGamesPlayed: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val accuracy: Float = 0f,
    val bestStreakEver: Int = 0,
    val totalPerfectGames: Int = 0,
    val totalTimePlayed: Long = 0L,
    val unlockedAchievements: List<Achievement> = emptyList(),
    val allAchievements: List<Achievement> = Achievement.entries,
    // Racha diaria
    val currentDailyStreak: Int = 0,
    val bestDailyStreak: Int = 0,
    val totalDaysPlayed: Int = 0,
    val freezeTokens: Int = 0,
    // Desafios diarios
    val challengeStats: ChallengeStats = ChallengeStats()
)

class ProfileViewModel(
    private val progressionManager: ProgressionManager,
    private val gameStatsManager: GameStatsManager,
    private val achievementManager: AchievementManager,
    private val getUserGlobalRankUseCase: GetUserGlobalRankUseCase,
    private val streakManager: StreakManager,
    private val dailyChallengeManager: DailyChallengeManager,
    private val xpSyncManager: XpSyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val totalXp = progressionManager.getTotalXp()
            val level = progressionManager.getCurrentLevel()
            val title = progressionManager.getCurrentTitle()
            val nickname = progressionManager.getNickname()
            val image = progressionManager.getImageBase64()

            val totalGames = gameStatsManager.getTotalGamesPlayed()
            val totalCorrect = gameStatsManager.getTotalCorrectAnswers()
            val accuracy = gameStatsManager.getAccuracy()
            val bestStreak = gameStatsManager.getBestStreakEver()
            val perfectGames = gameStatsManager.getTotalPerfectGames()
            val timePlayed = gameStatsManager.getTotalTimePlayed()

            val unlocked = achievementManager.getUnlockedAchievements()

            val streakState = streakManager.getStreakState()
            val challengeStats = dailyChallengeManager.getChallengeStats()

            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val rank = if (uid != null && nickname.isNotBlank()) {
                try { getUserGlobalRankUseCase.invoke(uid) } catch (_: Exception) { -1 }
            } else -1

            _uiState.update {
                it.copy(
                    isLoading = false,
                    nickname = nickname,
                    imageBase64 = image,
                    level = level,
                    title = title,
                    totalXp = totalXp,
                    xpProgressInLevel = ProgressionManager.xpProgressInCurrentLevel(totalXp, level),
                    xpNeededForLevel = ProgressionManager.xpNeededForCurrentLevel(level),
                    xpForNextLevel = ProgressionManager.xpForNextLevel(level),
                    globalRank = rank,
                    totalGamesPlayed = totalGames,
                    totalCorrectAnswers = totalCorrect,
                    accuracy = accuracy,
                    bestStreakEver = bestStreak,
                    totalPerfectGames = perfectGames,
                    totalTimePlayed = timePlayed,
                    unlockedAchievements = unlocked,
                    currentDailyStreak = streakState.currentStreak,
                    bestDailyStreak = streakState.bestStreak,
                    totalDaysPlayed = streakState.totalDaysPlayed,
                    freezeTokens = streakState.freezeTokens,
                    challengeStats = challengeStats
                )
            }
        }
    }

    fun onNicknameChanged(nickname: String) {
        viewModelScope.launch {
            progressionManager.setNickname(nickname)
            _uiState.update { it.copy(nickname = nickname) }
            xpSyncManager.syncPendingIfNeeded()
        }
    }

    fun saveProfileImage(imageBase64: String) {
        viewModelScope.launch {
            progressionManager.setImageBase64(imageBase64)
            _uiState.update { it.copy(imageBase64 = imageBase64) }
        }
    }
}
