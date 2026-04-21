package com.alvaroquintana.usecases.question.generators

import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.Currency
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.question.GenerationContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CurrencyDetectiveQuestionGeneratorTest {

    private lateinit var getCountryById: GetCountryById
    private lateinit var generator: CurrencyDetectiveQuestionGenerator

    @Before
    fun setUp() {
        getCountryById = mockk()
        generator = CurrencyDetectiveQuestionGenerator(getCountryById, totalCountries = 10)
    }

    @Test
    fun `builds currency question text with symbol when present`() = runTest {
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(
                name = "C$id", icon = "🏳️$id", alpha2Code = "X$id",
                currencies = listOf(Currency(name = "Cur$id", code = "CUR$id", symbol = "$$id"))
            )
        }

        val q = generator.generate(GenerationContext())

        assertNotNull(q)
        assertTrue(q!!.currencyQuestion.startsWith("¿Qué país usa el Cur"))
        assertTrue(q.currencyQuestion.contains("("))
        assertTrue(q.currencyQuestion.contains(")"))
        // Correct answer is alpha2Code, not currency name
        assertTrue(q.correctAnswer.startsWith("X"))
    }

    @Test
    fun `returns null when no country has currencies`() = runTest {
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(name = "C$id", alpha2Code = "X$id", currencies = emptyList())
        }

        val q = generator.generate(GenerationContext())

        assertTrue(q == null)
    }
}
