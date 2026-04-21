package com.alvaroquintana.usecases.question.generators.worldmix

import com.alvaroquintana.domain.MixQuestionType
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.question.GeneratedQuestion
import com.alvaroquintana.usecases.question.GenerationContext
import com.alvaroquintana.usecases.question.QuestionGenerator
import com.alvaroquintana.usecases.question.internal.RandomUtils
import com.alvaroquintana.usecases.question.internal.SingleCountryPicker
import com.alvaroquintana.usecases.question.internal.WrongOptionsPicker

class DemonymQuestionGenerator(
    getCountryById: GetCountryById,
    totalCountries: Int
) : QuestionGenerator {

    private val picker = SingleCountryPicker(getCountryById, totalCountries)
    private val wrongPicker = WrongOptionsPicker(getCountryById, totalCountries)

    override suspend fun generate(context: GenerationContext): GeneratedQuestion? {
        val (id, country) = picker.pickEligible(
            excludedIds = context.excludedCountryIds,
            maxAttempts = 30,
            filter = { it.demonym.isNotBlank() }
        ) ?: return null

        val correctDemonym = country.demonym
        val correctPos = RandomUtils.randomWithExclusion(0, 3)
        val three = wrongPicker.pick(id, correctPos) { it.demonym != correctDemonym }

        val options = MutableList(4) { "" }
        options[correctPos] = country.alpha2Code
        options[three.p1] = three.o1.alpha2Code
        options[three.p2] = three.o2.alpha2Code
        options[three.p3] = three.o3.alpha2Code

        return GeneratedQuestion(
            options = options,
            correctAnswer = country.alpha2Code,
            mixQuestionText = "¿De qué país son los $correctDemonym?",
            currentMixType = MixQuestionType.DEMONYM_TO_COUNTRY,
            selectedCountryId = id,
            selectedCountryAlpha2 = country.alpha2Code
        )
    }
}
