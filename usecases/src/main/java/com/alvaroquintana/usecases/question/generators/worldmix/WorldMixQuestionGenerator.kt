package com.alvaroquintana.usecases.question.generators.worldmix

import com.alvaroquintana.domain.MixQuestionType
import com.alvaroquintana.usecases.question.GeneratedQuestion
import com.alvaroquintana.usecases.question.GenerationContext
import com.alvaroquintana.usecases.question.QuestionGenerator
import com.alvaroquintana.usecases.question.generators.ClassicQuestionGenerator

/**
 * Composite generator for WorldMix mode. Tries each registered sub-generator in random
 * order and returns the first one that produces a valid question. Falls back to the
 * Classic generator (FLAG_TO_COUNTRY) if none succeed.
 */
class WorldMixQuestionGenerator(
    private val flagToCountry: ClassicQuestionGenerator,
    private val flagToCapital: FlagToCapitalQuestionGenerator,
    private val currencyToCountry: CurrencyToCountryQuestionGenerator,
    private val demonymToCountry: DemonymQuestionGenerator,
    private val languageToCountry: LanguageQuestionGenerator,
    private val callingCodeToCountry: CallingCodeQuestionGenerator,
    private val neighborChallenge: NeighborChallengeQuestionGenerator
) : QuestionGenerator {

    private val candidates: List<Pair<MixQuestionType, QuestionGenerator>>
        get() = listOf(
            MixQuestionType.FLAG_TO_COUNTRY to flagToCountry,
            MixQuestionType.FLAG_TO_CAPITAL to flagToCapital,
            MixQuestionType.CURRENCY_TO_COUNTRY to currencyToCountry,
            MixQuestionType.DEMONYM_TO_COUNTRY to demonymToCountry,
            MixQuestionType.LANGUAGE_TO_COUNTRY to languageToCountry,
            MixQuestionType.CALLING_CODE_TO_COUNTRY to callingCodeToCountry,
            MixQuestionType.NEIGHBOR_CHALLENGE to neighborChallenge
        )

    override suspend fun generate(context: GenerationContext): GeneratedQuestion? {
        for ((type, generator) in candidates.shuffled()) {
            val q = generator.generate(context) ?: continue
            return if (type == MixQuestionType.FLAG_TO_COUNTRY) {
                q.copy(currentMixType = MixQuestionType.FLAG_TO_COUNTRY)
            } else {
                q
            }
        }
        return flagToCountry.generate(context)?.copy(currentMixType = MixQuestionType.FLAG_TO_COUNTRY)
    }
}
