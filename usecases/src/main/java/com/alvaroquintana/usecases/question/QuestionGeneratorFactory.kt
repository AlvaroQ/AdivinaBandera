package com.alvaroquintana.usecases.question

import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.isRegional
import com.alvaroquintana.usecases.question.generators.CapitalByFlagQuestionGenerator
import com.alvaroquintana.usecases.question.generators.ClassicQuestionGenerator
import com.alvaroquintana.usecases.question.generators.CurrencyDetectiveQuestionGenerator
import com.alvaroquintana.usecases.question.generators.PopulationChallengeQuestionGenerator
import com.alvaroquintana.usecases.question.generators.SubdivisionQuestionGenerator
import com.alvaroquintana.usecases.question.generators.worldmix.WorldMixQuestionGenerator

/**
 * Selects the right [QuestionGenerator] for a given [GameMode].
 * All generators are injected — the factory is stateless.
 */
class QuestionGeneratorFactory(
    private val classic: ClassicQuestionGenerator,
    private val capitalByFlag: CapitalByFlagQuestionGenerator,
    private val currencyDetective: CurrencyDetectiveQuestionGenerator,
    private val populationChallenge: PopulationChallengeQuestionGenerator,
    private val subdivision: SubdivisionQuestionGenerator,
    private val worldMix: WorldMixQuestionGenerator
) {
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
