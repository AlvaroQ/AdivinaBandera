package com.alvaroquintana.adivinabandera.ui.result

import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.managers.GameStatsManager
import com.alvaroquintana.adivinabandera.ui.mvi.MviViewModel
import com.alvaroquintana.data.datasource.PreferencesDataSource
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.GameResult
import com.alvaroquintana.domain.User
import com.alvaroquintana.usecases.GetRecordScore
import com.alvaroquintana.usecases.ProcessGameResultUseCase
import com.alvaroquintana.usecases.SaveTopScore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlin.random.Random
import kotlinx.coroutines.withTimeoutOrNull

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ResultViewModel::class)
@Inject
class ResultViewModel(
    private val saveTopScore: SaveTopScore,
    private val getRecordScore: GetRecordScore,
    private val preferencesDataSource: PreferencesDataSource,
    private val processGameResultUseCase: ProcessGameResultUseCase,
    private val gameStatsManager: GameStatsManager
) : MviViewModel<ResultUiState, ResultViewModel.Intent, ResultViewModel.Event>(ResultUiState()) {

    private var currentGameMode: String = "Classic"

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_RESULT)
    }

    /**
     * Typed boot method, kept outside [Intent] by design (pragmatic MVI).
     * Wrapping a single-argument startup call in a sealed `Intent.Init`
     * variant adds ceremony without changing semantics.
     */
    fun initWithGameMode(gameMode: String) {
        currentGameMode = gameMode
        dispatch(Intent.LoadWorldRecord)
    }

    sealed class Intent {
        object LoadWorldRecord : Intent()
        data class CheckIfShouldSave(val gamePoints: Int) : Intent()
        data class SaveTopScore(val user: User) : Intent()
        data class LoadPersonalRecord(val points: Int) : Intent()
        data class ProcessGameOutcome(
            val correctAnswers: Int,
            val totalQuestions: Int,
            val bestStreak: Int,
            val timePlayedMs: Long,
            val completedAllQuestions: Boolean,
            val gameMode: String = "Classic"
        ) : Intent()
        object DismissLevelUp : Intent()
        object DismissMysteryBox : Intent()
        object DismissStreakDialog : Intent()
        object DismissModeUnlock : Intent()
        object NavigateToGame : Intent()
        object NavigateToRate : Intent()
        object NavigateToRanking : Intent()
        data class NavigateToShare(val points: Int) : Intent()
    }

    sealed class Event {
        data class Share(val points: Int) : Event()
        object Rate : Event()
        object Game : Event()
        object Ranking : Event()
        /** Asks the screen to show the "save your score" dialog. */
        data class SaveScoreDialog(val points: String) : Event()
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.LoadWorldRecord -> loadWorldRecord()
            is Intent.CheckIfShouldSave -> checkIfShouldSave(intent.gamePoints)
            is Intent.SaveTopScore -> saveTopScore.invoke(intent.user, currentGameMode)
            is Intent.LoadPersonalRecord -> loadPersonalRecord(intent.points)
            is Intent.ProcessGameOutcome -> processGameOutcome(intent)
            Intent.DismissLevelUp -> updateState { it.copy(showLevelUpDialog = false) }
            Intent.DismissMysteryBox -> updateState { it.copy(mysteryBoxReward = null) }
            Intent.DismissStreakDialog -> updateState { it.copy(showStreakDialog = false) }
            Intent.DismissModeUnlock -> updateState { it.copy(unlockEvent = null) }
            Intent.NavigateToGame -> {
                Analytics.analyticsClicked(Analytics.BTN_PLAY_AGAIN)
                emit(Event.Game)
            }
            Intent.NavigateToRate -> {
                Analytics.analyticsClicked(Analytics.BTN_RATE)
                emit(Event.Rate)
            }
            Intent.NavigateToRanking -> {
                Analytics.analyticsClicked(Analytics.BTN_RANKING)
                emit(Event.Ranking)
            }
            is Intent.NavigateToShare -> {
                Analytics.analyticsClicked(Analytics.BTN_SHARE)
                emit(Event.Share(intent.points))
            }
        }
    }

    private suspend fun loadWorldRecord() {
        val record = withTimeoutOrNull(5_000L) { getRecordScore.invoke(1, currentGameMode) } ?: "—"
        updateState { state ->
            val (level, diff) = computeEngagement(
                gamePoints = null,
                personalRecord = state.personalRecord,
                worldRecord = record
            )
            state.copy(worldRecord = record, engagementLevel = level, pointsDifference = diff)
        }
    }

    private suspend fun checkIfShouldSave(gamePoints: Int) {
        val pointsLastClassified = getRecordScore.invoke(8, currentGameMode)
        if (gamePoints > (pointsLastClassified.toIntOrNull() ?: 0)) {
            Analytics.analyticsScreenViewed(Analytics.SCREEN_DIALOG_SAVE_SCORE)
            emit(Event.SaveScoreDialog(gamePoints.toString()))
        }
    }

    private suspend fun loadPersonalRecord(points: Int) {
        val personalRecordPoints = preferencesDataSource.getPersonalRecord(currentGameMode)
        val newPersonalRecord = if (points > personalRecordPoints) {
            preferencesDataSource.savePersonalRecord(points, currentGameMode)
            points
        } else {
            personalRecordPoints
        }

        updateState { state ->
            val (level, diff) = computeEngagement(points, newPersonalRecord, state.worldRecord)
            state.copy(personalRecord = newPersonalRecord, engagementLevel = level, pointsDifference = diff)
        }
    }

    private suspend fun processGameOutcome(intent: Intent.ProcessGameOutcome) {
        val gameResult = GameResult(
            gameMode = intent.gameMode,
            correctAnswers = intent.correctAnswers,
            totalQuestions = intent.totalQuestions,
            bestStreak = intent.bestStreak,
            timePlayedMs = intent.timePlayedMs,
            completedAllQuestions = intent.completedAllQuestions
        )
        val processed = processGameResultUseCase.invoke(gameResult)

        val shouldShowStreakDialog = processed.streakCheckResult != null &&
                processed.streakCheckResult !is com.alvaroquintana.domain.StreakCheckResult.AlreadyPlayedToday

        val unlockEvent = if (processed.leveledUp) {
            detectModeUnlock(processed.previousLevel, processed.newLevel)
        } else null

        val totalGames = gameStatsManager.getTotalGamesPlayed()
        val mysteryBox = if (totalGames > 0 && totalGames % 10 == 0) {
            buildMysteryBoxReward(totalGames)
        } else null

        updateState { state ->
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

    /**
     * Detecta si al subir de [previousLevel] a [newLevel] se cruza un umbral de desbloqueo.
     * Solo devuelve el desbloqueo de nivel más alto cruzado (el más relevante para mostrar).
     */
    private fun detectModeUnlock(previousLevel: Int, newLevel: Int): UnlockEvent? {
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
}
