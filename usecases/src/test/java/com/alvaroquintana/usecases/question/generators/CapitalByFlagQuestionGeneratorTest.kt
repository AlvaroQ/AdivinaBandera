package com.alvaroquintana.usecases.question.generators

import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.question.GenerationContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CapitalByFlagQuestionGeneratorTest {

    private lateinit var getCountryById: GetCountryById
    private lateinit var generator: CapitalByFlagQuestionGenerator

    @Before
    fun setUp() {
        getCountryById = mockk()
        generator = CapitalByFlagQuestionGenerator(getCountryById, totalCountries = 10)
    }

    @Test
    fun `filters countries with blank capital and uses country name as fallback in distractors`() = runTest {
        // Country id 0 has blank capital — must be skipped as the main pick
        coEvery { getCountryById.invoke(0) } returns Country(name = "NoCap", alpha2Code = "NC", capital = "")
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(name = "C$id", alpha2Code = "X$id", capital = if (id == 0) "" else "Cap$id")
        }

        val q = generator.generate(GenerationContext(excludedCountryIds = emptySet()))

        assertNotNull(q)
        // Correct answer is a capital string, not alpha2
        assertTrue(q!!.correctAnswer.startsWith("Cap"))
        assertEquals(q.correctAnswer, q.options.first { it == q.correctAnswer })
        // Country name is exposed (not the flag) — CapitalByFlag mode shows name
        assertTrue(q.countryName.startsWith("C"))
        assertEquals("", q.flagIcon)
    }
}
