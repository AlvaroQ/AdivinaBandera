package com.alvaroquintana.usecases.question.generators

import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.question.GeneratedQuestion
import com.alvaroquintana.usecases.question.GenerationContext
import com.alvaroquintana.usecases.question.QuestionGenerator
import com.alvaroquintana.usecases.question.internal.RandomUtils

class PopulationChallengeQuestionGenerator(
    private val getCountryById: GetCountryById,
    private val totalCountries: Int
) : QuestionGenerator {

    override suspend fun generate(context: GenerationContext): GeneratedQuestion? {
        val excluded = context.excludedCountryIds
        val countryA = pickWithPopulation(excluded.toIntArray()) ?: return null
        val idA = countryA.first
        val cA = countryA.second

        val countryB = pickDistinctOpponent(cA, excluded.toIntArray() + idA) ?: return null
        val cB = countryB.second

        val options = mutableListOf(cA.alpha2Code, cB.alpha2Code)
        val correctAnswer = if (cA.population >= cB.population) cA.alpha2Code else cB.alpha2Code

        return GeneratedQuestion(
            options = options,
            correctAnswer = correctAnswer,
            populationPair = cA to cB,
            selectedCountryId = idA,
            selectedCountryAlpha2 = cA.alpha2Code
        )
    }

    private suspend fun pickWithPopulation(excluded: IntArray, maxAttempts: Int = 200): Pair<Int, Country>? {
        var attempts = 0
        while (attempts < maxAttempts) {
            val id = RandomUtils.randomWithExclusion(0, totalCountries, *excluded)
            val country = getCountryById.invoke(id)
            if (country.population > 0) return id to country
            attempts++
        }
        return null
    }

    private suspend fun pickDistinctOpponent(
        opponentOf: Country,
        excluded: IntArray,
        maxAttempts: Int = 200
    ): Pair<Int, Country>? {
        var attempts = 0
        while (attempts < maxAttempts) {
            val id = RandomUtils.randomWithExclusion(0, totalCountries, *excluded)
            val country = getCountryById.invoke(id)
            if (country.population > 0 && country.population != opponentOf.population) return id to country
            attempts++
        }
        return null
    }
}
