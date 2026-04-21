package com.alvaroquintana.usecases.question.generators

import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.question.GenerationContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Before
import org.junit.Test

class PopulationChallengeQuestionGeneratorTest {

    private lateinit var getCountryById: GetCountryById
    private lateinit var generator: PopulationChallengeQuestionGenerator

    @Before
    fun setUp() {
        getCountryById = mockk()
        generator = PopulationChallengeQuestionGenerator(getCountryById, totalCountries = 10)
    }

    @Test
    fun `picks two distinct countries with positive population`() = runTest {
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(name = "C$id", alpha2Code = "X$id", population = (id + 1) * 1_000_000)
        }

        val q = generator.generate(GenerationContext())

        assertNotNull(q)
        val pair = q!!.populationPair!!
        assertEquals(2, q.options.size)
        assertNotSame(pair.first.alpha2Code, pair.second.alpha2Code)
    }

    @Test
    fun `correctAnswer is alpha2 of the higher-population country`() = runTest {
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(name = "C$id", alpha2Code = "X$id", population = (id + 1) * 1_000_000)
        }

        val q = generator.generate(GenerationContext())

        val pair = q!!.populationPair!!
        val expected = if (pair.first.population >= pair.second.population) pair.first.alpha2Code
            else pair.second.alpha2Code
        assertEquals(expected, q.correctAnswer)
    }
}
