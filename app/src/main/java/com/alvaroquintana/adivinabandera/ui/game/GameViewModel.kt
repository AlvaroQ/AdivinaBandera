package com.alvaroquintana.adivinabandera.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.MixQuestionType
import com.alvaroquintana.domain.regionalAlpha2
import com.alvaroquintana.domain.toRouteString
import com.alvaroquintana.usecases.RecordAnswerUseCase
import com.alvaroquintana.usecases.question.GeneratedQuestion
import com.alvaroquintana.usecases.question.GenerationContext
import com.alvaroquintana.usecases.question.QuestionGeneratorFactory
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(
    private val questionGeneratorFactory: QuestionGeneratorFactory,
    private val recordAnswer: RecordAnswerUseCase,
    val gameMode: GameMode = GameMode.Classic,
    val forcedCountryPool: List<Int> = emptyList()
) : ViewModel() {

    private val excludedCountryIds = mutableSetOf<Int>()
    private val seenSubdivisionIds = mutableSetOf<String>()
    private var currentQuestion: GeneratedQuestion? = null

    private val _question = MutableStateFlow("")
    val question: StateFlow<String> = _question.asStateFlow()

    private val _countryName = MutableStateFlow("")
    val countryName: StateFlow<String> = _countryName.asStateFlow()

    private val _currencyQuestion = MutableStateFlow("")
    val currencyQuestion: StateFlow<String> = _currencyQuestion.asStateFlow()

    private val _mixQuestionText = MutableStateFlow("")
    val mixQuestionText: StateFlow<String> = _mixQuestionText.asStateFlow()

    private val _currentMixType = MutableStateFlow<MixQuestionType?>(null)
    val currentMixType: StateFlow<MixQuestionType?> = _currentMixType.asStateFlow()

    private val _populationPair = MutableStateFlow<Pair<Country, Country>?>(null)
    val populationPair: StateFlow<Pair<Country, Country>?> = _populationPair.asStateFlow()

    private val _responseOptions = MutableSharedFlow<MutableList<String>>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val responseOptions: SharedFlow<MutableList<String>> = _responseOptions.asSharedFlow()

    private val _progress = MutableStateFlow<UiModel>(UiModel.Loading(false))
    val progress: StateFlow<UiModel> = _progress.asStateFlow()

    private val _navigation = MutableSharedFlow<Navigation>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val navigation: SharedFlow<Navigation> = _navigation.asSharedFlow()

    private val _showingAds = MutableSharedFlow<UiModel>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val showingAds: SharedFlow<UiModel> = _showingAds.asSharedFlow()

    // --- Tracking de engagement ---
    private val _streakMessage = MutableSharedFlow<String?>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val streakMessage: SharedFlow<String?> = _streakMessage.asSharedFlow()

    private var currentStreak: Int = 0
    private var bestStreak: Int = 0
    private var correctAnswers: Int = 0
    private var gameStartTimeMs: Long = 0L

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_GAME)
        gameStartTimeMs = System.currentTimeMillis()
        generateNewStage()
        _showingAds.tryEmit(UiModel.ShowBannerAd(true))
    }

    fun generateNewStage() {
        viewModelScope.launch {
            _progress.value = UiModel.Loading(true)
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
                _progress.value = UiModel.Loading(false)
                return@launch
            }
            apply(q)
        }
    }

    private fun apply(q: GeneratedQuestion) {
        currentQuestion = q
        q.selectedCountryId?.let { excludedCountryIds.add(it) }
        q.seenSubdivisionId?.let { seenSubdivisionIds.add(it) }

        _question.value = q.flagIcon
        _countryName.value = q.countryName
        _mixQuestionText.value = q.mixQuestionText
        _currencyQuestion.value = q.currencyQuestion
        _currentMixType.value = q.currentMixType
        _populationPair.value = q.populationPair
        _responseOptions.tryEmit(q.options.toMutableList())
        _progress.value = UiModel.Loading(false)
    }

    fun showRewardedAd() {
        _showingAds.tryEmit(UiModel.ShowRewardedAd(true))
    }

    fun onCorrectAnswer() {
        correctAnswers++
        currentStreak++
        if (currentStreak > bestStreak) bestStreak = currentStreak

        streakMessageFor(currentStreak)?.let { _streakMessage.tryEmit(it) }

        viewModelScope.launch {
            recordAnswer.invoke(
                isCorrect = true,
                alpha2Code = currentQuestion?.selectedCountryAlpha2,
                gameMode = gameMode.toRouteString(),
                regionalAlpha2 = gameMode.regionalAlpha2
            )
        }
    }

    fun onWrongAnswer() {
        currentStreak = 0

        viewModelScope.launch {
            recordAnswer.invoke(
                isCorrect = false,
                alpha2Code = currentQuestion?.selectedCountryAlpha2,
                gameMode = gameMode.toRouteString(),
                regionalAlpha2 = gameMode.regionalAlpha2
            )
        }
    }

    private fun streakMessageFor(streak: Int): String? = when (streak) {
        3 -> "¡En racha!"
        5 -> "¡Imparable!"
        10 -> "¡Legendario!"
        15 -> "¡Combo: $streak!"
        else -> if (streak > 15 && streak % 5 == 0) "¡Combo: $streak!" else null
    }

    fun navigateToResult(points: String, totalQuestions: Int, completedAllQuestions: Boolean) {
        Analytics.analyticsGameFinished(points)
        val timePlayedMs = System.currentTimeMillis() - gameStartTimeMs
        _navigation.tryEmit(
            Navigation.Result(
                points = points,
                correctAnswers = correctAnswers,
                totalQuestions = totalQuestions,
                bestStreak = bestStreak,
                timePlayedMs = timePlayedMs,
                completedAllQuestions = completedAllQuestions
            )
        )
    }

    fun getCorrectAnswer(): String = currentQuestion?.correctAnswer ?: ""

    fun getCode2CountryCorrect(): String? = currentQuestion?.selectedCountryAlpha2

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
        data class ShowBannerAd(val show: Boolean) : UiModel()
        data class ShowRewardedAd(val show: Boolean) : UiModel()
    }

    sealed class Navigation {
        data class Result(
            val points: String,
            val correctAnswers: Int,
            val totalQuestions: Int,
            val bestStreak: Int,
            val timePlayedMs: Long,
            val completedAllQuestions: Boolean
        ) : Navigation()
    }
}
