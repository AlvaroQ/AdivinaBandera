package com.alvaroquintana.usecases.question.generators.worldmix

import com.alvaroquintana.domain.MixQuestionType
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.GetRandomCountries
import com.alvaroquintana.usecases.question.GeneratedQuestion
import com.alvaroquintana.usecases.question.GenerationContext
import com.alvaroquintana.usecases.question.QuestionGenerator
import com.alvaroquintana.usecases.question.internal.RandomUtils
import com.alvaroquintana.usecases.question.internal.SingleCountryPicker
import com.alvaroquintana.usecases.question.internal.WrongOptionsPicker

class NeighborChallengeQuestionGenerator(
    getCountryById: GetCountryById,
    private val getRandomCountries: GetRandomCountries,
    private val totalCountries: Int
) : QuestionGenerator {

    private val picker = SingleCountryPicker(getCountryById, totalCountries)
    private val wrongPicker = WrongOptionsPicker(getCountryById, totalCountries)

    override suspend fun generate(context: GenerationContext): GeneratedQuestion? {
        val (id, country) = picker.pickEligible(
            excludedIds = context.excludedCountryIds,
            maxAttempts = 30,
            filter = { it.borders.isNotEmpty() }
        ) ?: return null

        val alpha3Map = getRandomCountries.invoke(totalCountries)
            .filter { it.alpha3Code.isNotBlank() }
            .associateBy { it.alpha3Code }

        val validNeighbor = country.borders.mapNotNull { alpha3Map[it] }.firstOrNull() ?: return null
        val neighborAlpha2 = validNeighbor.alpha2Code
        val bordersAlpha3 = country.borders.toSet()

        val correctPos = RandomUtils.randomWithExclusion(0, 3)
        val three = wrongPicker.pick(id, correctPos) { candidate ->
            candidate.alpha2Code != neighborAlpha2 &&
                candidate.alpha3Code.isNotBlank() &&
                !bordersAlpha3.contains(candidate.alpha3Code)
        }

        val options = MutableList(4) { "" }
        options[correctPos] = neighborAlpha2
        options[three.p1] = three.o1.alpha2Code
        options[three.p2] = three.o2.alpha2Code
        options[three.p3] = three.o3.alpha2Code

        return GeneratedQuestion(
            options = options,
            correctAnswer = neighborAlpha2,
            mixQuestionText = "¿Cuál de estos países es vecino de ${country.name}?",
            currentMixType = MixQuestionType.NEIGHBOR_CHALLENGE,
            selectedCountryId = id,
            selectedCountryAlpha2 = country.alpha2Code
        )
    }
}
