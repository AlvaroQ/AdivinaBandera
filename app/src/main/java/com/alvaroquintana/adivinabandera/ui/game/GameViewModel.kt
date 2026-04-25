package com.alvaroquintana.adivinabandera.ui.game

import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.ui.mvi.MviViewModel
import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.MixQuestionType
import com.alvaroquintana.domain.regionalAlpha2
import com.alvaroquintana.domain.toRouteString
import com.alvaroquintana.usecases.RecordAnswerUseCase
import com.alvaroquintana.usecases.question.GeneratedQuestion
import com.alvaroquintana.usecases.question.GenerationContext
import com.alvaroquintana.usecases.question.QuestionGeneratorFactory
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey

/**
 * Single immutable snapshot of the game screen.
 *
 * `responseOptions` lives on the state so reads are synchronous. The
 * companion [GameViewModel.Event.QuestionRefreshed] is fired in tandem
 * to let the UI reset answer-button visuals on every new question — the
 * state is "what's on screen", the event is "a new question just landed".
 */
data class GameUiState(
    val flagIcon: String = "",
    val countryName: String = "",
    val currencyQuestion: String = "",
    val mixQuestionText: String = "",
    val currentMixType: MixQuestionType? = null,
    val populationPair: Pair<Country, Country>? = null,
    val responseOptions: List<String> = emptyList(),
    val isLoading: Boolean = false
)

@AssistedInject
class GameViewModel(
    @Assisted val gameMode: GameMode,
    @Assisted val forcedCountryPool: List<Int>,
    private val questionGeneratorFactory: QuestionGeneratorFactory,
    private val recordAnswer: RecordAnswerUseCase
) : MviViewModel<GameUiState, GameViewModel.Intent, GameViewModel.Event>(GameUiState()) {

    /**
     * Manual assisted factory used by [assistedMetroViewModel] callers in
     * Compose. Lets the navigation layer pass [gameMode] and a forced
     * country pool — both runtime parameters that cannot be resolved
     * from the dependency graph alone.
     */
    @AssistedFactory
    @ManualViewModelAssistedFactoryKey(Factory::class)
    @ContributesIntoMap(AppScope::class)
    fun interface Factory : ManualViewModelAssistedFactory {
        fun create(gameMode: GameMode, forcedCountryPool: List<Int>): GameViewModel
    }

    sealed class Intent {
        object GenerateNewStage : Intent()
        object OnCorrectAnswer : Intent()
        object OnWrongAnswer : Intent()
        object ShowRewardedAd : Intent()
        data class NavigateToResult(
            val points: String,
            val totalQuestions: Int,
            val completedAllQuestions: Boolean
        ) : Intent()
    }

    sealed class Event {
        /** Fired every time a new question is loaded so the UI can reset answer states. */
        data class QuestionRefreshed(val options: List<String>) : Event()
        data class StreakMessage(val message: String) : Event()
        object ShowRewardedAd : Event()
        data class NavigateToResult(
            val points: String,
            val correctAnswers: Int,
            val totalQuestions: Int,
            val bestStreak: Int,
            val timePlayedMs: Long,
            val completedAllQuestions: Boolean
        ) : Event()
    }

    private val excludedCountryIds = mutableSetOf<Int>()
    private val seenSubdivisionIds = mutableSetOf<String>()
    private var currentQuestion: GeneratedQuestion? = null

    private var currentStreak: Int = 0
    private var bestStreak: Int = 0
    private var correctAnswers: Int = 0
    private var gameStartTimeMs: Long = 0L

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_GAME)
        gameStartTimeMs = System.currentTimeMillis()
        // Note: the first question is intentionally NOT dispatched here. The
        // screen does it via LaunchedEffect so QuestionRefreshed reaches a
        // subscribed collector — emitting before any subscriber would lose
        // the event (replay=0 SharedFlow).
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.GenerateNewStage -> generateNewStage()
            Intent.OnCorrectAnswer -> onCorrectAnswer()
            Intent.OnWrongAnswer -> onWrongAnswer()
            Intent.ShowRewardedAd -> emit(Event.ShowRewardedAd)
            is Intent.NavigateToResult -> emitResult(intent)
        }
    }

    private suspend fun generateNewStage() {
        updateState { it.copy(isLoading = true) }
        val generator = questionGeneratorFactory.forMode(gameMode)
        val q = generator.generate(
            GenerationContext(
                excludedCountryIds = excludedCountryIds.toSet(),
                forcedCountryPool = forcedCountryPool,
                seenSubdivisionIds = seenSubdivisionIds.toSet(),
                subdivisionAlpha2 = gameMode.regionalAlpha2
            )
        )
        if (q == null) {
            updateState { it.copy(isLoading = false) }
            return
        }
        applyQuestion(q)
    }

    private fun applyQuestion(q: GeneratedQuestion) {
        currentQuestion = q
        q.selectedCountryId?.let { excludedCountryIds.add(it) }
        q.seenSubdivisionId?.let { seenSubdivisionIds.add(it) }

        val options = q.options.toList()
        updateState {
            it.copy(
                flagIcon = q.flagIcon,
                countryName = q.countryName,
                mixQuestionText = q.mixQuestionText,
                currencyQuestion = q.currencyQuestion,
                currentMixType = q.currentMixType,
                populationPair = q.populationPair,
                responseOptions = options,
                isLoading = false
            )
        }
        emit(Event.QuestionRefreshed(options))
    }

    private suspend fun onCorrectAnswer() {
        correctAnswers++
        currentStreak++
        if (currentStreak > bestStreak) bestStreak = currentStreak

        streakMessageFor(currentStreak)?.let { emit(Event.StreakMessage(it)) }

        recordAnswer.invoke(
            isCorrect = true,
            alpha2Code = currentQuestion?.selectedCountryAlpha2,
            gameMode = gameMode.toRouteString(),
            regionalAlpha2 = gameMode.regionalAlpha2
        )
    }

    private suspend fun onWrongAnswer() {
        currentStreak = 0
        recordAnswer.invoke(
            isCorrect = false,
            alpha2Code = currentQuestion?.selectedCountryAlpha2,
            gameMode = gameMode.toRouteString(),
            regionalAlpha2 = gameMode.regionalAlpha2
        )
    }

    private fun emitResult(intent: Intent.NavigateToResult) {
        Analytics.analyticsGameFinished(intent.points)
        val timePlayedMs = System.currentTimeMillis() - gameStartTimeMs
        emit(
            Event.NavigateToResult(
                points = intent.points,
                correctAnswers = correctAnswers,
                totalQuestions = intent.totalQuestions,
                bestStreak = bestStreak,
                timePlayedMs = timePlayedMs,
                completedAllQuestions = intent.completedAllQuestions
            )
        )
    }

    private fun streakMessageFor(streak: Int): String? = when (streak) {
        3 -> "¡En racha!"
        5 -> "¡Imparable!"
        10 -> "¡Legendario!"
        15 -> "¡Combo: $streak!"
        else -> if (streak > 15 && streak % 5 == 0) "¡Combo: $streak!" else null
    }

    /** Synchronous getter used by the screen to validate the selected answer. */
    fun getCorrectAnswer(): String = currentQuestion?.correctAnswer ?: ""

    /** Synchronous getter used for analytics / mastery on the answered country. */
    fun getCode2CountryCorrect(): String? = currentQuestion?.selectedCountryAlpha2
}
