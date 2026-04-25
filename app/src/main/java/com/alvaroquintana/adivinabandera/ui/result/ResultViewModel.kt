package com.alvaroquintana.adivinabandera.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.managers.GameStatsManager
import com.alvaroquintana.data.datasource.PreferencesDataSource
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.GameResult
import com.alvaroquintana.domain.User
import com.alvaroquintana.usecases.GetRecordScore
import com.alvaroquintana.usecases.ProcessGameResultUseCase
import com.alvaroquintana.usecases.SaveTopScore
import kotlin.random.Random
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@dev.zacsweers.metro.ContributesIntoMap(dev.zacsweers.metro.AppScope::class)
@dev.zacsweers.metrox.viewmodel.ViewModelKey(ResultViewModel::class)
@dev.zacsweers.metro.Inject
class ResultViewModel(
    private val saveTopScore: SaveTopScore,
    private val getRecordScore: GetRecordScore,
    private val preferencesDataSource: PreferencesDataSource,
    private val processGameResultUseCase: ProcessGameResultUseCase,
    private val gameStatsManager: GameStatsManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private val _navigation = MutableSharedFlow<Navigation>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val navigation: SharedFlow<Navigation> = _navigation.asSharedFlow()

    private var currentGameMode: String = "Classic"

    fun initWithGameMode(gameMode: String) {
        currentGameMode = gameMode
        viewModelScope.launch {
            val record = withTimeoutOrNull(5_000L) { getRecordScore.invoke(1, gameMode) } ?: "—"
            _uiState.update { state ->
                val (level, diff) = computeEngagement(
                    gamePoints = null,
                    personalRecord = state.personalRecord,
                    worldRecord = record
                )
                state.copy(worldRecord = record, engagementLevel = level, pointsDifference = diff)
            }
        }
    }


    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_RESULT)
    }

    /**
     * Calcula el nivel de engagement comparando los puntos actuales con récords.
     * [gamePoints] puede ser null si aún no se conoce (primer estado).
     */
    internal fun computeEngagement(
        gamePoints: Int?,
        personalRecord: Int,
        worldRecord: String?
    ): Pair<EngagementLevel, Int> {
        if (gamePoints == null || gamePoints == 0) return Pair(EngagementLevel.KEEP_TRYING, 0)

        val personal = personalRecord
        val world = worldRecord?.toIntOrNull()

        if (world != null && gamePoints > world) {
            return Pair(EngagementLevel.NEW_WORLD_RECORD, gamePoints - world)
        }

        if (gamePoints > personal) {
            return Pair(EngagementLevel.NEW_PERSONAL_BEST, gamePoints - personal)
        }

        val gap = personal - gamePoints
        if (gap in 1..10) {
            return Pair(EngagementLevel.SO_CLOSE, gap)
        }

        return Pair(EngagementLevel.KEEP_TRYING, 0)
    }

    fun setPersonalRecordOnServer(gamePoints: Int) {
        viewModelScope.launch {
            val pointsLastClassified = getRecordScore.invoke(8, currentGameMode)
            if (gamePoints > (pointsLastClassified.toIntOrNull() ?: 0)) {
                showDialogToSaveGame(gamePoints.toString())
            }
        }
    }

    fun saveTopScore(user: User) {
        viewModelScope.launch {
            saveTopScore.invoke(user, currentGameMode)
        }
    }

    fun getPersonalRecord(points: Int) {
        viewModelScope.launch {
            val personalRecordPoints = preferencesDataSource.getPersonalRecord(currentGameMode)
            val newPersonalRecord = if (points > personalRecordPoints) {
                preferencesDataSource.savePersonalRecord(points, currentGameMode)
                points
            } else {
                personalRecordPoints
            }

            _uiState.update { state ->
                val (level, diff) = computeEngagement(points, newPersonalRecord, state.worldRecord)
                state.copy(personalRecord = newPersonalRecord, engagementLevel = level, pointsDifference = diff)
            }
        }
    }

    private fun showDialogToSaveGame(points: String) {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_DIALOG_SAVE_SCORE)
        _navigation.tryEmit(Navigation.Dialog(points))
    }

    fun navigateToGame() {
        Analytics.analyticsClicked(Analytics.BTN_PLAY_AGAIN)
        _navigation.tryEmit(Navigation.Game)
    }

    fun navigateToRate() {
        Analytics.analyticsClicked(Analytics.BTN_RATE)
        _navigation.tryEmit(Navigation.Rate)
    }

    fun navigateToRanking() {
        Analytics.analyticsClicked(Analytics.BTN_RANKING)
        _navigation.tryEmit(Navigation.Ranking)
    }

    fun navigateToShare(points: Int) {
        Analytics.analyticsClicked(Analytics.BTN_SHARE)
        _navigation.tryEmit(Navigation.Share(points))
    }

    fun dismissLevelUpDialog() {
        _uiState.update { it.copy(showLevelUpDialog = false) }
    }

    /**
     * Procesa el resultado de la partida a traves del sistema de engagement.
     * Actualiza el estado con XP ganado, nivel nuevo, logros desbloqueados.
     */
    fun processEngagement(
        correctAnswers: Int,
        totalQuestions: Int,
        bestStreak: Int,
        timePlayedMs: Long,
        completedAllQuestions: Boolean,
        gameMode: String = "Classic"
    ) {
        viewModelScope.launch {
            val gameResult = GameResult(
                gameMode = gameMode,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                bestStreak = bestStreak,
                timePlayedMs = timePlayedMs,
                completedAllQuestions = completedAllQuestions
            )
            val processed = processGameResultUseCase.invoke(gameResult)

            // Determinar si mostrar el dialog de racha (excluir AlreadyPlayedToday)
            val shouldShowStreakDialog = processed.streakCheckResult != null &&
                    processed.streakCheckResult !is com.alvaroquintana.domain.StreakCheckResult.AlreadyPlayedToday

            val unlockEvent = if (processed.leveledUp) {
                detectModeUnlock(processed.previousLevel, processed.newLevel)
            } else null

            // Caja misteriosa: activa cada 10 partidas jugadas (10, 20, 30, ...)
            val totalGames = gameStatsManager.getTotalGamesPlayed()
            val mysteryBox = if (totalGames > 0 && totalGames % 10 == 0) {
                buildMysteryBoxReward(totalGames)
            } else null

            _uiState.update { state ->
                state.copy(
                    xpGained = processed.xpGained,
                    xpBreakdown = processed.xpBreakdown,
                    leveledUp = processed.leveledUp,
                    newLevel = processed.newLevel,
                    newTitle = processed.newTitle,
                    previousLevel = processed.previousLevel,
                    newAchievements = processed.newAchievements,
                    showLevelUpDialog = processed.leveledUp && unlockEvent == null,
                    streakCheckResult = processed.streakCheckResult,
                    showStreakDialog = shouldShowStreakDialog,
                    challengeCompletionResult = processed.challengeCompletionResult,
                    coinsEarned = processed.coinsEarned,
                    gemsEarned = processed.gemsEarned,
                    unlockEvent = unlockEvent,
                    mysteryBoxReward = mysteryBox
                )
            }
        }
    }

    /**
     * Genera una recompensa aleatoria para la caja misteriosa.
     * Usa el numero de partidas como seed para resultados reproducibles por sesion.
     */
    private fun buildMysteryBoxReward(totalGames: Int): MysteryBoxReward {
        val random = Random(totalGames.toLong())
        val roll = random.nextFloat()
        return when {
            roll < 0.15f -> MysteryBoxReward(
                type = MysteryBoxReward.Type.FREEZE_TOKEN,
                xpAmount = 0,
                coinsAmount = 0
            )
            roll < 0.50f -> MysteryBoxReward(
                type = MysteryBoxReward.Type.COINS_BONUS,
                xpAmount = 0,
                coinsAmount = random.nextInt(30, 101)
            )
            else -> MysteryBoxReward(
                type = MysteryBoxReward.Type.XP_BONUS,
                xpAmount = random.nextInt(50, 201),
                coinsAmount = 0
            )
        }
    }

    fun dismissMysteryBox() {
        _uiState.update { it.copy(mysteryBoxReward = null) }
    }

    fun dismissStreakDialog() {
        _uiState.update { it.copy(showStreakDialog = false) }
    }

    fun dismissModeUnlockCelebration() {
        _uiState.update { it.copy(unlockEvent = null) }
    }

    /**
     * Detecta si al subir de [previousLevel] a [newLevel] se cruza un umbral de desbloqueo.
     * Solo devuelve el desbloqueo de nivel más alto cruzado (el más relevante para mostrar).
     */
    private fun detectModeUnlock(previousLevel: Int, newLevel: Int): UnlockEvent? {
        // Los modos regionales (España/México/...) no se desbloquean por nivel XP,
        // sino por aciertos en el chain (RegionalProgressionManager).
        val lockedModes = listOf(
            GameMode.CurrencyDetective to "Currency Detective",
            GameMode.PopulationChallenge to "Population Challenge",
            GameMode.WorldMix to "World Mix"
        )
        return lockedModes
            .filter { (mode, _) -> previousLevel < mode.unlockLevel && newLevel >= mode.unlockLevel }
            .maxByOrNull { (mode, _) -> mode.unlockLevel }
            ?.let { (mode, modeName) -> UnlockEvent.ModeUnlocked(modeName, mode.unlockLevel) }
    }

    sealed class Navigation {
        data class Share(val points: Int) : Navigation()
        object Rate : Navigation()
        object Game : Navigation()
        object Ranking : Navigation()
        data class Dialog(val points: String) : Navigation()
    }
}
