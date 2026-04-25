package com.alvaroquintana.adivinabandera.ui.profile

import com.alvaroquintana.adivinabandera.managers.AchievementManager
import com.alvaroquintana.adivinabandera.managers.DailyChallengeManager
import com.alvaroquintana.adivinabandera.managers.GameStatsManager
import com.alvaroquintana.adivinabandera.managers.ProgressionManager
import com.alvaroquintana.adivinabandera.managers.StreakManager
import com.alvaroquintana.adivinabandera.managers.XpSyncManager
import com.alvaroquintana.adivinabandera.ui.mvi.MviViewModel
import com.alvaroquintana.domain.Achievement
import com.alvaroquintana.domain.challenge.ChallengeStats
import com.alvaroquintana.usecases.GetUserGlobalRankUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

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

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ProfileViewModel::class)
@Inject
class ProfileViewModel(
    private val progressionManager: ProgressionManager,
    private val gameStatsManager: GameStatsManager,
    private val achievementManager: AchievementManager,
    private val getUserGlobalRankUseCase: GetUserGlobalRankUseCase,
    private val streakManager: StreakManager,
    private val dailyChallengeManager: DailyChallengeManager,
    private val xpSyncManager: XpSyncManager
) : MviViewModel<ProfileUiState, ProfileViewModel.Intent, ProfileViewModel.Event>(ProfileUiState()) {

    sealed class Intent {
        object Load : Intent()
        data class ChangeNickname(val nickname: String) : Intent()
        data class SaveProfileImage(val imageBase64: String) : Intent()
    }

    sealed class Event

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> loadProfile()
            is Intent.ChangeNickname -> changeNickname(intent.nickname)
            is Intent.SaveProfileImage -> saveImage(intent.imageBase64)
        }
    }

    private suspend fun loadProfile() {
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
            try {
                getUserGlobalRankUseCase.invoke(uid)
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().apply {
                    log("profile_global_rank_load_failed")
                    setCustomKey("profile_has_nickname", nickname.isNotBlank())
                    recordException(e)
                }
                -1
            }
        } else -1

        updateState {
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

    private suspend fun changeNickname(nickname: String) {
        progressionManager.setNickname(nickname)
        updateState { it.copy(nickname = nickname) }
        xpSyncManager.syncPendingIfNeeded()
    }

    private suspend fun saveImage(imageBase64: String) {
        progressionManager.setImageBase64(imageBase64)
        updateState { it.copy(imageBase64 = imageBase64) }
    }
}
