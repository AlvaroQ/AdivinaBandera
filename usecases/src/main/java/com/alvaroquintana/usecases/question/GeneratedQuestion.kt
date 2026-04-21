package com.alvaroquintana.usecases.question

import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.MixQuestionType

data class GeneratedQuestion(
    val options: List<String>,
    val correctAnswer: String,
    val flagIcon: String = "",
    val countryName: String = "",
    val mixQuestionText: String = "",
    val currencyQuestion: String = "",
    val currentMixType: MixQuestionType? = null,
    val populationPair: Pair<Country, Country>? = null,
    val selectedCountryId: Int? = null,
    val selectedCountryAlpha2: String? = null,
    val seenSubdivisionId: String? = null
)
