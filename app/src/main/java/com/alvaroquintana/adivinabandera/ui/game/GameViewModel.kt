package com.alvaroquintana.adivinabandera.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.managers.CountryMasteryManager
import com.alvaroquintana.adivinabandera.managers.DailyChallengeManager
import com.alvaroquintana.adivinabandera.managers.ProgressionManager
import com.alvaroquintana.adivinabandera.managers.RegionalProgressionManager
import com.alvaroquintana.adivinabandera.utils.Constants.TOTAL_COUNTRIES
import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.challenge.ChallengeEvent
import com.alvaroquintana.domain.CountrySubdivision
import com.alvaroquintana.domain.isRegional
import com.alvaroquintana.domain.regionalAlpha2
import com.alvaroquintana.domain.toRouteString
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.GetRandomCountries
import com.alvaroquintana.usecases.GetSubdivisionsForCountry
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class MixQuestionType {
    FLAG_TO_COUNTRY,
    FLAG_TO_CAPITAL,
    CURRENCY_TO_COUNTRY,
    DEMONYM_TO_COUNTRY,
    LANGUAGE_TO_COUNTRY,
    CALLING_CODE_TO_COUNTRY,
    NEIGHBOR_CHALLENGE
}

class GameViewModel(
    private val getCountryById: GetCountryById,
    private val getRandomCountries: GetRandomCountries,
    private val getSubdivisionsForCountry: GetSubdivisionsForCountry,
    private val dailyChallengeManager: DailyChallengeManager,
    private val progressionManager: ProgressionManager,
    val gameMode: GameMode = GameMode.Classic,
    private val countryMasteryManager: CountryMasteryManager,
    private val regionalProgressionManager: RegionalProgressionManager,
    val forcedCountryPool: List<Int> = emptyList()
) : ViewModel() {
    private var randomCountries = mutableListOf<Int>()
    private lateinit var country: Country
    // For NEIGHBOR_CHALLENGE: stores the correct neighbor alpha2Code since country holds the "asked about" country
    private var neighborCorrectAlpha2: String = ""
    private var subdivision: CountrySubdivision? = null
    private val seenSubdivisionIds = mutableSetOf<String>()

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

            val isCapitalMode = gameMode == GameMode.CapitalByFlag
            val isCurrencyMode = gameMode == GameMode.CurrencyDetective
            val isPopulationMode = gameMode == GameMode.PopulationChallenge
            val isWorldMixMode = gameMode == GameMode.WorldMix

            if (isPopulationMode) {
                // Population Challenge: pick two distinct countries with population > 0 and different populations
                var idA: Int
                var countryA: Country
                do {
                    idA = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray())
                    countryA = getCountry(idA)
                } while (countryA.population <= 0)

                var idB: Int
                var countryB: Country
                do {
                    idB = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray(), idA)
                    countryB = getCountry(idB)
                } while (countryB.population <= 0 || countryB.population == countryA.population)

                randomCountries.add(idA)

                _populationPair.value = Pair(countryA, countryB)
                country = countryA // keep country field for getCode2CountryCorrect compatibility

                // Options: 2 items — index 0 = countryA, index 1 = countryB
                // Correct answer is derived on demand via getCorrectAnswer() using _populationPair
                val optionList = mutableListOf(
                    countryA.alpha2Code,
                    countryB.alpha2Code
                )
                _responseOptions.tryEmit(optionList)
                _question.value = ""
                _progress.value = UiModel.Loading(false)
                return@launch
            }

            if (gameMode.isRegional) {
                val alpha2 = gameMode.regionalAlpha2
                if (alpha2 != null) {
                    generateSubdivisionStage(alpha2)
                } else {
                    _progress.value = UiModel.Loading(false)
                }
                return@launch
            }

            if (isWorldMixMode) {
                generateWorldMixStage()
                return@launch
            }

            /** Generate question */
            var numRandomMain: Int
            var candidateCountry: Country
            if (forcedCountryPool.isNotEmpty()) {
                // Practice mode: pick from the forced pool, excluding already-seen entries
                val available = forcedCountryPool.filter { it !in randomCountries }
                numRandomMain = if (available.isNotEmpty()) available.random() else forcedCountryPool.random()
                candidateCountry = getCountry(numRandomMain)
            } else {
                do {
                    numRandomMain = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray())
                    candidateCountry = getCountry(numRandomMain)
                } while (
                    (isCapitalMode && candidateCountry.capital.isBlank()) ||
                    (isCurrencyMode && candidateCountry.currencies.isEmpty())
                )
            }
            randomCountries.add(numRandomMain)
            country = candidateCountry

            /** Generate responses */
            val numRandomMainPosition = generateRandomWithExclusion(0, 3)

            // For currency mode: wrong options must not share the same currency code as the correct answer
            val correctCurrencyCode = if (isCurrencyMode) country.currencies.first().code else ""

            var numRandomOption1: Int
            var option1: Country
            do {
                numRandomOption1 = generateRandomWithExclusion(1, TOTAL_COUNTRIES, numRandomMain)
                option1 = getCountry(numRandomOption1)
            } while (isCurrencyMode && option1.currencies.any { it.code == correctCurrencyCode })
            val numRandomPosition1 = generateRandomWithExclusion(0, 3, numRandomMainPosition)

            var numRandomOption2: Int
            var option2: Country
            do {
                numRandomOption2 = generateRandomWithExclusion(1, TOTAL_COUNTRIES, numRandomMain, numRandomOption1)
                option2 = getCountry(numRandomOption2)
            } while (isCurrencyMode && option2.currencies.any { it.code == correctCurrencyCode })
            val numRandomPosition2 = generateRandomWithExclusion(0, 3, numRandomMainPosition, numRandomPosition1)

            var numRandomOption3: Int
            var option3: Country
            do {
                numRandomOption3 = generateRandomWithExclusion(1, TOTAL_COUNTRIES, numRandomMain, numRandomOption1, numRandomOption2)
                option3 = getCountry(numRandomOption3)
            } while (isCurrencyMode && option3.currencies.any { it.code == correctCurrencyCode })
            val numRandomPosition3 = generateRandomWithExclusion(0, 3, numRandomMainPosition, numRandomPosition1, numRandomPosition2)

            /** Save value */
            val optionList = mutableListOf("", "", "", "")

            when {
                isCapitalMode -> {
                    optionList[numRandomMainPosition] = country.capital
                    optionList[numRandomPosition1] = option1.capital.ifBlank { option1.name }
                    optionList[numRandomPosition2] = option2.capital.ifBlank { option2.name }
                    optionList[numRandomPosition3] = option3.capital.ifBlank { option3.name }
                    _countryName.value = country.name
                }
                isCurrencyMode -> {
                    // Options are country alpha2codes (flags), same as Classic mode
                    optionList[numRandomMainPosition] = country.alpha2Code
                    optionList[numRandomPosition1] = option1.alpha2Code
                    optionList[numRandomPosition2] = option2.alpha2Code
                    optionList[numRandomPosition3] = option3.alpha2Code
                    // Build question text from currency info
                    val currency = country.currencies.first()
                    val symbol = if (currency.symbol.isNotBlank()) " (${currency.symbol})" else ""
                    _currencyQuestion.value = "¿Qué país usa el ${currency.name}$symbol?"
                }
                else -> {
                    optionList[numRandomMainPosition] = country.alpha2Code
                    optionList[numRandomPosition1] = option1.alpha2Code
                    optionList[numRandomPosition2] = option2.alpha2Code
                    optionList[numRandomPosition3] = option3.alpha2Code
                }
            }

            _responseOptions.tryEmit(optionList)
            _question.value = country.icon
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun generateSubdivisionStage(alpha2: String) {
        val pool = getSubdivisionsForCountry.invoke(alpha2)
        if (pool.size < 4) {
            _progress.value = UiModel.Loading(false)
            return
        }

        val available = pool.filter { it.id !in seenSubdivisionIds }
        val correct = (if (available.isNotEmpty()) available else pool).random()
        seenSubdivisionIds.add(correct.id)
        val wrongPool = pool.filter { it.id != correct.id }.shuffled().take(3)
        subdivision = correct

        val correctPos = generateRandomWithExclusion(0, 3)
        val positions = (0..3).filter { it != correctPos }
        val options = MutableList(4) { "" }
        options[correctPos] = correct.name
        wrongPool.forEachIndexed { idx, sub -> options[positions[idx]] = sub.name }

        _question.value = correct.flagUrl
        _countryName.value = ""
        _currencyQuestion.value = ""
        _mixQuestionText.value = ""
        _responseOptions.tryEmit(options)
        _progress.value = UiModel.Loading(false)
    }

    private suspend fun generateWorldMixStage() {
        // Pick a random question type, with fallback to FLAG_TO_COUNTRY on failure
        val allTypes = MixQuestionType.entries.toList()
        val shuffled = allTypes.shuffled()

        for (type in shuffled) {
            val success = tryGenerateMixQuestion(type)
            if (success) return
        }

        // Absolute fallback: FLAG_TO_COUNTRY
        tryGenerateMixQuestion(MixQuestionType.FLAG_TO_COUNTRY)
    }

    /**
     * Attempts to generate a question of the given type.
     * Returns true on success, false if the type cannot be generated
     * (e.g., no countries with the required field).
     */
    private suspend fun tryGenerateMixQuestion(type: MixQuestionType): Boolean {
        return when (type) {
            MixQuestionType.FLAG_TO_COUNTRY -> {
                val id = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray())
                val c = getCountry(id)
                randomCountries.add(id)
                country = c

                val pos = generateRandomWithExclusion(0, 3)
                val (o1, o2, o3, p1, p2, p3) = pickThreeWrongOptions(id, pos) { true }

                val optionList = mutableListOf("", "", "", "")
                optionList[pos] = c.alpha2Code
                optionList[p1] = o1.alpha2Code
                optionList[p2] = o2.alpha2Code
                optionList[p3] = o3.alpha2Code

                _currentMixType.value = MixQuestionType.FLAG_TO_COUNTRY
                _mixQuestionText.value = ""
                _question.value = c.icon
                _countryName.value = ""
                _currencyQuestion.value = ""
                _responseOptions.tryEmit(optionList)
                _progress.value = UiModel.Loading(false)
                true
            }

            MixQuestionType.FLAG_TO_CAPITAL -> {
                var id: Int
                var c: Country
                var attempts = 0
                do {
                    id = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray())
                    c = getCountry(id)
                    attempts++
                } while (c.capital.isBlank() && attempts < 30)
                if (c.capital.isBlank()) return false

                randomCountries.add(id)
                country = c

                val pos = generateRandomWithExclusion(0, 3)
                val (o1, o2, o3, p1, p2, p3) = pickThreeWrongOptions(id, pos) { true }

                val optionList = mutableListOf("", "", "", "")
                optionList[pos] = c.capital
                optionList[p1] = o1.capital.ifBlank { o1.name }
                optionList[p2] = o2.capital.ifBlank { o2.name }
                optionList[p3] = o3.capital.ifBlank { o3.name }

                _currentMixType.value = MixQuestionType.FLAG_TO_CAPITAL
                _mixQuestionText.value = ""
                _question.value = c.icon
                _countryName.value = ""
                _currencyQuestion.value = ""
                _responseOptions.tryEmit(optionList)
                _progress.value = UiModel.Loading(false)
                true
            }

            MixQuestionType.CURRENCY_TO_COUNTRY -> {
                var id: Int
                var c: Country
                var attempts = 0
                do {
                    id = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray())
                    c = getCountry(id)
                    attempts++
                } while (c.currencies.isEmpty() && attempts < 30)
                if (c.currencies.isEmpty()) return false

                randomCountries.add(id)
                country = c

                val correctCurrencyCode = c.currencies.first().code
                val pos = generateRandomWithExclusion(0, 3)
                val (o1, o2, o3, p1, p2, p3) = pickThreeWrongOptions(id, pos) { candidate ->
                    candidate.currencies.none { it.code == correctCurrencyCode }
                }

                val optionList = mutableListOf("", "", "", "")
                optionList[pos] = c.alpha2Code
                optionList[p1] = o1.alpha2Code
                optionList[p2] = o2.alpha2Code
                optionList[p3] = o3.alpha2Code

                val currency = c.currencies.first()
                val symbol = if (currency.symbol.isNotBlank()) " (${currency.symbol})" else ""
                val questionText = "¿Qué país usa el ${currency.name}$symbol?"

                _currentMixType.value = MixQuestionType.CURRENCY_TO_COUNTRY
                _mixQuestionText.value = questionText
                _question.value = ""
                _countryName.value = ""
                _currencyQuestion.value = questionText
                _responseOptions.tryEmit(optionList)
                _progress.value = UiModel.Loading(false)
                true
            }

            MixQuestionType.DEMONYM_TO_COUNTRY -> {
                var id: Int
                var c: Country
                var attempts = 0
                do {
                    id = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray())
                    c = getCountry(id)
                    attempts++
                } while (c.demonym.isBlank() && attempts < 30)
                if (c.demonym.isBlank()) return false

                randomCountries.add(id)
                country = c

                val correctDemonym = c.demonym
                val pos = generateRandomWithExclusion(0, 3)
                val (o1, o2, o3, p1, p2, p3) = pickThreeWrongOptions(id, pos) { candidate ->
                    candidate.demonym != correctDemonym
                }

                val optionList = mutableListOf("", "", "", "")
                optionList[pos] = c.alpha2Code
                optionList[p1] = o1.alpha2Code
                optionList[p2] = o2.alpha2Code
                optionList[p3] = o3.alpha2Code

                val questionText = "¿De qué país son los ${c.demonym}?"

                _currentMixType.value = MixQuestionType.DEMONYM_TO_COUNTRY
                _mixQuestionText.value = questionText
                _question.value = ""
                _countryName.value = ""
                _currencyQuestion.value = ""
                _responseOptions.tryEmit(optionList)
                _progress.value = UiModel.Loading(false)
                true
            }

            MixQuestionType.LANGUAGE_TO_COUNTRY -> {
                var id: Int
                var c: Country
                var attempts = 0
                do {
                    id = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray())
                    c = getCountry(id)
                    attempts++
                } while (c.languages.isEmpty() && attempts < 30)
                if (c.languages.isEmpty()) return false

                randomCountries.add(id)
                country = c

                val chosenLanguage = c.languages.random()
                val languageName = chosenLanguage.name
                val pos = generateRandomWithExclusion(0, 3)
                val (o1, o2, o3, p1, p2, p3) = pickThreeWrongOptions(id, pos) { candidate ->
                    candidate.languages.none { it.name == languageName }
                }

                val optionList = mutableListOf("", "", "", "")
                optionList[pos] = c.alpha2Code
                optionList[p1] = o1.alpha2Code
                optionList[p2] = o2.alpha2Code
                optionList[p3] = o3.alpha2Code

                val questionText = "¿En qué país se habla ${languageName}?"

                _currentMixType.value = MixQuestionType.LANGUAGE_TO_COUNTRY
                _mixQuestionText.value = questionText
                _question.value = ""
                _countryName.value = ""
                _currencyQuestion.value = ""
                _responseOptions.tryEmit(optionList)
                _progress.value = UiModel.Loading(false)
                true
            }

            MixQuestionType.CALLING_CODE_TO_COUNTRY -> {
                var id: Int
                var c: Country
                var attempts = 0
                do {
                    id = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray())
                    c = getCountry(id)
                    attempts++
                } while ((c.callingCodes.isEmpty() || c.callingCodes.first().isBlank()) && attempts < 30)
                if (c.callingCodes.isEmpty() || c.callingCodes.first().isBlank()) return false

                randomCountries.add(id)
                country = c

                val correctCode = c.callingCodes.first()
                val pos = generateRandomWithExclusion(0, 3)
                val (o1, o2, o3, p1, p2, p3) = pickThreeWrongOptions(id, pos) { candidate ->
                    candidate.callingCodes.none { it == correctCode }
                }

                val optionList = mutableListOf("", "", "", "")
                optionList[pos] = c.alpha2Code
                optionList[p1] = o1.alpha2Code
                optionList[p2] = o2.alpha2Code
                optionList[p3] = o3.alpha2Code

                val questionText = "¿De qué país es el código +${correctCode}?"

                _currentMixType.value = MixQuestionType.CALLING_CODE_TO_COUNTRY
                _mixQuestionText.value = questionText
                _question.value = ""
                _countryName.value = ""
                _currencyQuestion.value = ""
                _responseOptions.tryEmit(optionList)
                _progress.value = UiModel.Loading(false)
                true
            }

            MixQuestionType.NEIGHBOR_CHALLENGE -> {
                var id: Int
                var c: Country
                var attempts = 0
                do {
                    id = generateRandomWithExclusion(0, TOTAL_COUNTRIES, *randomCountries.toIntArray())
                    c = getCountry(id)
                    attempts++
                } while (c.borders.isEmpty() && attempts < 30)
                if (c.borders.isEmpty()) return false

                // Build a map of alpha3Code -> Country to resolve border codes
                // getRandomCountries with TOTAL_COUNTRIES fetches all countries in one query
                val allCountries = getRandomCountries.invoke(TOTAL_COUNTRIES)
                val alpha3Map = allCountries.filter { it.alpha3Code.isNotBlank() }
                    .associateBy { it.alpha3Code }

                // Find a neighbor that exists in our DB
                val validNeighbor = c.borders.mapNotNull { borderCode ->
                    alpha3Map[borderCode]
                }.firstOrNull()

                if (validNeighbor == null) return false

                randomCountries.add(id)
                country = c

                val neighborAlpha2 = validNeighbor.alpha2Code
                val neighborBordersAlpha3 = c.borders.toSet()

                val pos = generateRandomWithExclusion(0, 3)
                // Wrong options: countries that are NOT neighbors of c
                val (o1, o2, o3, p1, p2, p3) = pickThreeWrongOptions(id, pos) { candidate ->
                    candidate.alpha2Code != neighborAlpha2 &&
                    candidate.alpha3Code.isNotBlank() &&
                    !neighborBordersAlpha3.contains(candidate.alpha3Code)
                }

                val optionList = mutableListOf("", "", "", "")
                optionList[pos] = neighborAlpha2
                optionList[p1] = o1.alpha2Code
                optionList[p2] = o2.alpha2Code
                optionList[p3] = o3.alpha2Code

                val questionText = "¿Cuál de estos países es vecino de ${c.name}?"

                neighborCorrectAlpha2 = neighborAlpha2
                _currentMixType.value = MixQuestionType.NEIGHBOR_CHALLENGE
                _mixQuestionText.value = questionText
                _question.value = ""
                _countryName.value = ""
                _currencyQuestion.value = ""
                _responseOptions.tryEmit(optionList)
                _progress.value = UiModel.Loading(false)
                true
            }
        }
    }

    /**
     * Data class to hold three wrong-option countries and their assigned positions.
     * Destructuring order: o1, o2, o3, p1, p2, p3
     */
    private data class ThreeOptions(
        val o1: Country, val o2: Country, val o3: Country,
        val p1: Int, val p2: Int, val p3: Int
    )

    /**
     * Picks 3 wrong-option countries that satisfy [predicate], and assigns them
     * to the 3 remaining positions after [correctPosition] is taken.
     * Falls back to any country if predicate cannot be satisfied within 50 attempts.
     */
    private suspend fun pickThreeWrongOptions(
        correctId: Int,
        correctPosition: Int,
        predicate: (Country) -> Boolean
    ): ThreeOptions {
        val maxAttempts = 50

        var id1: Int
        var c1: Country
        var att = 0
        do {
            id1 = generateRandomWithExclusion(1, TOTAL_COUNTRIES, correctId)
            c1 = getCountry(id1)
            att++
        } while (!predicate(c1) && att < maxAttempts)
        val p1 = generateRandomWithExclusion(0, 3, correctPosition)

        var id2: Int
        var c2: Country
        att = 0
        do {
            id2 = generateRandomWithExclusion(1, TOTAL_COUNTRIES, correctId, id1)
            c2 = getCountry(id2)
            att++
        } while (!predicate(c2) && att < maxAttempts)
        val p2 = generateRandomWithExclusion(0, 3, correctPosition, p1)

        var id3: Int
        var c3: Country
        att = 0
        do {
            id3 = generateRandomWithExclusion(1, TOTAL_COUNTRIES, correctId, id1, id2)
            c3 = getCountry(id3)
            att++
        } while (!predicate(c3) && att < maxAttempts)
        val p3 = generateRandomWithExclusion(0, 3, correctPosition, p1, p2)

        return ThreeOptions(c1, c2, c3, p1, p2, p3)
    }

    private suspend fun getCountry(id: Int): Country {
        return getCountryById.invoke(id)
    }

    fun showRewardedAd() {
        _showingAds.tryEmit(UiModel.ShowRewardedAd(true))
    }

    /**
     * Registra una respuesta correcta, actualiza racha y emite mensaje motivacional si corresponde.
     * Tambien notifica al sistema de desafios diarios via ChallengeEvent.AnswerGiven.
     */
    fun onCorrectAnswer() {
        correctAnswers++
        currentStreak++
        if (currentStreak > bestStreak) {
            bestStreak = currentStreak
        }

        val message = when (currentStreak) {
            3 -> "¡En racha!"
            5 -> "¡Imparable!"
            10 -> "¡Legendario!"
            15 -> "¡Combo: $currentStreak!"
            else -> if (currentStreak > 15 && currentStreak % 5 == 0) "¡Combo: $currentStreak!" else null
        }
        if (message != null) {
            _streakMessage.tryEmit(message)
        }

        // Notificar al sistema de desafios diarios y registrar dominio del pais
        viewModelScope.launch {
            val alpha2 = if (::country.isInitialized) country.alpha2Code else null
            if (!alpha2.isNullOrBlank()) {
                countryMasteryManager.recordAnswer(alpha2, true, gameModeKey())
            }
            // Progresion del chain regional: contar el acierto para la region activa.
            gameMode.regionalAlpha2?.let { regionalProgressionManager.recordCorrectAnswer(it) }
            val playerLevel = progressionManager.getCurrentLevel()
            dailyChallengeManager.processEvent(
                event = ChallengeEvent.AnswerGiven(isCorrect = true),
                playerLevel = playerLevel
            )
        }
    }

    /**
     * Registra una respuesta incorrecta, resetea la racha actual
     * y notifica al sistema de desafios diarios.
     */
    fun onWrongAnswer() {
        currentStreak = 0

        // Notificar al sistema de desafios diarios y registrar error en el pais
        viewModelScope.launch {
            val alpha2 = if (::country.isInitialized) country.alpha2Code else null
            if (!alpha2.isNullOrBlank()) {
                countryMasteryManager.recordAnswer(alpha2, false, gameModeKey())
            }
            val playerLevel = progressionManager.getCurrentLevel()
            dailyChallengeManager.processEvent(
                event = ChallengeEvent.AnswerGiven(isCorrect = false),
                playerLevel = playerLevel
            )
        }
    }

    /** Retorna la clave de modo de juego usada en CountryMasteryManager. */
    private fun gameModeKey(): String = gameMode.toRouteString()

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

    fun getCorrectAnswer(): String {
        if (gameMode.isRegional) return subdivision?.name ?: ""
        return when (gameMode) {
            is GameMode.CapitalByFlag -> country.capital
            is GameMode.PopulationChallenge -> {
                val pair = _populationPair.value
                if (pair != null) {
                    if (pair.first.population >= pair.second.population) pair.first.alpha2Code
                    else pair.second.alpha2Code
                } else ""
            }
            is GameMode.WorldMix -> {
                when (_currentMixType.value) {
                    MixQuestionType.FLAG_TO_CAPITAL -> country.capital
                    MixQuestionType.NEIGHBOR_CHALLENGE -> neighborCorrectAlpha2
                    else -> country.alpha2Code
                }
            }
            else -> country.alpha2Code
        }
    }

    fun getCode2CountryCorrect(): String? {
        return country.alpha2Code
    }

    private fun generateRandomWithExclusion(start: Int, end: Int, vararg exclude: Int): Int {
        var numRandom = (start..end).random()
        var attempts = 0
        while (exclude.contains(numRandom)) {
            numRandom = (start..end).random()
            if (++attempts > 1000) break
        }
        return numRandom
    }

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
