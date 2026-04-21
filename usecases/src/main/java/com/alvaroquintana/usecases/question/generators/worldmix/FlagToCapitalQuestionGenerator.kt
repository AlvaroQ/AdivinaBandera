package com.alvaroquintana.usecases.question.generators.worldmix

import com.alvaroquintana.domain.MixQuestionType
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.question.GeneratedQuestion
import com.alvaroquintana.usecases.question.GenerationContext
import com.alvaroquintana.usecases.question.QuestionGenerator
import com.alvaroquintana.usecases.question.internal.RandomUtils
import com.alvaroquintana.usecases.question.internal.SingleCountryPicker
import com.alvaroquintana.usecases.question.internal.WrongOptionsPicker

class FlagToCapitalQuestionGenerator(
    getCountryById: GetCountryById,
    totalCountries: Int
) : QuestionGenerator {

    private val picker = SingleCountryPicker(getCountryById, totalCountries)
    private val wrongPicker = WrongOptionsPicker(getCountryById, totalCountries)

    override suspend fun generate(context: GenerationContext): GeneratedQuestion? {
        val (id, country) = picker.pickEligible(
            excludedIds = context.excludedCountryIds,
            maxAttempts = 30,
            filter = { it.capital.isNotBlank() }
        ) ?: return null

        val correctPos = RandomUtils.randomWithExclusion(0, 3)
        val three = wrongPicker.pick(id, correctPos)

        val options = MutableList(4) { "" }
        options[correctPos] = country.capital
        options[three.p1] = three.o1.capital.ifBlank { three.o1.name }
        options[three.p2] = three.o2.capital.ifBlank { three.o2.name }
        options[three.p3] = three.o3.capital.ifBlank { three.o3.name }

        return GeneratedQuestion(
            options = options,
            correctAnswer = country.capital,
            flagIcon = country.icon,
            currentMixType = MixQuestionType.FLAG_TO_CAPITAL,
            selectedCountryId = id,
            selectedCountryAlpha2 = country.alpha2Code
        )
    }
}
