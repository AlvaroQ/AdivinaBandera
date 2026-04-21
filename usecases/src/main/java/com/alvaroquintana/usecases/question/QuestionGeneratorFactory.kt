package com.alvaroquintana.usecases.question

import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.isRegional
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.GetRandomCountries
import com.alvaroquintana.usecases.GetSubdivisionsForCountry
import com.alvaroquintana.usecases.question.generators.CapitalByFlagQuestionGenerator
import com.alvaroquintana.usecases.question.generators.ClassicQuestionGenerator
import com.alvaroquintana.usecases.question.generators.CurrencyDetectiveQuestionGenerator
import com.alvaroquintana.usecases.question.generators.PopulationChallengeQuestionGenerator
import com.alvaroquintana.usecases.question.generators.SubdivisionQuestionGenerator
import com.alvaroquintana.usecases.question.generators.worldmix.CallingCodeQuestionGenerator
import com.alvaroquintana.usecases.question.generators.worldmix.CurrencyToCountryQuestionGenerator
import com.alvaroquintana.usecases.question.generators.worldmix.DemonymQuestionGenerator
import com.alvaroquintana.usecases.question.generators.worldmix.FlagToCapitalQuestionGenerator
import com.alvaroquintana.usecases.question.generators.worldmix.LanguageQuestionGenerator
import com.alvaroquintana.usecases.question.generators.worldmix.NeighborChallengeQuestionGenerator
import com.alvaroquintana.usecases.question.generators.worldmix.WorldMixQuestionGenerator

/**
 * Selects the right [QuestionGenerator] for a given [GameMode].
 * Instantiates each generator once at construction time and reuses it across calls.
 *
 * ClassicQuestionGenerator is shared with WorldMix's FLAG_TO_COUNTRY subtype —
 * they produce the same output; the composite adds the mix-type tag.
 */
class QuestionGeneratorFactory(
    getCountryById: GetCountryById,
    getRandomCountries: GetRandomCountries,
    getSubdivisionsForCountry: GetSubdivisionsForCountry,
    totalCountries: Int
) {
    private val classic = ClassicQuestionGenerator(getCountryById, totalCountries)
    private val capitalByFlag = CapitalByFlagQuestionGenerator(getCountryById, totalCountries)
    private val currencyDetective = CurrencyDetectiveQuestionGenerator(getCountryById, totalCountries)
    private val populationChallenge = PopulationChallengeQuestionGenerator(getCountryById, totalCountries)
    private val subdivision = SubdivisionQuestionGenerator(getSubdivisionsForCountry)

    private val worldMix = WorldMixQuestionGenerator(
        flagToCountry = classic,
        flagToCapital = FlagToCapitalQuestionGenerator(getCountryById, totalCountries),
        currencyToCountry = CurrencyToCountryQuestionGenerator(getCountryById, totalCountries),
        demonymToCountry = DemonymQuestionGenerator(getCountryById, totalCountries),
        languageToCountry = LanguageQuestionGenerator(getCountryById, totalCountries),
        callingCodeToCountry = CallingCodeQuestionGenerator(getCountryById, totalCountries),
        neighborChallenge = NeighborChallengeQuestionGenerator(getCountryById, getRandomCountries, totalCountries)
    )

    fun forMode(mode: GameMode): QuestionGenerator = when {
        mode.isRegional -> subdivision
        mode is GameMode.Classic -> classic
        mode is GameMode.CapitalByFlag -> capitalByFlag
        mode is GameMode.CurrencyDetective -> currencyDetective
        mode is GameMode.PopulationChallenge -> populationChallenge
        mode is GameMode.WorldMix -> worldMix
        else -> classic
    }
}
